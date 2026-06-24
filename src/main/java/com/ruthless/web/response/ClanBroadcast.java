package com.ruthless.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClanBroadcast {

    private int id;
    private String clanDiscordGuildId;
    private long startsAt;
    private long expiresAt;
    private String message;
}
