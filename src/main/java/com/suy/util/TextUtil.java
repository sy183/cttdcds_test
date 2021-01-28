package com.suy.util;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Replace {
    int start;
    int end;

    public Replace() {
    }

    public Replace(int start, int end) {
        this.start = start;
        this.end = end;
    }
}

class ParamsReplace extends Replace {
    String param;

    public ParamsReplace() {
    }

    public ParamsReplace(int start, int end, String param) {
        super(start, end);
        this.param = param;
    }
}

class StringReplace extends Replace {
    String str;

    public StringReplace() {
    }

    public StringReplace(int start, int end, String str) {
        super(start, end);
        this.str = str;
    }
}

class EscapeReplace extends StringReplace {
    boolean isEscape;

    public EscapeReplace() {
    }

    public EscapeReplace(int start, int end, String str, boolean isEscape) {
        super(start, end, str);
        this.isEscape = isEscape;
    }
}

public class TextUtil {

    public static class Node {
        protected String name = ".";
        protected Map<String, Object> vars = new LinkedHashMap<>();
        protected List<Replace> replaces = new ArrayList<>();
        transient protected Node parent;
        protected String src = "";

        static final Pattern pattern = Pattern.compile("\\$\\{|\\$\\(|}|\\)`|`");

        private static class NodeStatus extends Node {
            transient private int srcOffset;
            transient private boolean isParam;
            transient private boolean isKey;
            transient private boolean isValue;
            transient private int keywordStart, keywordEnd;
            transient private int paramStart, paramEnd;
            transient private int keyStart, keyEnd;
            transient private int valueStart, valueEnd;
            transient private int paramRealStart, paramRealEnd;
            transient private int keyRealStart, valueRealEnd;
            transient private String key;
            transient private NodeStatus value;
            transient private EscapeReplace escape;

            public NodeStatus() {
            }

            public NodeStatus(String name, String src) {
                this.name = name;
                this.src = src;
            }

            public NodeStatus(String key, NodeStatus parentStatus) {
                this.srcOffset = parentStatus.valueStart;
                this.name = key;
                this.src = parentStatus.src;
                this.parent = parentStatus;
                parentStatus.value = this;
            }
        }

        private Node() {
        }

        private Node(String src) {
            this.src = src;
            parseSrc();
        }

        private Node(Map<String, Object> vars) {
            putVars(vars);
        }

        private Node(Map<String, Object> vars, String src) {
            putVars(vars);
            this.src = src;
            parseSrc();
        }

        private void putVars(Map<String, Object> vars) {
            this.vars.putAll(vars);
        }

