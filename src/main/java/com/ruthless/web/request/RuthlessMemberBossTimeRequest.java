package com.ruthless.web.request;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class RuthlessMemberBossTimeRequest {
    private String guid;
    private String sourceName;
    private String time;
    private String personalBest;
    private int killcount;
    private int world;
    private int groupSize;
    private String players;
    private String addedBy;
}
