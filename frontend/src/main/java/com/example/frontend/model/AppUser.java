package com.example.frontend.model;

public class AppUser {
    private Integer id;
    private String name;
    private String email;
    private String password;

    public AppUser() {
    }

    public AppUser(Integer id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        if (id != null) {
            this.id = id;
        }
    }

    public void setId(String id) {
        this.id = (id == null || id.isEmpty()) ? null : Integer.parseInt(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
