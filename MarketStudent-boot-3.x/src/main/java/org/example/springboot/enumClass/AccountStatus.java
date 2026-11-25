package org.example.springboot.enumClass;

import lombok.Getter;

@Getter
public enum AccountStatus {
    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final Integer value;
    private final String description;

    AccountStatus(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

}