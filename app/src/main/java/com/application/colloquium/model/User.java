package com.application.colloquium.model;

public class User {

    private String phoneNo;
    private String name;

    public User(String phoneNo, String name) {
        this.phoneNo = phoneNo;
        this.name = name;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "phoneNo='" + phoneNo + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
