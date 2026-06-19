package com.ruthless;

import com.ruthless.RuthlessPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RuthlessPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RuthlessPlugin.class);
		RuneLite.main(args);
	}
}