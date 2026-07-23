package com.ruthless;

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

	public static final String CLAN_ID = "clanId";
	public static final String BASE_API_HOSTNAME = "baseApiHostname";
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
		section = generalSettings
	)
	default String memberAPIKey()
	{
		return "";
	}

	@ConfigItem(
			keyName = SHOW_CLAN_BROADCASTS,
			name = "Show Clan Broadcasts",
			description = "If enabled, will show the clan broadcast as a message once per session.",
			section = generalSettings
	)
	default boolean showClanBroadcasts() { return true; }

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
}
