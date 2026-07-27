package com.ruthless;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ruthless.api.ClanBroadcastType;
import com.ruthless.event.ClanBroadcastEvent;
import com.ruthless.event.MemberAPIKeyInvalidEvent;
import com.ruthless.eventprocessor.BossKillChatEventProcessor;
import com.ruthless.eventprocessor.DonationChatEventProcessor;
import com.ruthless.eventprocessor.LootReceivedProcessor;
import com.ruthless.eventprocessor.PlayerDeathProcessor;
import com.ruthless.ui.infobox.RuthlessInfoboxManager;
import com.ruthless.ui.overlay.MemberAPIKeyInvalidOverlay;
import com.ruthless.utils.ClanBroadcastValidator;
import com.ruthless.web.RuthlessClient;
import com.ruthless.web.response.ClanBroadcast;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.Player;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.gameval.VarPlayerID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;

import java.awt.*;
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
	private @Inject OverlayManager overlayManager;
	private @Inject MemberAPIKeyInvalidOverlay memberAPIKeyInvalidOverlay;
	private @Inject ChatMessageBuilder chatMessageBuilder;
	private @Inject ClanBroadcastValidator clanBroadcastValidator;
	private @Inject ChatMessageManager chatMessageManager;
	private @Inject EventBus eventBus;
	private @Inject BossKillChatEventProcessor bossKillChatEventProcessor;
	private @Inject DonationChatEventProcessor donationChatEventProcessor;
	private @Inject LootReceivedProcessor lootReceivedProcessor;
	private @Inject PlayerDeathProcessor playerDeathProcessor;
	private @Inject RuthlessInfoboxManager ruthlessInfoboxManager;

	private boolean sentClanBroadcast;
	private boolean memberAPIKeyValid;



	@Override
	protected void startUp() throws Exception
	{
		//register event processor(s)
		eventBus.register(bossKillChatEventProcessor);
		eventBus.register(lootReceivedProcessor);
		eventBus.register(donationChatEventProcessor);
		eventBus.register(playerDeathProcessor);

		ruthlessClient.getClanWhitelist();
		ruthlessClient.getWebhooks();

		sentClanBroadcast = false;
		memberAPIKeyValid = !config.memberAPIKey().isEmpty();
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.removeIf(MemberAPIKeyInvalidOverlay.class::isInstance);
		eventBus.unregister(bossKillChatEventProcessor);
		eventBus.unregister(lootReceivedProcessor);
		eventBus.unregister(donationChatEventProcessor);
		eventBus.unregister(playerDeathProcessor);
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

		if (configChanged.getKey().equals(RuthlessConfig.MEMBER_API_KEY)) {
			memberAPIKeyValid = !configChanged.getNewValue().isEmpty();
			overlayManager.removeIf(MemberAPIKeyInvalidOverlay.class::isInstance);
			if (memberAPIKeyValid) {
				clientThread.invokeLater(this::queueClanBroadcast);
			} else {
				overlayManager.add(memberAPIKeyInvalidOverlay);
			}
		}
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event) {
		if (event.getGameState() == GameState.LOGGED_IN) {
			//login triggered,
			if (!sentClanBroadcast) {
				clientThread.invokeLater(this::queueClanBroadcast);
			}
		}
	}

	@Subscribe
	public void onClanBroadcastEvent(ClanBroadcastEvent event ) {
		//if not enabled, dont show.
		if (!config.showClanBroadcasts()) { return; }

		ClanBroadcast broadcast = event.getClanBroadcast();
		if (clanBroadcastValidator.valid(broadcast)) {
			sentClanBroadcast = true;
			ChatMessageBuilder cmd = new ChatMessageBuilder();
			cmd.append("[Ruthless] ").append(broadcast.getMessage());
			chatMessageManager.queue(QueuedMessage.builder()
					.type(determineChatType())
					.runeLiteFormattedMessage(cmd.build()).build()
			);
		}
	}

	private ChatMessageType determineChatType() {
		switch (config.clanBroadcastType())
		{
			case BROADCAST:
				return ChatMessageType.BROADCAST;
			case GAME_MESSAGE:
				return ChatMessageType.CLAN_MESSAGE;
		}
		return ChatMessageType.BROADCAST;



	}

	@Subscribe
	public void onMemberAPIKeyInvalidEvent(MemberAPIKeyInvalidEvent event) {
		overlayManager.add(memberAPIKeyInvalidOverlay);
	}

	private boolean queueClanBroadcast() {

		if (sentClanBroadcast) {
			return true;
		}
		Player local = client.getLocalPlayer();

		if ( local == null ) {
			return false;
		}
		log.debug(" Getting clan broadcasts");
		ruthlessClient.getClanBroadcast();
		return true;
	}

	/**
	 * Scheduled polling for new information since we don't use Websockets... yet
	 */
	@Schedule(
		period = 1,
		unit= ChronoUnit.MINUTES
	)
	public void iotdSchedule() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			//we aren't logged in, dont poll.
			return;
		}
		Player local = client.getLocalPlayer();
		if (local == null) {
			//player info isnt loaded, dont poll.
			return;
		}
	}

	@Schedule(
			period = 5,
			unit = ChronoUnit.MINUTES
	)
	public void refetchWhitelist() {
		if (client.getGameState() != GameState.LOGGED_IN) {
			return;
		}
		ruthlessClient.getClanWhitelist();
		ruthlessClient.getWebhooks();
	}
}
