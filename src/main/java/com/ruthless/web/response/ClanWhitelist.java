package com.ruthless.web.response;

import lombok.Getter;

import java.util.List;

@Getter
public class ClanWhitelist {

    private List<ClanItem> items;
    private List<ClanSource> sources;
}
