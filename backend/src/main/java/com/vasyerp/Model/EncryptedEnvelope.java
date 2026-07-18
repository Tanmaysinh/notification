package com.vasyerp.Model;


/** Matches the frontend's { iv, ciphertext } JSON shape exactly. */
public class EncryptedEnvelope {
    private String iv;
    private String ciphertext;

    public String getIv() { return iv; }
    public void setIv(String iv) { this.iv = iv; }

    public String getCiphertext() { return ciphertext; }
    public void setCiphertext(String ciphertext) { this.ciphertext = ciphertext; }
}