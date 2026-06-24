package com.ruthless;

import com.google.inject.Provides;
import javax.inject.Inject;

import com.ruthless.event.ClanBroadcastEvent;
import com.ruthless.event.ItemOfTheDayReceivedEvent;
import com.ruthless.event.MemberAPIKeyInvalidEvent;
import com.ruthless.event.RuthlessSlayerTaskInfoReceivedEvent;
import com.ruthless.ui.infobox.RuthlessInfobox;
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
import net.runelite.client.callback.ClientThread;
import net.runelite.client.chat.ChatMessageBuilder;
import net.runelite.client.chat.ChatMessageManager;
import net.runelite.client.chat.QueuedMessage;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.task.Schedule;
import net.runelite.client.ui.overlay.OverlayManager;
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
	private @Inject OverlayManager overlayManager;
	private @Inject MemberAPIKeyInvalidOverlay memberAPIKeyInvalidOverlay;
	private @Inject ChatMessageBuilder chatMessageBuilder;
	private @Inject ClanBroadcastValidator clanBroadcastValidator;
	private @Inject ChatMessageManager chatMessageManager;

	private RuthlessInfobox ruthlessInfobox;
	private boolean sentClanBroadcast;
	private boolean memberAPIKeyValid;



	@Override
	protected void startUp() throws Exception
	{
		ruthlessInfobox = new RuthlessInfobox(this, config);
		infoBoxManager.addInfoBox(ruthlessInfobox);
		ruthlessClient.getItemOfTheDay();
		sentClanBroadcast = false;
		if( client.getLocalPlayer() != null) {
			this.attemptGetSlayerTask();
		}
		memberAPIKeyValid = !config.memberAPIKey().isEmpty();
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
			clientThread.invokeLater(this::attemptGetSlayerTask);
			if (!sentClanBroadcast) {
				clientThread.invokeLater(this::queueClanBroadcast);
			}
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

	@Subscribe
	public void onClanBroadcastEvent(ClanBroadcastEvent event ) {

		ClanBroadcast broadcast = event.getClanBroadcast();
		if (clanBroadcastValidator.valid(broadcast)) {
			sentClanBroadcast = true;
			ChatMessageBuilder cmd = new ChatMessageBuilder();
			cmd.append("[Ruthless] ").append(broadcast.getMessage());
			chatMessageManager.queue(QueuedMessage.builder()
					.type(ChatMessageType.BROADCAST)
					.runeLiteFormattedMessage(cmd.build()).build());
		}
	}

	@Subscribe
	public void onMemberAPIKeyInvalidEvent(MemberAPIKeyInvalidEvent event) {
		overlayManager.add(memberAPIKeyInvalidOverlay);
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

	private boolean queueClanBroadcast() {

		if (sentClanBroadcast) {
			return true;
		}
		Player local = client.getLocalPlayer();

		if ( local == null ) {
			return false;
		}
		ruthlessClient.getClanBroadcast();
		return true;
	}

	@Schedule(
		period = 1,
		unit= ChronoUnit.MINUTES
	)
	public void iotdSchedule() {
		log.debug("Scheduled Ruthless iotd lookup");
		ruthlessClient.getItemOfTheDay();
	}

	@Schedule(
		period = 1,
		unit=ChronoUnit.MINUTES
	)
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
