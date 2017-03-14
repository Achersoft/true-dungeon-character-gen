package com.achersoft.exception;

public class UnknownException extends RuntimeException {
    
    public String error;
    
    public UnknownException(String error) {
        this.error = error;
    }
}
