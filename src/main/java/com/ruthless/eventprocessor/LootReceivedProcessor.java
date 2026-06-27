package com.ruthless.eventprocessor;

import com.ruthless.event.ClanWhitelistReceivedEvent;
import com.ruthless.web.RuthlessClient;
import com.ruthless.web.request.RuthlessMemberLootItem;
import com.ruthless.web.request.RuthlessMemberLootRequest;
import com.ruthless.web.response.ClanWhitelist;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStack;
import net.runelite.client.plugins.loottracker.LootReceived;
import net.runelite.client.task.Schedule;

import javax.inject.Inject;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class LootReceivedProcessor {

    private final Set<String> whitelistedSources = new HashSet<>();;
    private final Set<Integer> whitelistedItems = new HashSet<>();;

    private @Inject RuthlessClient ruthlessClient;
    private @Inject Client client;
    private @Inject ItemManager itemManager;

    @Subscribe
    public void onLootReceived(LootReceived lootReceived) {
        if (whitelistedSources.isEmpty()|| whitelistedItems.isEmpty()) {
            log.debug("Clan whitelist isn't set, drop the message for now.");
            ruthlessClient.getClanWhitelist();
            return;
        }
        if (validLoot(lootReceived)) {
            Player local = client.getLocalPlayer();
            RuthlessMemberLootRequest request = RuthlessMemberLootRequest.builder()
                    .items(lootReceived.getItems().stream().map(item -> new RuthlessMemberLootItem(item.getId(), item.getQuantity(), itemManager.getItemComposition(item.getId()).getName())).collect(Collectors.toCollection(ArrayList::new)))
                    .sourceName(lootReceived.getName())
                    .world(client.getWorld())
                    .groupSize(1)
                    .players(local.getName())
                    .addedBy(local.getName())
                    .build();
            ruthlessClient.submitLoot(request);
        }
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
