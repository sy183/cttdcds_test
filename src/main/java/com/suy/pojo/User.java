package com.suy.pojo;

public class User {
    private Integer id;
    private String name;
    private String number;
    private String server;

    public User() {
    }

    public User(Integer id, String name, String number, String server) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.server = server;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
