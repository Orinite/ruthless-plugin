package com.ruthless.web.response.events;

import lombok.Data;

import java.util.List;

@Data
public class ClanEvent {
    private int id;
    private int clanId;
    private Integer eventTemplateId;
    private String eventTemplateName;
    private String startsAt;
    private String endsAt;
    private List<ClanEventTeam> teams;
}
