package com.ruthless;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ruthless.ui.ItemOfTheDayInfoBox;
import com.ruthless.web.RuthlessClient;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@PluginDescriptor(
	name = "Ruthless Clan",
		tags = {"ruthless", "clan"},
		description = "Automates things for Ruthless clan."
)
public class RuthlessPlugin extends Plugin
{
	static final String CONFIG_GROUP = "ruthlessosrsclan";
	@Inject
	private Client client;

	@Inject
	private RuthlessConfig config;

	@Inject
	private RuthlessClient ruthlessClient;

	@Inject
	private InfoBoxManager infoBoxManager;

	private List<InfoBox> ruthlessInfobox = new ArrayList<>();

	@Override
	protected void startUp() throws Exception
	{
		if (config.showIotd()) {
			ruthlessClient.getItemOfTheDay();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		cleanupInfoBoxes();
	}

	@Provides
	RuthlessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuthlessConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if( !configChanged.getGroup().equals(RuthlessPlugin.CONFIG_GROUP) ) {
			return;
		}
		cleanupInfoBoxes();
		if( config.showIotd() ) {
			ruthlessClient.getItemOfTheDay();
		}
	}

	public void addIotdInfoBox(ItemOfTheDayInfoBox infoBox) {
		this.ruthlessInfobox.add(infoBox);
		infoBoxManager.addInfoBox(infoBox);
	}

	private void cleanupInfoBoxes() {
		infoBoxManager.removeIf(ItemOfTheDayInfoBox.class::isInstance);
		ruthlessInfobox.clear();
	}
}
