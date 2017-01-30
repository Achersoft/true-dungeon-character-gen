package com.achersoft.security.providers;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.ext.Provider;

@Provider
public class SignatureServiceProvider {
    
    private final Mac sha;
    
    public SignatureServiceProvider(SecretKeySpec signatureKey) throws NoSuchAlgorithmException, InvalidKeyException {
        sha = Mac.getInstance("HmacSHA256");
        sha.init(signatureKey);
    }
    
    public byte[] sign(byte[] bytes) {
        return sha.doFinal(bytes);
    }
}
