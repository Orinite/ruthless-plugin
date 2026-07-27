package com.ruthless.eventprocessor;

import com.ruthless.RuthlessConfig;
import com.ruthless.event.ClanWebhooksReceivedEvent;
import com.ruthless.event.ClanWhitelistReceivedEvent;
import com.ruthless.utils.ConfigUtils;
import com.ruthless.utils.RaidUtils;
import com.ruthless.utils.ScreenshotUtils;
import com.ruthless.web.RuthlessClient;
import com.ruthless.web.request.RuthlessMemberLootItem;
import com.ruthless.web.request.LootDropSubmission;
import com.ruthless.web.request.discord.DiscordWebhookBody;
import com.ruthless.web.response.ClanWhitelist;
import com.ruthless.web.response.webhooks.ClanWebhook;
import com.ruthless.web.response.webhooks.ClanWebhooks;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.chatcommands.ChatCommandsPlugin;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.ui.DrawManager;
import net.runelite.client.util.ImageUtil;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static net.runelite.http.api.RuneLiteAPI.GSON;

@Slf4j
public class LootReceivedProcessor {

    private final Set<String> whitelistedSources = new HashSet<>();;
    private final Set<Integer> whitelistedItems = new HashSet<>();;

    private @Inject RuthlessClient ruthlessClient;
    private @Inject RuthlessConfig config;
    private @Inject Client client;
    private @Inject ItemManager itemManager;
    private @Inject ConfigManager configManager;
    private @Inject DrawManager drawManager;
    private @Inject ClientThread clientThread;
    private @Inject OkHttpClient okHttpClient;

    private static final String RL_CHAT_CMD_PLUGIN_NAME = ChatCommandsPlugin.class.getSimpleName().toLowerCase();
    private String lootChannel;


    @Subscribe
    public void onClanWebhooksReceivedEvent(ClanWebhooksReceivedEvent event) {
        this.lootChannel = null; //clear this in case we disable
        Optional<ClanWebhook> lootDiscordChannel = event.getClanWebhooks().getItems()
                .stream()
                .filter(webhook -> webhook.getWebhookTypeName().equals(ClanWebhooks.LOOTS_WEBHOOK_TYPE_NAME))
                .findFirst();
        if (lootDiscordChannel.isPresent()) {
            log.debug("Setting loot channel");
            this.lootChannel = lootDiscordChannel.get().getUrl();
        }
    }

    @Subscribe
    public void onLootReceived(LootReceived lootReceived) {
        if (whitelistedSources.isEmpty()|| whitelistedItems.isEmpty()) {
            log.debug("Clan whitelist isn't set, drop the message for now.");
            ruthlessClient.getClanWhitelist();
            return;
        }
        boolean isValidLoot = validLoot(lootReceived);
        boolean isValidSource = validSource(lootReceived);
        if (isValidLoot || isValidSource) {
            Player local = client.getLocalPlayer();
            Map<String, Object> metadata = new HashMap<>();
            List<RuthlessMemberLootItem> items = lootReceived.getItems().stream()
                    .map(item -> new RuthlessMemberLootItem(item.getId(), item.getQuantity(), itemManager.getItemComposition(item.getId()).getName()))
                    .collect(Collectors.toCollection(ArrayList::new));
            metadata.put("items", items);
            Collection<String> groupMembers = RaidUtils.getBossParty(client, lootReceived.getName());
            metadata.put("players", groupMembers);
            LootDropSubmission request = LootDropSubmission.builder()
                    .sourceName(lootReceived.getName())
                    .world(client.getWorld())
                    .killCount(getKillcount(lootReceived.getName()))
                    .username(local.getName())
                    .groupSize(groupMembers.size())
                    .metadata(metadata)
                    .build();
            ruthlessClient.submitLoot(request);

            //only send webhook to discord if loot is good.
            if( isValidLoot ) {
                submitScreenshotToLootChannel(items);
            }

        }
    }

    private int getKillcount(String sourceName) {
        // get kc from base runelite chat commands plugin (if enabled)
        if (!ConfigUtils.isPluginDisabled(configManager, RL_CHAT_CMD_PLUGIN_NAME)) {
            Integer kc = configManager.getRSProfileConfiguration("killcount", cleanBossName(sourceName), int.class);
            if (kc != null) {
                return kc;
            }
        }
        return 0;
    }

    /**
     * @param boss {@link LootReceived#getName()}
     * @return lowercase boss name that {@link ChatCommandsPlugin} uses during serialization
     */
    private static String cleanBossName(String boss) {
        if ("The Gauntlet".equalsIgnoreCase(boss) || "Crystalline Hunllef".equals(boss)) return "gauntlet";
        if ("Corrupted Hunllef".equals(boss)) return "corrupted gauntlet";
        if ("The Leviathan".equalsIgnoreCase(boss)) return "leviathan";
        if ("The Whisperer".equalsIgnoreCase(boss)) return "whisperer";
        if ("The Hueycoatl".equalsIgnoreCase(boss)) return "hueycoatl";
        if (boss.startsWith("Barrows")) return "barrows chests";
        if (boss.endsWith("Tempoross)")) return "tempoross";
        if (boss.endsWith("Wintertodt)")) return "wintertodt";
        return StringUtils.remove(boss.toLowerCase(), ':');
    }


    @Subscribe
    public void onClanWhitelistReceivedEvent(ClanWhitelistReceivedEvent clanWhitelistReceivedEvent ) {
        ClanWhitelist whitelist = clanWhitelistReceivedEvent.getClanWhitelist();

        //reset
        whitelistedSources.clear();
        whitelistedItems.clear();

        whitelist.getItems().forEach(item -> whitelistedItems.add(item.getWikiItemId()));
        whitelist.getSources().forEach(source -> whitelistedSources.add(source.getName()));
    }

    private boolean validSource(LootReceived lootReceived) {
        String sourceName = lootReceived.getName();
        return whitelistedSources.contains(sourceName);
    }

    private boolean validLoot(LootReceived lootReceived) {
        for (ItemStack loot : lootReceived.getItems()) {
            if (whitelistedItems.contains(loot.getId())) {
                return true;
            }
        }
        return false;
    }

    private void submitScreenshotToLootChannel(List<RuthlessMemberLootItem> items) {
        if (lootChannel != null) {
            ScreenshotUtils.getImage(drawManager, client, clientThread, (image) -> this.sendPayload(image, items));
        }
    }

    private void sendPayload(Image image, List<RuthlessMemberLootItem> items) {
        BufferedImage img = ImageUtil.bufferedImageFromImage(image);
        byte[] imageBytes;
        try {
            imageBytes = ScreenshotUtils.convertImageToByteArray(img);
        } catch(IOException e) {
            log.warn("Error converting image to a byte array for sending");
            return;
        }
        Player local = client.getLocalPlayer();
        DiscordWebhookBody body = DiscordWebhookBody.builder().content(local.getName() + " has received items: " + String.join(", ", items.stream().map(RuthlessMemberLootItem::getName).collect(Collectors.toList()))).build();
        MultipartBody requestBodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("payload_json", GSON.toJson(body))
                .addFormDataPart("file", "image.png", RequestBody.create(MediaType.parse("image/png"), imageBytes)).build();

        List<String> urls = new ArrayList<>();
        urls.add(lootChannel);
        if (!config.customLootWebhooks().isBlank()) {
            urls.addAll(Arrays.stream(config.customLootWebhooks().split(",")).collect(Collectors.toList()));
        }
        for ( String url : urls ) {
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
