package com.ruthless.web.response;

import lombok.Getter;

import java.util.Map;

@Getter
public class ClanBroadcastAcknowledgement {
    private int id;
    private int clanId;
    private int clanBroadcastId;
    private int clanMemberId;
    private int submittedApiKeyId;
    private String username;
    private Map<String,Object> metadata;
    private String createdAt;
    private String updatedAt;
}
