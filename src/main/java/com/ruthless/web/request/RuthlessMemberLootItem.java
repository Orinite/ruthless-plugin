package com.ruthless.web.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class RuthlessMemberLootItem {

    private int wikiItemId;
    private long quantity;
    private String name;
}
