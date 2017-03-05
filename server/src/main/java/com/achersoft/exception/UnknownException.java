package com.achersoft.exception;

public class UnknownException extends RuntimeException {
    
    public final SystemError error;
   
    public UnknownException(SystemError error) {
        this.error = error;
    }
}
