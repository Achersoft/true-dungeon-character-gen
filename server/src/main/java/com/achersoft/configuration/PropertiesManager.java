package com.achersoft.configuration;

import lombok.Data;

@Data
public class PropertiesManager {
    public int sessionTimeout = 43200000;
    public int failedLoginLockTimeout = 1800000;
    public int maxLoginAttempts = 5;
}