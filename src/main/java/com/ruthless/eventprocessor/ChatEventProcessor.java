package com.ruthless.eventprocessor;

import joptsimple.internal.Strings;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.events.ChatMessage;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.chatcommands.ChatCommandsPlugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ChatEventProcessor {

    //These Patterns were grabbed from Runelite's ChatCommandsPlugin.java
    private static final Pattern KILLCOUNT_PATTERN = Pattern.compile("Your (?<pre>completion count for |subdued |completed )?(?:<col=[0-9a-f]{6}>)?(?<boss>.+?)(?:</col>)? (?<post>(?:(?:kill|harvest|lap|completion|success) )?(?:count )?)is: ?<col=[0-9a-f]{6}>(?<kc>[0-9,]+)</col>");
    private static final Pattern KILL_DURATION_PATTERN = Pattern.compile("(?i)(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:) <col=[0-9a-f]{6}>[0-9:.]+</col>\\. Personal best: (?:<col=ff0000>)?(?<pb>[0-9:]+(?:\\.[0-9]+)?)");
    private static final Pattern NEW_PB_PATTERN = Pattern.compile("(?i)(?:(?:Fight |Lap |Challenge |Corrupted challenge )?duration:|Subdued in|(?<!total )completion time:) <col=[0-9a-f]{6}>(?<pb>[0-9:]+(?:\\.[0-9]+)?)</col> \\(new personal best\\)");

    @Setter
    private String lastBoss = null;
    @Setter
    private int lastKc = -1;
    @Setter
    private double lastTiming = -1;

    @Subscribe
    public void onChatMessage( ChatMessage chatMessage ) {
        if (chatMessage.getType() != ChatMessageType.GAMEMESSAGE) {
            return;
        }

        String message = chatMessage.getMessage();
        Matcher matcher = KILLCOUNT_PATTERN.matcher(message);
        if (matcher.find())
        {
            final String boss = matcher.group("boss");
            final int kc = Integer.parseInt(matcher.group("kc").replace(",",""));
            setLastBoss(boss);
            setLastKc(kc);
        }

        matcher = KILL_DURATION_PATTERN.matcher(message);
        if(matcher.find()) {
            lastTiming = timeStringToSeconds(matcher.group("pb"));
        }
        matcher = NEW_PB_PATTERN.matcher(message);
        if(matcher.find()) {
            lastTiming = timeStringToSeconds(matcher.group("pb"));
        }

        if (!Strings.isNullOrEmpty(lastBoss) && lastKc > 0 && lastTiming > 0.0) {
            log.debug("new kc event emit. Boss: {}, kc: {}, pb: {}", lastBoss, lastKc, lastTiming);
            lastBoss = null;
            lastKc = -1;
            lastTiming = -1;
        }

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
