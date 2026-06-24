package com.ruthless;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ruthless.event.ItemOfTheDayReceivedEvent;
import com.ruthless.event.RuthlessSlayerTaskInfoReceivedEvent;
import com.ruthless.ui.RuthlessInfobox;
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
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.time.temporal.ChronoUnit;

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

	private RuthlessInfobox ruthlessInfobox;



	@Override
	protected void startUp() throws Exception
	{
		ruthlessInfobox = new RuthlessInfobox(this, config);
		infoBoxManager.addInfoBox(ruthlessInfobox);
		ruthlessClient.getItemOfTheDay();
		if( client.getLocalPlayer() != null) {
			this.attemptGetSlayerTask();
		}
	}

	@Override
	protected void shutDown() throws Exception
	{
		cleanupInfobox();
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
		ruthlessInfobox.setItemOfTheDay(event.getItemOfTheDay());

	}

	@Subscribe
	public void onRuthlessSlayerTaskInfoReceivedEvent( RuthlessSlayerTaskInfoReceivedEvent event ) {
		ruthlessInfobox.setRuthlessSlayerTaskInfo(event.getRuthlessSlayerTask());

	}

	private void cleanupInfobox() {
		ruthlessInfobox = null;
		infoBoxManager.removeIf(RuthlessInfobox.class::isInstance);
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

	@Schedule(period = 1, unit= ChronoUnit.MINUTES)
	public void iotdSchedule() {
		log.debug("Scheduled iotd lookup");
		ruthlessClient.getItemOfTheDay();
	}

	@Schedule(period=1, unit=ChronoUnit.MINUTES)
	public void slayerTaskSchedule() {

		Player local = client.getLocalPlayer();
		if (local == null) {
			//we aren't logged in, dont poll
			return;
		}
		log.debug("Scheduling slayer task lookup");
		ruthlessClient.getCurrentSlayerTask(local.getName());
	}
}
