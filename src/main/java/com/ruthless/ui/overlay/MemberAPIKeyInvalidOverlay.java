package com.ruthless.ui.overlay;

import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import com.ruthless.event.MemberAPIKeyInvalidEvent;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class MemberAPIKeyInvalidOverlay extends OverlayPanel {

    private final Client client;
    private final RuthlessPlugin plugin;
    private final RuthlessConfig config;

    @Inject
    private MemberAPIKeyInvalidOverlay(Client client, RuthlessPlugin plugin, RuthlessConfig config) {
        super(plugin);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setPreferredSize(new Dimension(180, 75));
        setPreferredColor(Color.RED);
        this.client = client;
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {

        panelComponent.getChildren().add(TitleComponent.builder().text("Ruthless API Key Invalid").color(Color.RED).build());

        return super.render(graphics);
    }
}
