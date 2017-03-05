package com.achersoft.exception;

public class InvalidDataException extends UnknownException {

    public InvalidDataException(String error) {
        super(SystemError.builder()
                    .code(SystemError.INVALID_REQUEST_DATA)                                               
                    .message(error)                                               
                    .build());
    }
}
