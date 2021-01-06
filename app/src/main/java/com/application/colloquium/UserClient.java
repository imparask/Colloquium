package com.application.colloquium;

import android.app.Application;

import com.application.colloquium.model.User;

public class UserClient extends Application {

    private User user = null;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
