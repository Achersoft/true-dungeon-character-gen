package com.achersoft.exception;

public class AuthenticationException extends UnknownException {

    public AuthenticationException(int code, String error) {
        super(SystemError.builder()
                    .code(code)                                               
                    .message(error)                                               
                    .build());
    }
}
