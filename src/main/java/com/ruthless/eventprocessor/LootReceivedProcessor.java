package com.ruthless.eventprocessor;

import com.ruthless.event.ClanWhitelistReceivedEvent;
import com.ruthless.utils.ConfigUtils;
import com.ruthless.utils.RaidUtils;
import com.ruthless.web.RuthlessClient;
import com.ruthless.web.request.RuthlessMemberLootItem;
import com.ruthless.web.request.LootDropSubmission;
import com.ruthless.web.response.ClanWhitelist;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Player;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.chatcommands.ChatCommandsPlugin;
import net.runelite.client.plugins.loottracker.LootReceived;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LootReceivedProcessor {

    private final Set<String> whitelistedSources = new HashSet<>();;
    private final Set<Integer> whitelistedItems = new HashSet<>();;

    private @Inject RuthlessClient ruthlessClient;
    private @Inject Client client;
    private @Inject ItemManager itemManager;
    private @Inject ConfigManager configManager;

    private static final String RL_CHAT_CMD_PLUGIN_NAME = ChatCommandsPlugin.class.getSimpleName().toLowerCase();

    @Subscribe
    public void onLootReceived(LootReceived lootReceived) {
        if (whitelistedSources.isEmpty()|| whitelistedItems.isEmpty()) {
            log.debug("Clan whitelist isn't set, drop the message for now.");
            ruthlessClient.getClanWhitelist();
            return;
        }
        if (validLoot(lootReceived)) {
            Player local = client.getLocalPlayer();
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("items",
                    lootReceived.getItems().stream()
                            .map(item -> new RuthlessMemberLootItem(item.getId(), item.getQuantity(), itemManager.getItemComposition(item.getId()).getName()))
                            .collect(Collectors.toCollection(ArrayList::new))
            );
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
        }
    }

    private int getKillcount(String sourceName) {
        // get kc from base runelite chat commands plugin (if enabled)
        if (!ConfigUtils.isPluginDisabled(configManager, RL_CHAT_CMD_PLUGIN_NAME)) {
            Integer kc = configManager.getRSProfileConfiguration("killcount", cleanBossName(sourceName), int.class);
            if (kc != null) {
                return kc - 1; // decremented since chat event typically occurs before loot event
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

    private boolean validLoot(LootReceived lootReceived) {
        String sourceName = lootReceived.getName();
        if(whitelistedSources.contains(sourceName)) {
            return true;
        }
        for (ItemStack loot : lootReceived.getItems()) {
            if (whitelistedItems.contains(loot.getId())) {
                return true;
            }
        }
        return false;
    }

}
