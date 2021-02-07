package com.anthonyhilyard.cooperativeadvancements;

import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraft.server.integrated.IntegratedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class StartupClientOnly
{
	private static final Logger LOGGER = LogManager.getLogger();

	public static DistExecutor.SafeRunnable clientSetup(MinecraftServer SERVER)
	{
		return new DistExecutor.SafeRunnable()
		{
			@Override public void run()
			{
				IntegratedServer server = (IntegratedServer) SERVER;

				if (!server.isDedicatedServer())
				{
					// Replace the current player list with our new one.
					server.setPlayerList(new CustomIntegratedPlayerList(server));
				}
			}
		};
	}
	
}