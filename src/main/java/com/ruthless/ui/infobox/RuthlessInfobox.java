package com.ruthless.ui.infobox;

import com.ruthless.RuthlessPlugin;
import lombok.Setter;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.*;

public class RuthlessInfobox extends InfoBox {

    @Inject
    public RuthlessInfobox(RuthlessPlugin plugin) {
        super(ImageUtil.loadImageResource(RuthlessPlugin.class, "imgs/ruthless_v2.png"), plugin);
    }

    @Override
    public String getTooltip()
    {
        return "TOTW Info will go here";
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
