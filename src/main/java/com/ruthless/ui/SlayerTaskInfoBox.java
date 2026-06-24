package com.ruthless.ui;

import com.ruthless.RuthlessPlugin;
import com.ruthless.web.response.ItemOfTheDay;
import com.ruthless.web.response.RuthlessSlayerTask;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ColorUtil;
import net.runelite.client.util.ImageUtil;
import org.apache.commons.lang3.time.DurationFormatUtils;

import javax.annotation.Nonnull;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

public class SlayerTaskInfoBox extends InfoBox {
    public SlayerTaskInfoBox(BufferedImage image, @Nonnull Plugin plugin) {
        super(image, plugin);
    }

    @Override
    public String getText() {
        return "";
    }

    @Override
    public Color getTextColor() {
        return null;
    }

//    private RuthlessSlayerTask slayerTask;
//
//    public SlayerTaskInfoBox(RuthlessSlayerTask slayerTask, RuthlessPlugin plugin) {
//        super(ImageUtil.loadImageResource(RuthlessPlugin.class, "imgs/ruthless.png"), plugin);
//        this.slayerTask = slayerTask;
//    }
//
//    @Override
//    public String getTooltip()
//    {
//        StringBuilder sb = new StringBuilder();
//        sb.append("Item of the Day: " + slayerTask.getItemName()).append("</br>")
//                .append("Time left: " + getTimeLeft());
//        return sb.toString();
//    }
//
//    private String getTimeLeft() {
//        Duration timeLeft;
//        if (itemOfTheDay == null) {
//            timeLeft = Duration.ZERO;
//        } else {
//            timeLeft = Duration.between(Instant.now(), new Date(itemOfTheDay.getExpirationTimestamp()*1000).toInstant());
//        }
//
//        return DurationFormatUtils.formatDuration(timeLeft.toMillis(), "H'h'm'm");
//    }
//    @Override
//    public String getText() {
//
//        return getTimeLeft();
//    }
//
//    @Override
//    public Color getTextColor() {
//        return new Color(0x51f542);
//    }
}
