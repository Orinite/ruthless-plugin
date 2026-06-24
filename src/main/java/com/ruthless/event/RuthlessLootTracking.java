package com.ruthless.event;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.loottracker.LootReceived;

@Slf4j
public class RuthlessLootTracking {

    @Subscribe
    public void onLootReceived(LootReceived lootReceived) {
        log.debug(lootReceived.toString());
    }
}
