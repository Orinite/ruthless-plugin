package com.ruthless.web.request;

import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Builder
public class LootDropSubmission {

    private String sourceName;
    private int world;
    private String username;
    private int killCount;
    private int groupSize;
    private String obtainedAt;
    private Map<String, Object> metadata;

}
