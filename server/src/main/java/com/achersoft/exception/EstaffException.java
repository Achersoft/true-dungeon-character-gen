package com.achersoft.exception;

public class EstaffException extends RuntimeException {
    
    public final SystemError error;
   
    public EstaffException(SystemError error) {
        this.error = error;
    }
}
