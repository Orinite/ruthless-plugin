package com.ruthless;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(RuthlessPlugin.CONFIG_GROUP)
public interface RuthlessConfig extends Config
{
	public static final String MEMBER_API_KEY = "memberAPIKey";
	public static final String SHOW_IOTD_INFO = "showIotdInfoInInfobox";
	public static final String SHOW_SLAYER_INFO = "showSlayerInfoInInfobox";
	public static final String SHOW_NEW_SLAYERTASK_CHAT_NOTIFICATION = "showNewSlayertaskChatNotification";

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
			name = "Infobox",
			description = "Infobox Settings for display",
			position = 2
	)
	String infoboxSettings = "infoboxSettings";

	@ConfigItem(
			keyName = SHOW_IOTD_INFO,
			name = "Show Item Of the Day Infobox",
			description = "Show the Item of the day information in Ruthless Infobox",
			position = 3,
			section = infoboxSettings
	)
	default boolean showIotdInInfobox() { return true; }

	@ConfigItem(
			keyName = SHOW_SLAYER_INFO,
			name = "Show Ruthless Slayertask Infobox",
			description = "Show the Slayertask information in Ruthless Infobox",
			position = 4,
			section = infoboxSettings
	)
	default boolean showSlayertaskInInfobox() { return true; }

	@ConfigSection(
			name = "Chat Notifications",
			description = "Chat notifications when clan things happen",
			position = 5
	)
	String chatNotificationSettings = "chatNotificationSettings";

	@ConfigItem(
			keyName = SHOW_NEW_SLAYERTASK_CHAT_NOTIFICATION,
			name = "Show New Slayertask Notifications",
			description = "Shows new slayertask notification in chatbox when receiving a new slayertask",
			position = 6,
			section = chatNotificationSettings
	)
	default boolean showNewSlayertaskChatNotification() { return true; }

}
