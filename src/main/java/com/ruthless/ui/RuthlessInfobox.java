package com.ruthless.ui;

import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import com.ruthless.web.response.ItemOfTheDay;
import com.ruthless.web.response.RuthlessSlayerTaskInfo;
import lombok.Setter;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.inject.Inject;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class RuthlessInfobox extends InfoBox {

    @Setter
    private ItemOfTheDay itemOfTheDay;
    @Setter
    private RuthlessSlayerTaskInfo ruthlessSlayerTaskInfo;

    private @Inject RuthlessConfig config;

    public RuthlessInfobox(RuthlessPlugin plugin) {
        super(ImageUtil.loadImageResource(RuthlessPlugin.class, "imgs/ruthless.png"), plugin);
    }

    @Override
    public String getTooltip()
    {
        StringBuilder sb = new StringBuilder();
        if (itemOfTheDay != null && config.showIotdInInfobox()) {
            sb.append("Item of the Day: " + itemOfTheDay.getItemName()).append("</br>")
                    .append("Time left: " + getTimeLeft(itemOfTheDay.getExpirationTimestamp()));
        }


        return sb.toString();
    }

    private String getTimeLeft(long expirationInSeconds) {
        Duration timeLeft;
        if (itemOfTheDay == null) {
            timeLeft = Duration.ZERO;
        } else {
            timeLeft = Duration.between(Instant.now(), new Date(itemOfTheDay.getExpirationTimestamp()*1000).toInstant());
        }

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
