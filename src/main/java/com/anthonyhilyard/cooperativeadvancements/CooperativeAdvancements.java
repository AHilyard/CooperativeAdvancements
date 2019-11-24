package com.anthonyhilyard.cooperativeadvancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;

@Mod("cooperativeadvancements")
public class CooperativeAdvancements
{
	private static final Logger LOGGER = LogManager.getLogger();
	private static MinecraftServer SERVER;

	public CooperativeAdvancements()
	{
		// Register ourselves for server and other game events we are interested in.
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent event)
	{
		SERVER = event.getServer();
	}

	/**
	 * Synchronizes the advancements of two players.
	 * @param first The first player.
	 * @param second The second player.
	 */
	public static void syncAdvancements(ServerPlayerEntity first, ServerPlayerEntity second)
	{
		Collection<Advancement> allAdvancements = SERVER.getAdvancementManager().getAllAdvancements();

		// Loop through every possible advancement.
		for (Advancement advancement : allAdvancements)
		{
			// If the first player has completed this advancement and the second hasn't, grant it to the second.
			if (first.getAdvancements().getProgress(advancement).isDone() && !second.getAdvancements().getProgress(advancement).isDone())
			{
				grantAdvancement(second, advancement);
			}
			// Conversely, if the first hasn't completed it and the second has, grant it to the first.
			else if (!first.getAdvancements().getProgress(advancement).isDone() && second.getAdvancements().getProgress(advancement).isDone())
			{
				grantAdvancement(first, advancement);
			}
		}
	}

	/**
	 * Grants an advancement to a player.
	 * @param player The player.
	 * @param advancement The advancement.
	 */
	public static void grantAdvancement(ServerPlayerEntity player, Advancement advancement)
	{
		for (String criterion : advancement.getCriteria().keySet())
		{
			player.getAdvancements().grantCriterion(advancement, criterion);
		}
	}

	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
	public static class AdvancementEvents
	{
		/**
		 * Tries to grant an advancement to all players whenever a player gains a new one.
		 */
		@SubscribeEvent
		public static void onAdvancement(final AdvancementEvent event)
		{
			List<ServerPlayerEntity> currentPlayers = SERVER.getPlayerList().getPlayers();
			Advancement advancement = event.getAdvancement();

			for (ServerPlayerEntity player : currentPlayers)
			{
				if (event.getPlayer() != player)
				{
					grantAdvancement(player, advancement);
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
					syncAdvancements(newPlayer, player);
				}
			}
			event.setResult(Result.ALLOW);
		}
	}
}
