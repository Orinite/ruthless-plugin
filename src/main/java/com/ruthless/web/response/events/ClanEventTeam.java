package com.ruthless.web.response.events;

import lombok.Data;

import java.util.List;

@Data
public class ClanEventTeam {
    private int id;
    private int eventId;
    private Integer captainId;
    private String captainLabel;
    private String name;
    private int vanityRankId;
    private String vanityRankDisplayName;
    private String vanityRankColor;
    private VanityRank vanityRank;
    private String colorOverride;
    private String effectiveColor;
    private List<ClanEventTeamMember> players;
    private String createdAt;
    private String updatedAt;
}
