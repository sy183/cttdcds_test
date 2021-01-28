package com.suy.util;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TestTextUtil {
    @Test
    public void test() {
        String src = null;
        try {
            src = FileUtils.readFileToString(new File("message/sip/conferenceControl.sip"), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (src == null) {
            System.exit(-1);
        }

        Map<String, Object> config = new HashMap<>();
        config.put("remote_num", "cttdcds");
        config.put("remote_ip", "10.16.50.13");
        config.put("remote_port", 5080);
        config.put("local_num", "0000");
        config.put("local_ip", "10.16.0.169");
        config.put("local_port", 5080);
        TextUtil textUtil = new TextUtil(config, src);
        System.out.println(textUtil.getText());
    }
}
