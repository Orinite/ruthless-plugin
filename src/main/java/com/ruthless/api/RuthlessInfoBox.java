package com.ruthless.api;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;

public abstract class RuthlessInfoBox extends InfoBox {
    public RuthlessInfoBox(BufferedImage image, @Nonnull Plugin plugin) {
        super(image, plugin);
    }
}
