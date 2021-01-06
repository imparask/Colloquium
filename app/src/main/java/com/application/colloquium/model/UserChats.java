package com.application.colloquium.model;

import java.util.List;

public class UserChats {

    private User user;
    private String[] userChats;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String[] getUserChats() {
        return userChats;
    }

    public void setUserChats(String[] userChats) {
        this.userChats = userChats;
    }
}
