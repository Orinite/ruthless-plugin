package com.ruthless.api;

public enum ClanBroadcastType {
    BROADCAST("Broadcast"),
    GAME_MESSAGE("Game Message");

    private final String name;
    ClanBroadcastType(String name)
    {
        this.name = name;
    }
}
