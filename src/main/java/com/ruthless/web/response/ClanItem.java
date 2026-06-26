package com.ruthless.web.response;

import lombok.Getter;

@Getter
public class ClanItem {
    private int id;
    private String name;
    private boolean splittable;
    private int wikiItemId;
    private long price;
    private boolean liveLoots;
    private boolean validSlayer;
    private boolean validBounty;
    private long highPrice;
    private long lowPrice;
    private String dateUpdated;
}
