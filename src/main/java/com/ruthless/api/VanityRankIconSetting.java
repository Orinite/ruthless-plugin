package com.ruthless.api;

public enum VanityRankIconSetting {
    NEVER("Never"),
    BEFORE_USERNAME("Before Username"),
    AFTER_USERNAME("After Username");

    private final String name;
    VanityRankIconSetting(String name)
    {
        this.name = name;
    }
}
