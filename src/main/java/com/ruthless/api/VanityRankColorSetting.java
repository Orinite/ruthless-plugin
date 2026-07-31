package com.ruthless.api;

public enum VanityRankColorSetting {
    NEVER("Never"),
    CHATBOX("Chatbox"),
    CLAN_PANEL("Clan Panel"),
    ALL("All");

    private final String name;
    VanityRankColorSetting(String name)
    {
        this.name = name;
    }
}
