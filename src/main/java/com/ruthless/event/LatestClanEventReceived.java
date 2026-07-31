package com.ruthless.event;

import com.ruthless.web.response.events.ClanEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LatestClanEventReceived {

    private ClanEvent clanEvent;
}
