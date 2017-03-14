package com.achersoft.exception;

public class AuthenticationException extends RuntimeException {

    public String error;
    
    public AuthenticationException(String error) {
        this.error = error;
    }
}
