package com.ruthless.eventprocessor;

import com.ruthless.RuthlessConfig;
import com.ruthless.event.ClanWebhooksReceivedEvent;
import com.ruthless.utils.ScreenshotUtils;
import com.ruthless.web.request.discord.DiscordWebhookBody;
import com.ruthless.web.response.webhooks.ClanWebhook;
import com.ruthless.web.response.webhooks.ClanWebhooks;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.api.events.ActorDeath;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;
import okhttp3.*;

import javax.inject.Inject;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
public class PlayerDeathProcessor {

    private @Inject Client client;
    private @Inject RuthlessConfig config;
    private @Inject DrawManager drawManager;
    private @Inject ClientThread clientThread;
    private @Inject OkHttpClient okHttpClient;

    private String deathChannel;

    @Subscribe
    public void onClanWebhooksReceivedEvent(ClanWebhooksReceivedEvent event) {
        this.deathChannel = null; //clear in case we disable
        Optional<ClanWebhook> deathDiscordChannel = event.getClanWebhooks()
                .getItems()
                .stream()
                .filter(webhook -> webhook.getWebhookTypeName().equals(ClanWebhooks.DEATHS_WEBHHOOK_TYPE_NAME))
                .findFirst();
        if (deathDiscordChannel.isPresent()) {
            log.debug("Setting death channel");
            deathChannel = deathDiscordChannel.get().getUrl();
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath actorDeath) {
        if (client.getLocalPlayer() != actorDeath.getActor() || deathChannel == null || !config.sendDeaths()) {
            return;
        }

        ScreenshotUtils.getImage(drawManager, client, clientThread, this::sendPayload);
    }

    private void sendPayload(Image image) {
        BufferedImage img = ImageUtil.bufferedImageFromImage(image);
        byte[] imageBytes;
        try {
            imageBytes = ScreenshotUtils.convertImageToByteArray(img);
        } catch(IOException e) {
            log.warn("Error converting image to a byte array for sending");
            return;
        }
        Player local = client.getLocalPlayer();
        DiscordWebhookBody discordWebhookBody = DiscordWebhookBody.builder().content(config.deathMessage().replaceAll("\\$name", local.getName())).build();
        MultipartBody requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("payload_json", GSON.toJson(discordWebhookBody))
                .addFormDataPart("file", "image.png", RequestBody.create(MediaType.parse("image/png"), imageBytes)).build();
        List<String> urls = new ArrayList<>();
        urls.add(deathChannel);
        if (!config.customDeathWebhooks().isBlank()) {
            urls.addAll(Arrays.stream(config.customDeathWebhooks().split(",")).collect(Collectors.toList()));
        }
        for( String url : urls) {
            Request request = new Request.Builder().url(url).post(requestBodyBuilder).build();

            okHttpClient.newCall(request).enqueue(
                    new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            log.debug("Error submitting webhook", e);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            response.close();
                        }
                    }
            );
        }

    }
}
