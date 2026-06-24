package com.ruthless.event;

import com.ruthless.web.response.ClanBroadcast;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ClanBroadcastEvent {
    private ClanBroadcast clanBroadcast;
}
