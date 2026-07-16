package com.ruthless.eventprocessor;

import com.ruthless.utils.RaidUtils;
import com.ruthless.web.RuthlessClient;
import com.ruthless.web.request.BossKillSubmission;
import com.ruthless.web.request.DonationSubmission;
import joptsimple.internal.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Map.entry;

@Slf4j
public class DonationChatEventProcessor {

    private static final Pattern DONATION_PATTERN = Pattern.compile("(?<person>.+?) has deposited (?<amount>[0-9,]+) coins into the coffer.");


    private @Inject RuthlessClient ruthlessClient;
    private @Inject Client client;

    @Subscribe
    public void onChatMessage( ChatMessage chatMessage ) {

        if (chatMessage.getType() != ChatMessageType.CLAN_MESSAGE) {
            return;
        }

        String message = chatMessage.getMessage();

        Matcher matcher = DONATION_PATTERN.matcher(message);
        if (matcher.find()) {
            log.debug("donation pattern found");
            String person = matcher.group("person");
            long amount = Long.parseLong(matcher.group("amount").replaceAll(",",""));
            log.debug("Amount: {}", amount);
            log.debug("Person donating: {}", person);
            if (client.getLocalPlayer() != null && person.equalsIgnoreCase(client.getLocalPlayer().getName())) {
                //send event
                ruthlessClient.submitDonation(DonationSubmission.builder().value(amount).donatedAt(Instant.now().toString()).build());
            }
        }
    }

}
