package com.achersoft.exception;

import lombok.Builder;

@Builder
public class SystemError {
    
    public static final int UNKNOWN_EXCEPTION = 1;
    public static final int USER_BAD_CREDENTIALS = 1;
    public static final int USER_EXPIRED_PASSWORD = 1;
    public static final int SESSION_TIMEOUT = 1;
    public static final int INVALID_SESSION = 1;
    public static final int INSUFFICIENT_PRIVILEGES = 1;
    public static final int INVALID_REQUEST_DATA = 1;
    
    public int status;
    public int code;
    public String message;
}
