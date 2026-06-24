package com.ruthless.web.response;

import lombok.Getter;

import javax.annotation.Nullable;
import java.util.List;

@Getter
public class RuthlessSlayerTaskInfo {
    private int memberId;
    private String username;
    private String discordGuildId;
    private String discordUserId;
    private int slayerPoints;
    private long slayerRerollRenewAt;
    private int slayerRerollAvailable;
    private List<SlayerSetting> settings;
    private List<Object> qualifyingItems;
    private @Nullable RuthlessSlayerTask currentTask;
}