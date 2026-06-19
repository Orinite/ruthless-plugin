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
		position = 1
	)
	default String memberAPIKey()
	{
		return "";
	}
}
