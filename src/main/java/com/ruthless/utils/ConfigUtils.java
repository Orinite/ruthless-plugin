package com.ruthless.utils;

import lombok.experimental.UtilityClass;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.RuneLiteConfig;

@UtilityClass
public class ConfigUtils {

    public boolean isPluginDisabled(ConfigManager configManager, String simpleLowerClassName) {
        return "false".equals(configManager.getConfiguration(RuneLiteConfig.GROUP_NAME, simpleLowerClassName));
    }
}
