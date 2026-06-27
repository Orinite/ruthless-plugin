package com.ruthless.event;

import com.ruthless.web.response.ClanWhitelist;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ClanWhitelistReceivedEvent {

    @Getter
    private ClanWhitelist clanWhitelist;
}
