package com.achersoft.exception;

public class InvalidDataException extends RuntimeException {

    public String error;
    
    public InvalidDataException(String error) {
        this.error = error;
    }
}
