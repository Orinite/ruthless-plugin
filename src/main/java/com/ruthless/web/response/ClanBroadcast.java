package com.ruthless.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClanBroadcast {

    private int id;
    private int clanId;
    private String startsAt;
    private String expiresAt;
    private String message;
    private String createdAt;
    private String updatedAt;
}