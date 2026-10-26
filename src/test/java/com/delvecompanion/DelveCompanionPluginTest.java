package com.delvecompanion;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/** Launches a developer-mode RuneLite client with this plugin loaded: ./gradlew run */
public class DelveCompanionPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(DelveCompanionPlugin.class);
		RuneLite.main(args);
	}
}
