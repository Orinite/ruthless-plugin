package com.ruthless.ui;

import com.ruthless.RuthlessPlugin;
import com.ruthless.web.response.RuthlessSlayerTaskInfo;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class RuthlessSlayerTaskInfoBox extends InfoBox {

    private RuthlessSlayerTaskInfo ruthlessSlayerTaskInfo;
    public RuthlessSlayerTaskInfoBox(RuthlessSlayerTaskInfo ruthlessSlayerTaskInfo, @Nonnull Plugin plugin) {
        super(ImageUtil.loadImageResource(RuthlessPlugin.class, "imgs/ruthless.png"), plugin);
        this.ruthlessSlayerTaskInfo = ruthlessSlayerTaskInfo;
    }

    @Override
    public String getTooltip()
    {
        StringBuilder sb = new StringBuilder();
        if (ruthlessSlayerTaskInfo.getCurrentTask() == null) {
            sb.append("Go get a Slayer task from Ruth!");
        }
        sb.append("Current Slayer Task: " + ruthlessSlayerTaskInfo.getCurrentTask().getMonsterName()).append("</br>")
                .append("Time left: " + getTimeLeft());
        return sb.toString();
    }

    private String getTimeLeft() {
        Duration timeLeft;
        if (ruthlessSlayerTaskInfo == null || ruthlessSlayerTaskInfo.getCurrentTask() == null) {
            return "None";
        } else {
            timeLeft = Duration.between(Instant.now(), new Date(ruthlessSlayerTaskInfo.getCurrentTask().getExpiresAt()*1000).toInstant());
        }

        return DurationFormatUtils.formatDuration(timeLeft.toMillis(), "H'h'm'm's's'");
    }
    @Override
    public String getText() {

        return getTimeLeft();
    }

    @Override
    public Color getTextColor() {
        return new Color(0x51f542);
    }
}
