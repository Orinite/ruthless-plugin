package com.ruthless.web.response.events;

import lombok.Data;

import java.util.List;

@Data
public class EventsResponse {
    private List<ClanEvent> items;
}
