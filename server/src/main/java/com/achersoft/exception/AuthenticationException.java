package com.achersoft.exception;

public class AuthenticationException extends EstaffException {

    public AuthenticationException(int code, String error) {
        super(SystemError.builder()
                    .code(code)                                               
                    .message(error)                                               
                    .build());
    }
}
