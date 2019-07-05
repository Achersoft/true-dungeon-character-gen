package com.achersoft.security.helpers;

import com.achersoft.exception.AuthenticationException;
import com.achersoft.exception.SystemError;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class PasswordHelper {
    
    public static String generatePasswordHash(String password) throws AuthenticationException
    {
        try {
            int iterations = 1000;
            char[] chars = password.toCharArray();
            byte[] salt = getSalt().getBytes();
            
            PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            
            return toHex(skf.generateSecret(spec).getEncoded());
        } catch (Exception ex) {
            throw new AuthenticationException("Password hash error.");
        }
    }
     
    private static String getSalt() throws NoSuchAlgorithmException
    {
        return "5b4240333032306222";
    }
     
    private static String toHex(byte[] array) throws NoSuchAlgorithmException
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0"  +paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }}
