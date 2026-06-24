package com.ruthless.web.response;

import lombok.Getter;

import java.util.List;

@Getter
public class RuthlessSlayerTask {
    private int taskId;
    private int slayerMasterMonsterId;
    private int slayerMasterId;
    private String slayerMasterName;
    private int pointsWorth;
    private long createdAt;
    private long expiresAt;
    private boolean expired;
    private boolean cancelled;
    private boolean skipped;
    private boolean completed;
    private boolean rerolled;
    private boolean stored;
    private int storedDurationRemainingSeconds;
    private int weight;
    private boolean wildy;
    private boolean taskOnly;
    private String monsterName;
    private List<Object> sources;
}
