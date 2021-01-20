package com.application.colloquium.model;

public class PublicKey {

    private long prime;
    private long primitive;
    private long publicKey;

    public PublicKey(long prime, long primitive, long publicKey) {
        this.prime = prime;
        this.primitive = primitive;
        this.publicKey = publicKey;
    }

    public PublicKey() {
    }

    public long getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(long publicKey) {
        this.publicKey = publicKey;
    }

    public long getPrime() {
        return prime;
    }

    public void setPrime(long prime) {
        this.prime = prime;
    }

    public long getPrimitive() {
        return primitive;
    }

    public void setPrimitive(long primitive) {
        this.primitive = primitive;
    }
}
