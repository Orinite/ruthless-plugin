package com.ruthless.web.response.events;

import lombok.Data;

@Data
public class ClanEventTeamMember {
    private int id;
    private int eventTeamId;
    private int clanMemberId;
    private String clanMemberLabel;
    private String primaryUsername;
    private String createdAt;
    private String updatedAt;
}
