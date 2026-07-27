package com.ruthless.utils;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.annotations.Component;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.ui.DrawManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

@UtilityClass
@Slf4j
public class ScreenshotUtils {


    public void getImage(DrawManager drawManager, Client client, ClientThread clientThread, Consumer<Image> consumer) {
        boolean chatHidden = hideWidget(client, InterfaceID.Chatbox.CHATAREA);
        boolean whispersHidden = hideWidget(client, InterfaceID.PmChat.CONTAINER);
        drawManager.requestNextFrameListener(image -> {
            consumer.accept(image);

            unhideWidget(chatHidden, client, clientThread,InterfaceID.Chatbox.CHATAREA);
            unhideWidget(whispersHidden, client, clientThread, InterfaceID.PmChat.CONTAINER);
        });
    }

    public byte[] convertImageToByteArray(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public boolean hideWidget(Client client, @Component int info) {

        Widget widget = client.getWidget(info);
        if (widget == null || widget.isHidden())
            return false;

        widget.setHidden(true);
        return true;
    }

    public void unhideWidget(boolean shouldUnhide, Client client, ClientThread clientThread, @Component int info) {
        if (!shouldUnhide)
            return;

        clientThread.invoke(() -> {
            Widget widget = client.getWidget(info);
            if (widget != null)
                widget.setHidden(false);
        });
    }

}
