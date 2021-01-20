package com.application.colloquium.model;

public class PrivateKey {

    private String uid1;
    private String uid2;
    private int secret;

    public PrivateKey(String uid1, String uid2, int secret) {
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.secret = secret;
    }

    public PrivateKey() {
    }

    public String getUid1() {
        return uid1;
    }

    public void setUid1(String uid1) {
        this.uid1 = uid1;
    }

    public String getUid2() {
        return uid2;
    }

    public void setUid2(String uid2) {
        this.uid2 = uid2;
    }

    public int getSecret() {
        return secret;
    }

    public void setSecret(int secret) {
        this.secret = secret;
    }

    @Override
    public String toString() {
        return "SecretKey{" +
                "uid1='" + uid1 + '\'' +
                ", uid2='" + uid2 + '\'' +
                ", secret=" + secret +
                '}';
    }
}
