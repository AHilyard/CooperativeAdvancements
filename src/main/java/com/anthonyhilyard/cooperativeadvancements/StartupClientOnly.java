package com.anthonyhilyard.cooperativeadvancements;

import java.lang.reflect.Field;

import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraft.world.storage.PlayerData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class StartupClientOnly
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static DistExecutor.SafeRunnable clientSetup(MinecraftServer SERVER)
	{
		return new DistExecutor.SafeRunnable()
		{
			@Override public void run() {
				IntegratedServer server = (IntegratedServer) SERVER;

				try
				{
					// Use reflection to access some private fields in the server to hack in our custom player list.
					Field dynamicRegistriesField = server.getClass().getSuperclass().getDeclaredField("field_240767_f_");
					dynamicRegistriesField.setAccessible(true);
					DynamicRegistries.Impl registries = (DynamicRegistries.Impl) dynamicRegistriesField.get(server);

					Field playerDataManagerField = server.getClass().getSuperclass().getDeclaredField("playerDataManager");
					playerDataManagerField.setAccessible(true);
					PlayerData playerDataManager = (PlayerData) playerDataManagerField.get(server);

					if (!server.isDedicatedServer())
					{
						// Replace the current player list with our new one.
						server.setPlayerList(new CustomIntegratedPlayerList(server, registries, playerDataManager));
					}
				}
				catch (NoSuchFieldException|IllegalAccessException e)
				{
					LOGGER.error(e.toString());
				}
			}
		};
	}
	
}