package com.ruthless;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ruthless.event.ItemOfTheDayReceivedEvent;
import com.ruthless.event.RuthlessSlayerTaskInfoReceivedEvent;
import com.ruthless.ui.ItemOfTheDayInfoBox;
import com.ruthless.ui.RuthlessSlayerTaskInfoBox;
import com.ruthless.web.RuthlessClient;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.util.Objects;

@Slf4j
@PluginDescriptor(
	name = "Ruthless Clan",
		tags = {"ruthless", "clan"},
		description = "Automates things for Ruthless clan."
)
public class RuthlessPlugin extends Plugin
{
	static final String CONFIG_GROUP = "ruthlessosrsclan";

	private @Inject Client client;
	private @Inject RuthlessConfig config;
	private @Inject RuthlessClient ruthlessClient;
	private @Inject InfoBoxManager infoBoxManager;
	private @Inject ClientThread clientThread;

	private RuthlessInfobox;



	@Override
	protected void startUp() throws Exception
	{
		ruthlessClient.getItemOfTheDay();
		if( client.getLocalPlayer() != null) {
			this.attemptGetSlayerTask();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		cleanupInfoboxes();
	}

	@Provides
	RuthlessConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(RuthlessConfig.class);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (!configChanged.getGroup().equals(CONFIG_GROUP)) {
			return;
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			//login triggered,
			clientThread.invokeLater(this::attemptGetSlayerTask);
		}
	}

	@Subscribe
	public void onItemOfTheDayReceivedEvent( ItemOfTheDayReceivedEvent event ) {
		if (!config.showIotdInfobox()) {
			return;
		}
		if (null != iotdInfoBox) {
			infoBoxManager.removeInfoBox(iotdInfoBox);
		}
		iotdInfoBox = new ItemOfTheDayInfoBox(event.getItemOfTheDay(), this);
		infoBoxManager.addInfoBox(iotdInfoBox);

	}

	@Subscribe
	public void onRuthlessSlayerTaskInfoReceivedEvent( RuthlessSlayerTaskInfoReceivedEvent event ) {
		if (!config.showSlayertaskInfobox()) {
			return;
		}
		if (null != ruthlessSlayerTaskInfoBox) {
			infoBoxManager.removeInfoBox(ruthlessSlayerTaskInfoBox);
		}
		ruthlessSlayerTaskInfoBox = new RuthlessSlayerTaskInfoBox(event.getRuthlessSlayerTask(), this);
		infoBoxManager.addInfoBox(ruthlessSlayerTaskInfoBox);

	}

	private void cleanupInfoboxes() {
		if (iotdInfoBox != null) {
			infoBoxManager.removeInfoBox(iotdInfoBox);
			iotdInfoBox = null;
		}
		if (ruthlessSlayerTaskInfoBox != null) {
			infoBoxManager.removeInfoBox(ruthlessSlayerTaskInfoBox);
			ruthlessSlayerTaskInfoBox = null;
		}
	}

	private boolean attemptGetSlayerTask() {

		Player local = client.getLocalPlayer();
		if (local == null) {
			return false;
		}
		log.debug("Trying to get slayer task for {}", local.getName());
		ruthlessClient.getCurrentSlayerTask(local.getName());
		return true;
	}
}
