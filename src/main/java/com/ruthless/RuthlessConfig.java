package com.ruthless;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RuthlessPlugin.CONFIG_GROUP)
public interface RuthlessConfig extends Config
{
	@ConfigItem(
		keyName = "memberAPIKey",
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
			keyName = "showIotdInfobox",
			name = "Show Item Of the Day Infobox",
			description = "Show the Item of the day Infobox in the UI",
			position = 2,
			section = "Infobox"
	)
	default boolean showIotdInfobox() { return true; }

	@ConfigItem(
			keyName = "showSlayerInfobox",
			name = "Show Ruthless Slayertask Infobox",
			description = "Show the Slayertask infobox in the UI",
			position = 3,
			section = "Infobox"
	)
	default boolean showSlayertaskInfobox() { return true; }

}
