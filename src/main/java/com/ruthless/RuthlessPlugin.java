package com.ruthless;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ruthless.api.RuthlessInfoBox;
import com.ruthless.event.RuthlessLootTracking;
import com.ruthless.ui.ItemOfTheDayInfoBox;
import com.ruthless.web.RuthlessClient;
import com.ruthless.web.response.ClanItemWhitelist;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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

	@Inject
	private ChatMessageManager chatMessageManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private RuthlessLootTracking ruthlessLootTracking;

	private boolean sentClanBroadcast;

	private List<ClanItemWhitelist> clanItemWhitelist;

	private ItemOfTheDayInfoBox itemOfTheDayInfoBox;

	@Override
	protected void startUp() throws Exception
	{
		if (config.showIotd()) {
			ruthlessClient.getItemOfTheDay();
		}
		eventBus.register(ruthlessLootTracking);
		sentClanBroadcast = false;
		getItemWhitelist();
	}

	@Override
	protected void shutDown() throws Exception
	{
		cleanupAllInfoBoxes();
		clanItemWhitelist = new ArrayList<ClanItemWhitelist>();
		eventBus.unregister(ruthlessLootTracking);
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
		cleanupAllInfoBoxes();
		if( config.showIotd() ) {
			ruthlessClient.getItemOfTheDay();
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged) {
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
			clientThread.invokeLater(this::queueClanBroadcast);
		}
	}

	public void addIotdInfoBox(ItemOfTheDayInfoBox infoBox) {
		this.itemOfTheDayInfoBox = infoBox;
		infoBoxManager.addInfoBox(infoBox);
	}

	public void cleanupAllInfoBoxes() {
		this.itemOfTheDayInfoBox = null;
		cleanupInfoBoxes(RuthlessInfoBox.class);
	}

	public void cleanupInfoBoxes(Class clazz) {
		infoBoxManager.removeIf(clazz::isInstance);
	}

	private boolean queueClanBroadcast() {
		//we only want to send once per session. if we already sent then dont send again.
		if (sentClanBroadcast) {
			return true;
		}
		Player local = client.getLocalPlayer();

		if ( local == null ) {
			return false;
		}
		this.ruthlessClient.getClanBroadcast();
		sentClanBroadcast = true;
		return true;
	}

	private void getItemWhitelist() {
		ruthlessClient.getClanItemWhitelist();
	}

	public void setItemWhitelist(List<ClanItemWhitelist> clanItemWhitelist) {
		this.clanItemWhitelist = clanItemWhitelist;
	}

	@Schedule(
			period = 1,
			unit = ChronoUnit.MINUTES
	)
	public void fetchItemOfTheDay() {
		// Only fetch iotd if logged in or if configuration option is enabled.
		if( this.client.getGameState() != GameState.LOGGED_IN || !config.showIotd() ) {
			return;
		}
		if( itemOfTheDayInfoBox == null ) {
			this.ruthlessClient.getItemOfTheDay();
		} else {
			Instant now = Instant.now();
			Instant expiration = Instant.ofEpochSecond( itemOfTheDayInfoBox.getItemOfTheDay().getExpirationTimestamp());
			// only check for new iotd if current one expired.
			if (now.isAfter(expiration)) {
				this.cleanupInfoBoxes(ItemOfTheDayInfoBox.class);
				this.ruthlessClient.getItemOfTheDay();
			}
		}
	}
}
