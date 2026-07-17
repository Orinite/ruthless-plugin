package com.ruthless.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.annotations.VarCStr;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

import javax.annotation.Nullable;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.client.util.Text.removeTags;
import static net.runelite.client.util.Text.sanitize;

@UtilityClass
@Slf4j
public class RaidUtils {

    public final @VarCStr int TOA_MEMBER_NAME = 1099, TOB_MEMBER_NAME = 330;
    private final int TOA_PARTY_MAX_SIZE = 8, TOB_PARTY_MAX_SIZE = 5;

    public Collection<String> getBossParty(Client client, String source) {
        switch (source) {
            case "Chambers of Xeric":
            case "Chambers of Xeric Challenge Mode":
            case "Royal Titans":
            case "Eldric the Ice King":
            case "Branda the Fire Queen":
            case "Yama":
            case "Nightmare":
            case "Nex":
                return getLocalPlayers(client);
            case "Tombs of Amascut":
            case "Tombs of Amascut: Entry Mode":
            case "Tombs of Amascut: Expert Mode":
                return getAmascutTombsParty(client);
            case "Theatre of Blood":
            case "Theatre of Blood: Entry Mode":
            case "Theatre of Blood: Hard Mode":
                return getBloodTheatreParty(client);
            default:
                return List.of(client.getLocalPlayer().getName());
        }
    }

    private Collection<String> getLocalPlayers(Client client) {
        return client
                .getWorldView(-1)
                .players()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    private List<String> getVarcStrings(Client client, @VarCStr final int initialVarcId, final int maxSize) {
        List<String> strings = new ArrayList<>(maxSize);
        for (int i = 0; i < maxSize; i++) {
            String name = client.getVarcStrValue(initialVarcId + i);
            if (name == null || name.isEmpty()) continue;
            strings.add(name.replace('\u00A0', ' '));
        }
        return strings;
    }

    public Collection<String> getAmascutTombsParty(Client client) {
        return getVarcStrings(client, TOA_MEMBER_NAME, TOA_PARTY_MAX_SIZE);
    }

    private Collection<String> getBloodTheatreParty(Client client) {
        return getVarcStrings(client, TOB_MEMBER_NAME, TOB_PARTY_MAX_SIZE);
    }
}