        private void parseSrc() {
            if (src == null) {
                return;
            }

            NodeStatus rootStatus = new NodeStatus(name, src);
            NodeStatus status = rootStatus;
            Matcher matcher = pattern.matcher(src);

            while (true) {
                if (matcher.find()) {
                    String match = matcher.group();
                    LoggerUtil.logger.trace("Match keyword %s [start_index: %d, end_index: %d]".
                            formatted(match, status.keywordStart, status.keywordEnd));

                    switch (match) {
                        case "${" -> {
                            status.keywordStart = matcher.start();
                            status.keywordEnd = matcher.end();

                            if (status.isKey) {
                                LoggerUtil.logger.warn("Except get ')`' but get '${");
                                clearKey(status);
                            }

                            if (status.isParam) {
                                LoggerUtil.logger.warn("Except get '}' but get '${");
                                clearParam(status);
                            }

                            if ((status.escape = checkEscape(status)) != null) {
                                LoggerUtil.logger.trace("Match escape %s [start_index: %d, end_index: %d], replace %s".
                                        formatted(status.src.substring(status.escape.start + status.srcOffset,
                                                status.escape.end + status.srcOffset), status.keywordStart,
                                                status.keywordEnd, status.escape.str));
                                if (!status.escape.isEscape) {
                                    LoggerUtil.logger.debug("Escape not effect, start parse param");
                                    paramStart(status);
                                } else {
                                    LoggerUtil.logger.debug("Escape effect, skip parse param");
                                    addEscape(status);
                                }
                            } else {
                                LoggerUtil.logger.info("Start parse param");
                                paramStart(status);
                            }
                        }

                        case "$(" -> {
                            status.keywordStart = matcher.start();
                            status.keywordEnd = matcher.end();

                            if (status.isKey) {
                                LoggerUtil.logger.warn("Except get ')`' but get '$(");
                                clearKey(status);
                            }

                            if (status.isParam) {
                                LoggerUtil.logger.warn("Except get '}' but get '$(");
                                clearParam(status);
                            }

                            if ((status.escape = checkEscape(status)) != null) {
                                LoggerUtil.logger.trace("Match escape %s [start_index: %d, end_index: %d], replace %s".
                                        formatted(status.src.substring(status.escape.start + status.srcOffset,
                                                status.escape.end + status.srcOffset), status.keywordStart,
                                                status.keywordEnd, status.escape.str));
                                if (!status.escape.isEscape) {
                                    LoggerUtil.logger.debug("Escape not effect, start parse key");
                                    keyStart(status);
                                } else {
                                    LoggerUtil.logger.debug("Escape effect, skip parse key");
                                    addEscape(status);
                                }
                            } else {
                                LoggerUtil.logger.info("Start parse key");
                                keyStart(status);
                            }

                        }

                        case "}" -> {
                            status.keywordStart = matcher.start();
                            status.keywordEnd = matcher.end();

                            if (status.isParam && !status.isKey) {
                                paramEnd(status);
                            } else if (status.isKey) {
                                LoggerUtil.logger.warn("Except get ')`' but get '}");
                                clearKey(status);
                            }
                        }

                        case ")`" -> {
                            status.keywordStart = matcher.start();
                            status.keywordEnd = matcher.end();

                            if (status.isKey && !status.isParam) {
                                String key = keyEnd(status);
                                if (key != null) {
                                    LoggerUtil.logger.info("Start parse value");
                                    status = valueStart(key, status);
                                }
                            } else if (status.isParam) {
                                LoggerUtil.logger.warn("Except get '}' but get ')`");
                                clearParam(status);
                            }
                        }

                        case "`" -> {
                            status.keywordStart = matcher.start();
                            status.keywordEnd = matcher.end();

                            if (status.isKey) {
                                LoggerUtil.logger.warn("Except get ')`' but get '`");
                                clearKey(status);
                            }

                            if (status.isParam) {
                                LoggerUtil.logger.warn("Except get '}' but get '`");
                                clearParam(status);
                            }

                            if (status != rootStatus) {
                                if ((status.escape = checkEscape(status)) != null) {
                                    LoggerUtil.logger.trace("Match escape %s [start_index: %d, end_index: %d], replace %s".
                                            formatted(status.src.substring(status.escape.start + status.srcOffset,
                                                    status.escape.end + status.srcOffset), status.keywordStart,
                                                    status.keywordEnd, status.escape.str));
                                    if (!status.escape.isEscape) {
                                        LoggerUtil.logger.debug("Escape not effect, stop parse value");
                                        addEscape(status);
                                        status = valueEnd(status);
                                    } else {
                                        LoggerUtil.logger.debug("Escape effect, continue parse value");
                                        addEscape(status);
                                    }
                                } else {
                                    LoggerUtil.logger.info("Value parse completed");
                                    status = valueEnd(status);
                                }
                            }
                        }
                    }
                } else {
                    break;
                }
            }

            if (status.isParam) {
                LoggerUtil.logger.warn("Param missing keyword '}'");
            }

            if (status.isKey) {
                LoggerUtil.logger.warn("Key missing keyword ')`'");
            }

            while (status != rootStatus) {
                LoggerUtil.logger.warn("Value missing keyword '`'");
                status = valueEndNoClose(status);
            }

            merge(rootStatus, this);

        }

        private static Node copy(Node node) {
            Node newNode = new Node();
            newNode.src = node.src;
            newNode.name = node.name;
            newNode.parent = null;
            newNode.replaces.addAll(node.replaces);
            node.vars.forEach((key, value) -> {
                if (value instanceof Node) {
                    Node child = copy((Node) value);
                    newNode.vars.put(key, child);
                    child.parent = newNode;
                } else {
                    newNode.vars.put(key, value);
                }
            });
            return newNode;
        }

        private static void merge(Node src, Node dst) {
            dst.replaces.addAll(src.replaces);
            src.vars.forEach((key, value) -> {
                if (value instanceof Node) {
                    Node child = copy((Node) value);
                    dst.vars.put(key, child);
                    child.parent = dst;
                } else {
                    dst.vars.put(key, value);
                }
            });
        }

        private static void paramStart(NodeStatus status) {
            status.isParam = true;
            status.paramStart = status.keywordEnd;
            status.paramRealStart = status.keywordStart;
        }

        private static void keyStart(NodeStatus status) {
            status.isKey = true;
            status.keyStart = status.keywordEnd;
            status.keyRealStart = status.keywordStart;
        }

        private static NodeStatus valueStart(String key, NodeStatus status) {
            status.isValue = true;
            status.valueStart = status.keywordEnd;
            return new NodeStatus(key, status);
        }

