package com.anthonyhilyard.cooperativeadvancements;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("cooperativeadvancements")
public class CooperativeAdvancements
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static MinecraftServer SERVER;
	public static IEventBus MOD_EVENT_BUS;

	public CooperativeAdvancements()
	{
		// Register ourselves for server and other game events we are interested in.
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onServerAboutToStart(FMLServerAboutToStartEvent event)
	{
		SERVER = event.getServer();

		if (SERVER.isDedicatedServer())
		{
			// Replace the current player list with our new one.
			SERVER.setPlayerList(new CustomDedicatedPlayerList((DedicatedServer) SERVER));
		}
		else
		{
			DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> StartupClientOnly.clientSetup(SERVER));
		}
	}

	public static void registerClientOnlyEvents()
	{
		MOD_EVENT_BUS.register(StartupClientOnly.class);
	}

	/**
	 * Synchronizes the criteria of advancements for two players.
	 * @param first The first player.
	 * @param second The second player.
	 */
	public static void syncCriteria(ServerPlayerEntity first, ServerPlayerEntity second)
	{
		Collection<Advancement> allAdvancements = SERVER.getAdvancementManager().getAllAdvancements();

		// Loop through every possible advancement.
		for (Advancement advancement : allAdvancements)
		{
			for (String criterion : advancement.getCriteria().keySet())
			{
				// We know these iterables are actually lists, so just cast them.
				List<String> firstCompleted = (List<String>) first.getAdvancements().getProgress(advancement).getCompletedCriteria();
				List<String> secondCompleted = (List<String>) second.getAdvancements().getProgress(advancement).getCompletedCriteria();

				// If the first player has completed this criteria and the second hasn't, grant it to the second.
				if (firstCompleted.contains(criterion) && !secondCompleted.contains(criterion))
				{
					second.getAdvancements().grantCriterion(advancement, criterion);
				}
				// Conversely, if the first hasn't completed it and the second has, grant it to the first.
				else if (!firstCompleted.contains(criterion) && secondCompleted.contains(criterion))
				{
					first.getAdvancements().grantCriterion(advancement, criterion);
				}
			}
		}
	}



	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
	public static class AdvancementEvents
	{
		/**
		 * Tries to grant a criterion for an advancement to all players whenever a player gains a new one.
		 */
		@SubscribeEvent
		public static void onCriterion(final CriterionEvent event)
		{
			List<ServerPlayerEntity> currentPlayers = SERVER.getPlayerList().getPlayers();
			Advancement advancement = event.getAdvancement();
			String criterion = event.getCriterionKey();

			for (ServerPlayerEntity player : currentPlayers)
			{
				if (event.getPlayer() != player)
				{
					player.getAdvancements().grantCriterion(advancement, criterion);
				}
			}
			event.setResult(Result.ALLOW);
		}

		/**
		 * Synchronizes advancements of all players whenever a new one logs in.
		 * @param event The PlayerLoggedInEvent.
		 */
		@SubscribeEvent
		public static void onPlayerLogIn(final PlayerLoggedInEvent event)
		{
			List<ServerPlayerEntity> currentPlayers = SERVER.getPlayerList().getPlayers();
			ServerPlayerEntity newPlayer = (ServerPlayerEntity)event.getPlayer();

			// Loop through all the currently-connected players and synchronize their advancements.
			for (ServerPlayerEntity player : currentPlayers)
			{
				if (newPlayer != player)
				{
					syncCriteria(newPlayer, player);
				}
			}
			event.setResult(Result.ALLOW);
		}
	}
}
