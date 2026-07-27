package com.ruthless;

import com.ruthless.api.ClanBroadcastType;
import com.ruthless.utils.Constants;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(RuthlessPlugin.CONFIG_GROUP)
public interface RuthlessConfig extends Config
{
	public static final String MEMBER_API_KEY = "memberAPIKey";
	public static final String SHOW_CLAN_BROADCASTS = "showClanBroadcasts";
	public static final String BROADCAST_TYPE = "clanBroadcastType";
	public static final String BROADCAST_LIMIT = "clanBroadcastLimit";
	public static final String SEND_DEATHS = "sendDeaths";
	public static final String SEND_DEATH_MESSAGE = "sendDeathsMessage";
	public static final String CUSTOM_DEATH_WEBHOOK = "customDeathWebhooks";
	public static final String CUSTOM_LOOT_WEBHOOK = "customLootWebhooks";

	public static final String CLAN_ID = "clanId";
	public static final String BASE_API_HOSTNAME = "baseApiHostname";
	public static final String BASE_API_PORT = "baseApiPort";
	public static final String USE_HTTPS = "useHttps";

	@ConfigSection(
		name = "General",
		description = "General Ruthless plugin settings",
		position = 0
	)
	String generalSettings = "generalSettings";

	@ConfigItem(
		keyName = MEMBER_API_KEY,
		name = "Member API Key",
		description = "Provides verification. Use /api request-key in #commands in Ruthless discord.",
		secret = true,
		section = generalSettings,
			position = 0
	)
	default String memberAPIKey()
	{
		return "";
	}

	@ConfigItem(
			keyName = SHOW_CLAN_BROADCASTS,
			name = "Show Clan Broadcasts",
			description = "If enabled, will show the clan broadcast as a message once per session.",
			section = generalSettings,
			position = 1
	)
	default boolean showClanBroadcasts() { return true; }

	@ConfigItem(
			keyName = BROADCAST_TYPE,
			name = "Broadcast Type",
			description = "What type of broadcast you'd like. Defaults to in-game broadcast.",
			section = generalSettings,
			position = 2
	)
	default ClanBroadcastType clanBroadcastType() {
		return ClanBroadcastType.BROADCAST;
	}

	@ConfigItem(
			keyName = BROADCAST_LIMIT,
			name = "Broadcast limit",
			description = "Number of times to display a broadcast before not displaying again. 0 = unlimited times",
			section = generalSettings,
			position = 3
	)
	default int broadcastLimit() { return Constants.BROADCAST_LIMIT_DEFAULT;}

	@ConfigItem(
			keyName=SEND_DEATHS,
			name = "Send death screenshots",
			description = "Send Death screenshots to discord upon death.",
			section = generalSettings,
			position = 4
	)
	default boolean sendDeaths() { return false; }

	@ConfigItem(
			keyName=SEND_DEATH_MESSAGE,
			name = "Death message",
			description = "Message that is sent alongside screenshot",
			section = generalSettings,
			position = 5
	)
	default String deathMessage() { return "$name has died!"; }

	@ConfigItem(
			keyName = CUSTOM_DEATH_WEBHOOK,
			name = "Custom Discord Death Webhook",
			description = "Custom webhook url, separated by commas for multiple, that you want to send death alerts to",
			section = generalSettings,
			position = 6
	)
	default String customDeathWebhooks() { return ""; }

	@ConfigItem(
			keyName = CUSTOM_LOOT_WEBHOOK,
			name = "Custom Discord Loot Webhook",
			description = "Custom webhook url, separated by commas for multiple, that you want to send loot alerts to",
			section = generalSettings,
			position = 7
	)
	default String customLootWebhooks() { return ""; }

	@ConfigSection(
			name = "Development (DONT CHANGE)",
			description = "Development options for plugin. DO NOT CHANGE VALUES.",
			position = 1,
			closedByDefault = true
	)
	String developmentSettings = "developmentSettings";

	@ConfigItem(
			keyName = CLAN_ID,
			name = "Clan ID",
			description = "What clan ID to use for API",
			section = developmentSettings
	)
	default int clanId() { return Constants.RUTHLESS_CLAN_ID; }

	@ConfigItem(
			keyName = USE_HTTPS,
			name = "Use HTTPS",
			description = "Use HTTPS for the API calls. turn off for localhost",
			section = developmentSettings
	)
	default boolean useHttps() { return true; }

	@ConfigItem(
			keyName = BASE_API_HOSTNAME,
			name = "Base API URL",
			description = "What base URL to use for API requests",
			section = developmentSettings
	)
	default String baseApiHostname() { return Constants.RUTHLESS_BASE_HOSTNAME; }

	@ConfigItem(
			keyName = BASE_API_PORT,
			name = "Base API PORT",
			description = "What port to use for API requests",
			section = developmentSettings
	)
	default int baseApiPort() { return Constants.RUTHLESS_DEFAULT_PORT; }


}
