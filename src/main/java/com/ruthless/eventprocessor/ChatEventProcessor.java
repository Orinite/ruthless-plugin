package com.ruthless.eventprocessor;

import com.ruthless.web.RuthlessClient;
import com.ruthless.web.request.RuthlessMemberBossTimeRequest;
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
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ChatEventProcessor {

    //These Patterns were grabbed from Runelite's ChatCommandsPlugin.java
    private static final Pattern KILLCOUNT_PATTERN = Pattern.compile("Your (?<pre>completion count for |subdued |completed )?(?:<col=[0-9a-f]{6}>)?(?<boss>.+?)(?:</col>)? (?<post>(?:(?:kill|harvest|lap|completion|success) )?(?:count )?)is: ?<col=[0-9a-f]{6}>(?<kc>[0-9,]+)</col>");
    private static final Pattern KILL_DURATION_PATTERN = Pattern.compile("(?i)(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:) <col=[0-9a-f]{6}>(?<time>[0-9:.]+)</col>\\. Personal best: (?:<col=ff0000>)?(?<pb>[0-9:]+(?:\\.[0-9]+)?)");
    private static final Pattern NEW_PB_PATTERN = Pattern.compile("(?i)(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:) <col=[0-9a-f]{6}>(?<pb>[0-9:]+(?:\\.[0-9]+)?)</col> \\(new personal best\\)");
    private static final long FIVE_SECONDS_MILLIS = 5000L;

    @Setter
    private String lastBoss = null;
    @Setter
    private int lastKc = -1;
    @Setter
    private double lastTiming = -1;
    @Setter
    private Instant lastUpdateKc;
    @Setter
    private boolean isNewPb = false;
    @Setter
    private double lastPb = -1;

    private @Inject RuthlessClient ruthlessClient;
    private @Inject Client client;

    public void onChatMessage( ChatMessage chatMessage ) {

        String message = chatMessage.getMessage();
        Matcher matcher = KILLCOUNT_PATTERN.matcher(message);
        if (matcher.find())
        {
            final String boss = matcher.group("boss");
            final int kc = Integer.parseInt(matcher.group("kc").replace(",",""));
            setLastBoss(boss);
            setLastKc(kc);
            setLastUpdateKc(Instant.now());
        }

        matcher = KILL_DURATION_PATTERN.matcher(message);
        if(matcher.find()) {
            setNewPb(false);
            setLastTiming(timeStringToSeconds(matcher.group("time")));
            setLastPb(timeStringToSeconds(matcher.group("pb")));
        }
        matcher = NEW_PB_PATTERN.matcher(message);
        if(matcher.find()) {
            setNewPb(true);
            setLastTiming(timeStringToSeconds(matcher.group("pb")));
            setLastPb(timeStringToSeconds(matcher.group("pb")));
        }

        if (!Strings.isNullOrEmpty(lastBoss) && lastKc > 0 && lastTiming > 0.0 && validateTiming()) {
            log.debug("new time event emit. Boss: {}, kc: {}, time: {}, pb: {}", lastBoss, lastKc, lastTiming, lastPb);
            Player local = client.getLocalPlayer();
            if( local != null ){
                ruthlessClient.submitBossTimeRequest(
                        new RuthlessMemberBossTimeRequest(
                                UUID.randomUUID().toString(),
                                lastBoss,
                                String.valueOf(lastTiming),
                                String.valueOf(lastPb),
                                lastKc,
                                client.getWorld(),
                                1,
                                local.getName(),
                                local.getName()
                        )
                );
            }
            setLastBoss(null);
            setLastKc(-1);
            setLastTiming(-1);
            setNewPb(false);
        }

    }

    public boolean validateTiming() {
        return Instant.now().toEpochMilli() - lastUpdateKc.toEpochMilli() < FIVE_SECONDS_MILLIS;
    }

    static double timeStringToSeconds(String timeString)
    {
        String[] s = timeString.split(":");
        if (s.length == 2) // mm:ss
        {
            return Integer.parseInt(s[0]) * 60 + Double.parseDouble(s[1]);
        }
        else if (s.length == 3) // h:mm:ss
        {
            return Integer.parseInt(s[0]) * 60 * 60 + Integer.parseInt(s[1]) * 60 + Double.parseDouble(s[2]);
        }
        return Double.parseDouble(timeString);
    }

}
