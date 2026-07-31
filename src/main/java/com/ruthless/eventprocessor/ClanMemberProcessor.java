package com.ruthless.eventprocessor;

import com.ruthless.RuthlessConfig;
import com.ruthless.api.VanityRankColorSetting;
import com.ruthless.api.VanityRankIconSetting;
import com.ruthless.event.LatestClanEventReceived;
import com.ruthless.web.response.events.ClanEventTeam;
import com.ruthless.web.response.events.ClanEventTeamMember;
import com.ruthless.web.response.events.VanityRank;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.clan.ClanMember;
import net.runelite.api.clan.ClanTitle;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ScriptPostFired;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.gameval.SpriteID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ChatIconManager;
import net.runelite.client.util.Text;

import javax.inject.Inject;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class ClanMemberProcessor {

    private @Inject Client client;
    private @Inject ClientThread clientThread;
    private @Inject ChatIconManager chatIconManager;
    private @Inject RuthlessConfig config;
    private static final Pattern RECEIVED_DROP_PATTERN = Pattern.compile("(?<player>\\w+) received a drop:");
    private static final int CLAN_ICON_IMG_OFFSET = 2991;

    private Map<String, VanityRank> usernameToVanityRank = new HashMap<>();

    @Subscribe
    public void onChatMessage( ChatMessage chatMessage ) {
        if (Objects.requireNonNull(chatMessage.getType()) == ChatMessageType.CLAN_CHAT) {
            handleClanMessage(chatMessage);
        }

    }

    @Subscribe
    public void onLatestClanEventReceived(LatestClanEventReceived event) {
        log.debug("Received new clan event. setting up map.");
        usernameToVanityRank.clear();
        for( ClanEventTeam team : event.getClanEvent().getTeams() ) {
            for (ClanEventTeamMember player : team.getPlayers() ) {
                usernameToVanityRank.put(player.getPrimaryUsername().toLowerCase(), team.getVanityRank());
            }
        }
    }

    private void handleClanMessage(ChatMessage chatMessage) {
        final MessageNode messageNode = chatMessage.getMessageNode();

        String username = Text.sanitize(messageNode.getName());
        VanityRank rank = usernameToVanityRank.get(username.toLowerCase());
        if (rank == null) {
            return;
        }
        if( config.vanityRankSetting() != VanityRankIconSetting.NEVER ) {
            String name = messageNode.getName();
            String icon = "<img="+(Integer.valueOf(rank.getLookupValue())-CLAN_ICON_IMG_OFFSET)+">";
            if( config.vanityRankSetting() == VanityRankIconSetting.BEFORE_USERNAME ) {
                name = icon + name;
            } else {
                name = name + icon;
            }
            if( config.vanityRankColorSetting() == VanityRankColorSetting.ALL || config.vanityRankColorSetting() == VanityRankColorSetting.CHATBOX){
                name = "<col="+rank.getColor().replaceAll("#","")+">" + name + "</col>";
            }
            messageNode.setName(name);
        }

        log.debug("Received cc message. name: {}", messageNode.getName());
    }

    @Subscribe
    public void onScriptPostFired(ScriptPostFired event) {
        if (event.getScriptId() != ScriptID.CLAN_SIDEPANEL_DRAW) {
            return;
        }
        clientThread.invokeLater(this::recolorNames);
    }

    private void recolorNames() {
        if( config.vanityRankColorSetting() == VanityRankColorSetting.NEVER && config.vanityRankSetting() == VanityRankIconSetting.NEVER) {
            return;
        }

        Widget parent = client.getWidget(InterfaceID.ClansSidepanel.PLAYERLIST);
        if (parent == null) {
            return;
        }

        Widget[] children = parent.getDynamicChildren();
        if(children == null || children.length == 0) {
            return;
        }
        for( int i = 0; i < children.length; i++) {
            Widget member = children[i];
            if(member.getText().isEmpty()) {
                continue;
            }
            if (usernameToVanityRank.containsKey(member.getText().toLowerCase())) {
                VanityRank rank = usernameToVanityRank.get(member.getText().toLowerCase());

                String text = member.getText();
                String icon = "<img="+(Integer.valueOf(rank.getLookupValue())-CLAN_ICON_IMG_OFFSET)+">";
                if( config.vanityRankSetting() == VanityRankIconSetting.BEFORE_USERNAME ) {
                    text = icon + text;
                } else if (config.vanityRankSetting() == VanityRankIconSetting.AFTER_USERNAME) {
                    text = text + icon;
                }

                if( config.vanityRankColorSetting() == VanityRankColorSetting.ALL || config.vanityRankColorSetting() == VanityRankColorSetting.CLAN_PANEL){
                    member.setTextColor(Color.decode(rank.getColor()).getRGB());
                }

                member.setText(text);

            }

        }
    }
}
