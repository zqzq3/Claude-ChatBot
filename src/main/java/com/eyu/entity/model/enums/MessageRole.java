package com.eyu.entity.model.enums;

public enum MessageRole {
    USER("user"),
    SYSTEM("system"),
    ASSISTANT("assistant")
    ;

    private final String name;

    MessageRole(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
