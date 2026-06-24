package com.ruthless;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RuthlessPlugin.CONFIG_GROUP)
public interface RuthlessConfig extends Config
{
	public static final String MEMBER_API_KEY = "memberAPIKey";
	public static final String SHOW_IOTD_INFO = "showIotdInfoInInfobox";
	public static final String SHOW_SLAYER_INFO = "showSlayerInfoInInfobox";

	@ConfigItem(
		keyName = MEMBER_API_KEY,
		name = "Member API Key",
		description = "API Key to verify the member. If you need one, please generate in Discord server",
		secret = true,
		position = 1,
		section = "General"
	)
	default String memberAPIKey()
	{
		return "";
	}

	@ConfigItem(
			keyName = SHOW_IOTD_INFO,
			name = "Show Item Of the Day Infobox",
			description = "Show the Item of the day Infobox in the UI",
			position = 2,
			section = "Infobox"
	)
	default boolean showIotdInInfobox() { return true; }

	@ConfigItem(
			keyName = SHOW_SLAYER_INFO,
			name = "Show Ruthless Slayertask Infobox",
			description = "Show the Slayertask infobox in the UI",
			position = 3,
			section = "Infobox"
	)
	default boolean showSlayertaskInInfobox() { return true; }

}
