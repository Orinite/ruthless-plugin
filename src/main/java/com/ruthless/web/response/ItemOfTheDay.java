package com.ruthless.web.response;

import lombok.Getter;

@Getter
public class ItemOfTheDay {
    private int id;
    private String discordGuildId;
    private int itemId;
    private String itemName;
    private int tier;
    private String rewardValue;
    private String rewardType;
    private String keyword;
    private long dateAdded;
    private long expirationTimestamp;
    private boolean claimed;
    private String addedBy;
}
