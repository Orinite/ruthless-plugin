package com.ruthless.web.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Map;

@Builder
@AllArgsConstructor
public class BossKillSubmission {
    private String sourceName;
    private double killTimeSeconds;
    private String username;
    private int killCount;
    private double personalBestTimeSeconds;
    private int world;
    private int groupSize;
    private Map<String, Object> metadata;
}
