package com.application.colloquium.model;

public class Secret {

    private String uid1;
    private String uid2;
    private int secretKey;

    public Secret(String uid1, String uid2, int secretKey) {
        this.uid1 = uid1;
        this.uid2 = uid2;
        this.secretKey = secretKey;
    }

    public Secret() {
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

    public int getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(int secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    public String toString() {
        return "Secret{" +
                "uid1='" + uid1 + '\'' +
                ", uid2='" + uid2 + '\'' +
                ", secretKey=" + secretKey +
                '}';
    }
}
