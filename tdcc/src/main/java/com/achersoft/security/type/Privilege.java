package com.achersoft.security.type;

public enum Privilege {
    //ADMIN operations
    ADMIN("Admin", "Allows access to system administrative functions"),
    SYSTEM_USER("System User", "Allows access to user functions");
    
    private final String text;
    private final String description;

    Privilege(String text, String description) {
        this.text = text;
        this.description = description;
    }
    
    public String text() {
        return text;
    }

    public String description() {
        return description;
    }
}

