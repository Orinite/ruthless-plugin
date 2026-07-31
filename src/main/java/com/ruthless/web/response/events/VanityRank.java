package com.ruthless.web.response.events;

import lombok.Data;

@Data
public class VanityRank {
    private int id;
    private String lookupValue;
    private String displayName;
    private String color;
    private String createdAt;
    private String updatedAt;

}
