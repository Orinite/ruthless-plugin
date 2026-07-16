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

	public static final String CLAN_ID = "clanId";

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
		position = 1,
		section = generalSettings
	)
	default String memberAPIKey()
	{
		return "";
	}

	@ConfigSection(
			name = "Development",
			description = "Development options for plugin. DO NOT CHANGE VALUES.",
			position = 6,
			closedByDefault = true
	)
	String developmentSettings = "developmentSettings";

	@ConfigItem(
			keyName = CLAN_ID,
			name = "Clan ID",
			description = "What clan ID to use for API",
			position = 7,
			section = developmentSettings
	)
	default int clanId() { return Constants.RUTHLESS_CLAN_ID; }
}