        private static void paramEnd(NodeStatus status) {
            status.paramEnd = status.keywordStart;
            status.paramRealEnd = status.keywordEnd;
            if (status.paramStart > status.paramEnd) {
                LoggerUtil.logger.fatal("Logical error: paramStart > paramEnd");
                return;
            }
            String param = checkParam(status.src.substring(status.paramStart, status.paramEnd));
            if (param != null) {
                LoggerUtil.logger.info("Param parse completed: [%s]".formatted(param));
                addParam(status, param);
                addEscape(status);
                status.isParam = false;
            } else {
                LoggerUtil.logger.warn("Illegal param [%s]".formatted(status.src.substring(status.paramStart, status.paramEnd)));
                clearParam(status);
            }
        }

        private static String keyEnd(NodeStatus status) {
            status.keyEnd = status.keywordStart;
            if (status.keyStart > status.keyEnd) {
                LoggerUtil.logger.fatal("Logical error: keyStart > keyEnd");
                return null;
            }
            String key = checkKey(status.src.substring(status.keyStart, status.keyEnd));
            if (key != null) {
                LoggerUtil.logger.info("Key parse completed: [%s]".formatted(key));
                status.key = key;
                status.isKey = false;
            } else {
                LoggerUtil.logger.warn("Illegal key [%s]".formatted(status.src.substring(status.keyStart, status.keyEnd)));
                clearKey(status);
            }
            return key;
        }

        private static NodeStatus valueEnd(NodeStatus status) {
            NodeStatus parentStatus = (NodeStatus) status.parent;
            parentStatus.valueEnd = status.keywordStart;
            parentStatus.valueRealEnd = status.keywordEnd;
            String value = parentStatus.src.substring(parentStatus.valueStart, parentStatus.valueEnd);

            if (status.replaces.size() == 0 && status.vars.size() == 0) {
                LoggerUtil.logger.info("Value only string");
                parentStatus.vars.put(parentStatus.key, value);
            } else {
                LoggerUtil.logger.info("Value have params or sub var");
                parentStatus.vars.put(parentStatus.key, status);
                status.src = value;
            }

            LoggerUtil.logger.info("Var[%s] will be replaced with value[%s]".
                    formatted(parentStatus.src.substring(parentStatus.keyRealStart, parentStatus.valueRealEnd).replaceAll("\\r\\n|\\r|\\n", "\\\\n"),
                            value.replaceAll("\\n", "\\\\n")));
            addParam(parentStatus, parentStatus.key);
            addEscape(parentStatus);

            parentStatus.key = null;
            parentStatus.value = null;
            parentStatus.isValue = false;

            return parentStatus;
        }

        private static NodeStatus valueEndNoClose(NodeStatus status) {
            NodeStatus parentStatus = (NodeStatus) status.parent;
            if (status.replaces != null) {
                status.replaces.forEach(replace -> {
                    replace.start += status.srcOffset - parentStatus.srcOffset;
                    replace.end += status.srcOffset - parentStatus.srcOffset;
                });
                parentStatus.vars.putAll(status.vars);
                parentStatus.replaces.addAll(status.replaces);
            }
            return parentStatus;
        }

        private static String checkKey(String key) {
            key = key.trim();
            Pattern pattern = Pattern.compile("[\\s$\\\\]");
            Matcher matcher = pattern.matcher(key);
            if (matcher.find()) {
                return null;
            }
            return key;
        }

        private static String checkParam(String param) {
            param = param.trim();
            Pattern pattern = Pattern.compile("[\\s$\\\\]");
            Matcher matcher = pattern.matcher(param);
            if (matcher.find()) {
                return null;
            }
            return param;
        }

        private static void addParam(NodeStatus status, String param) {
            int start, end;
            if (status.isParam) {
                start = status.paramRealStart - status.srcOffset;
                end = status.paramRealEnd - status.srcOffset;
            } else if (status.isValue) {
                start = status.keyRealStart - status.srcOffset;
                end = status.valueRealEnd - status.srcOffset;
            } else {
                return;
            }
            status.replaces.add(new ParamsReplace(start, end, param));
        }

        private static void clearParam(NodeStatus status) {
            status.isParam = false;
            if (status.escape != null) {
                status.escape = null;
            }
        }

        private static void clearKey(NodeStatus status) {
            status.isKey = false;
            if (status.escape != null) {
                status.escape = null;
            }
        }

        private static EscapeReplace checkEscape(NodeStatus status) {
            EscapeReplace escape = null;
            int slashCount, escapeI = status.keywordStart - 1;
            String src = status.src;

            while (escapeI >= 0 && src.charAt(escapeI) == '\\') --escapeI;
            slashCount = status.keywordStart - 1 - escapeI;
            if (slashCount != 0) {
                escape = new EscapeReplace(escapeI + 1 - status.srcOffset, status.keywordStart - status.srcOffset,
                        "\\".repeat(slashCount / 2), slashCount % 2 != 0);
            }

            return escape;
        }

        private static void addEscape(NodeStatus status) {
            if (status.escape != null) {
                status.replaces.add(status.escape);
                status.escape = null;
            }
        }

