package com.ruthless.ui.overlay;

import com.ruthless.RuthlessConfig;
import com.ruthless.RuthlessPlugin;
import lombok.Setter;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Singleton
public class EventCodewordOverlay extends OverlayPanel {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm 'UTC'")
            .withZone(ZoneOffset.UTC);

    @Setter
    private String codeword;

    @Inject
    private EventCodewordOverlay(RuthlessPlugin ruthlessPlugin, RuthlessConfig config) {
        super(ruthlessPlugin);
        setPosition(OverlayPosition.ABOVE_CHATBOX_RIGHT);
        setPriority(PRIORITY_DEFAULT);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        if (codeword == null || codeword.isEmpty()) {
            return null; //dont render if codeword isnt set
        }
        panelComponent.getChildren().add(LineComponent.builder()
                .left(codeword)
                .leftColor(Color.ORANGE)
                .right(FORMATTER.format((Instant.now())))
                .rightColor(Color.WHITE)
                .build());
        return super.render(graphics);
    }
}
