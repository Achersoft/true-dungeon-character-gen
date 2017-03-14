package com.achersoft.exception;

import lombok.Builder;

@Builder
public class SystemError {
    public int status;
    public String message;
}