        @Override
        public String toString() {
            return GsonUtil.gson.toJson(this);
        }

    }

    private final Node root;
    private final Set<Node> recurSet = new HashSet<>();

    public TextUtil() {
        root = new Node();
    }

    public TextUtil(String src) {
        root = new Node(src);
    }

    public TextUtil(Map<String, Object> vars) {
        root = new Node(vars);
    }

    public TextUtil(Map<String, Object> vars, String src) {
        root = new Node(vars, src);
    }

    public Object findValue(Node root, String[] path) {
        Object curVar = root;

        for (int i = 0; i < path.length - 1; i++) {
            if (path[i].equals("") || path[i].equals(".")) {
                continue;
            } else if (path[i].equals("..")) {
                if ((curVar = ((Node) curVar).parent) == null) {
                    return null;
                }
                continue;
            }

            Object temp = ((Node) curVar).vars.get(path[i]);
            if (!(temp instanceof Node)) {
                return null;
            }
            curVar = temp;
        }

        if (path.length == 0 || (curVar = ((Node) curVar).vars.get(path[path.length - 1])) != null) {
            return curVar;
        }
        return null;
    }

    public Object findValue(String[] path) {
        return findValue(root, path);
    }

    public Object findValue(Node root, String path) {
        return findValue(root, path.split("/"));
    }

    public Object findValue(String path) {
        return findValue(root, path.split("/"));
    }

    public boolean setValue(Node root, Object value, String[] path) {
        if (path.length == 0) {
            return false;
        }
        String[] dirPath = new String[path.length - 1];
        System.arraycopy(path, 0, dirPath, 0, dirPath.length);
        Object dirValue = findValue(root, dirPath);
        if (!(dirValue instanceof Node)) {
            return false;
        }
        Node node = (Node) dirValue;
        node.vars.put(path[path.length - 1], value);
        return true;
    }

    public boolean setValue(Object value, String[] path) {
        return setValue(root, value, path);
    }

    public boolean setValue(Node root, Object value, String path) {
        return setValue(root, value, path.split("/"));
    }

    public boolean setValue(Object value, String path) {
        return setValue(root, value, path.split("/"));
    }

    public boolean setValues(Node root, Map<String, Object> values, String[] path) {
        Object dirValue = findValue(root, path);
        if (!(dirValue instanceof Node)) {
            return false;
        }
        Node node = (Node) dirValue;
        node.putVars(values);
        return true;
    }

    public boolean setValues(Map<String, Object> vars, String[] path) {
        return setValues(root, vars, path);
    }

    public boolean setValues(Node root, Map<String, Object> values, String path) {
        return setValues(root, values, path.split("/"));
    }

    public boolean setValues(Map<String, Object> values, String path) {
        return setValues(root, values, path.split("/"));
    }

    @SuppressWarnings("all")
    private String getTextRecur(Node root) {
        StringBuilder builder = new StringBuilder();
        int[] range = new int[2];

        root.replaces.sort(new Comparator<Replace>() {
            @Override
            public int compare(Replace o1, Replace o2) {
                return o1.start - o2.start;
            }
        });

        root.replaces.forEach((replace -> {
            range[0] = replace.start;
            if (range[1] <= range[0]) {
                builder.append(root.src.substring(range[1], range[0]));
            }

            Object value = null;
            String strValue = null;
            if (replace instanceof ParamsReplace) {
                String key = ((ParamsReplace) replace).param;
                if (key.contains("/")) {
                    value = findValue(root, key);
                } else {
                    Node cur = root;
                    while (cur != null) {
                        value = cur.vars.get(key);
                        if (value == null) {
                            cur = cur.parent;
                            continue;
                        }
                        break;
                    }
                }

                if (value instanceof Node) {
                    recurSet.add(root);
                    if (!recurSet.contains(value)) {
                        strValue = getTextRecur((Node) value);
                    }
                    recurSet.remove(root);
                } else if (value instanceof Function) {
                    try {
                        strValue = (String) ((Function) value).apply(root);
                    } catch (Exception ignored) {
                    }
                } else if (value != null) {
                    strValue = value.toString();
                }
            } else if (replace instanceof StringReplace) {
                strValue = ((StringReplace) replace).str;
            }

            range[1] = replace.end;
            if (strValue != null) {
                builder.append(strValue);
            } else {
                builder.append(root.src.substring(range[0], range[1]));
            }
        }));
        builder.append(root.src.substring(range[1], root.src.length()));
        return builder.toString();
    }

    public String getText(Node root) {
        recurSet.clear();
        String ret = getTextRecur(root);
        recurSet.clear();
        return ret;
    }

    public String getText() {
        recurSet.clear();
        String ret = getTextRecur(root);
        recurSet.clear();
        return ret;
    }

    @Override
    public String toString() {
        return "";
    }
}

