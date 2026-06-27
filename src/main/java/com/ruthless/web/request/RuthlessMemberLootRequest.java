package com.ruthless.web.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
public class RuthlessMemberLootRequest {

    private List<RuthlessMemberLootItem> items;
    private String sourceName;
    private int world;
    private int groupSize;
    private String players;
    private String addedBy;

}
