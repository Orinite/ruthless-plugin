package com.ruthless.ui.infobox;

import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import com.ruthless.web.response.ItemOfTheDay;
import com.ruthless.web.response.RuthlessSlayerTaskInfo;
import lombok.Setter;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.inject.Inject;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class RuthlessInfobox extends InfoBox {

    @Setter
    private ItemOfTheDay itemOfTheDay;
    @Setter
    private RuthlessSlayerTaskInfo ruthlessSlayerTaskInfo;

    private RuthlessConfig config;

    @Inject
    public RuthlessInfobox(RuthlessPlugin plugin, RuthlessConfig config) {
        super(ImageUtil.loadImageResource(RuthlessPlugin.class, "imgs/ruthless_v2.png"), plugin);
        this.config = config;
    }

    @Override
    public String getTooltip()
    {
        StringBuilder sb = new StringBuilder();
        if (itemOfTheDay != null && config.showIotdInInfobox()) {
            sb.append("Item of the Day: " + itemOfTheDay.getItemName())
                    .append("</br>")
                    .append("Time left: " + getTimeLeft(itemOfTheDay.getExpirationTimestamp()))
                    .append("</br>")
                    .append("</br>");
        }
        if (ruthlessSlayerTaskInfo != null && config.showSlayertaskInInfobox()) {
            if (ruthlessSlayerTaskInfo.getCurrentTask() == null) {
                sb.append("Go get a slayer task from Ruth!");
            } else {
                sb.append("Slayer Task: " + ruthlessSlayerTaskInfo.getCurrentTask().getMonsterName())
                        .append("</br>")
                        .append("Slayer task time remaining: " + getTimeLeft(ruthlessSlayerTaskInfo.getCurrentTask().getExpiresAt()));
            }

        }


        return sb.toString();
    }

    private String getTimeLeft(long expirationInSeconds) {
        Duration timeLeft;

        timeLeft = Duration.between(Instant.now(), new Date(expirationInSeconds*1000).toInstant());

        return DurationFormatUtils.formatDuration(timeLeft.toMillis(), "H'h'm'm's's'");
    }
    @Override
    public String getText() {

        return "";
    }

    @Override
    public Color getTextColor() {
        return new Color(0x51f542);
    }
}
