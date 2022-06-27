package com.anthonyhilyard.cooperativeadvancements;

import java.util.Collection;
import java.util.List;

import com.anthonyhilyard.iceberg.events.CriterionCallback;

import net.minecraft.advancements.Advancement;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import net.minecraftforge.api.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class CooperativeAdvancements implements ModInitializer
{
	public static final String MODID = "cooperativeadvancements";

	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	private static MinecraftServer SERVER;

	private static boolean skipCriterionEvent = false;

	@Override
	public void onInitialize()
	{
		ModLoadingContext.registerConfig(MODID, ModConfig.Type.COMMON, CooperativeAdvancementsConfig.SPEC);

		ServerLifecycleEvents.SERVER_STARTING.register(CooperativeAdvancements::onServerAboutToStart);
		if (CooperativeAdvancementsConfig.INSTANCE.enabled.get())
		{
			ServerPlayConnectionEvents.JOIN.register(CooperativeAdvancements::onPlayerLogin);
			CriterionCallback.EVENT.register(CooperativeAdvancements::onCriterion);
		}
	}

	public static void onServerAboutToStart(MinecraftServer server)
	{
		SERVER = server;
	}

	/**
	 * Synchronizes the criteria of advancements for two players.
	 * @param first The first player.
	 * @param second The second player.
	 */
	public static void syncCriteria(ServerPlayer first, ServerPlayer second)
	{
		Collection<Advancement> allAdvancements = SERVER.getAdvancements().getAllAdvancements();

		// Loop through every possible advancement.
		for (Advancement advancement : allAdvancements)
		{
			for (String criterion : advancement.getCriteria().keySet())
			{
				// We know these iterables are actually lists, so just cast them.
				List<String> firstCompleted = (List<String>) first.getAdvancements().getOrStartProgress(advancement).getCompletedCriteria();
				List<String> secondCompleted = (List<String>) second.getAdvancements().getOrStartProgress(advancement).getCompletedCriteria();

				skipCriterionEvent = true;
				// If the first player has completed this criteria and the second hasn't, grant it to the second.
				if (firstCompleted.contains(criterion) && !secondCompleted.contains(criterion))
				{
					second.getAdvancements().award(advancement, criterion);
				}
				// Conversely, if the first hasn't completed it and the second has, grant it to the first.
				else if (!firstCompleted.contains(criterion) && secondCompleted.contains(criterion))
				{
					first.getAdvancements().award(advancement, criterion);
				}
				skipCriterionEvent = false;
			}
		}
	}

	/**
	 * Tries to grant a criterion for an advancement to all players whenever a player gains a new one.
	 */
	public static void onCriterion(Player player, Advancement advancement, String criterionKey)
	{
		if (skipCriterionEvent)
		{
			return;
		}

		List<ServerPlayer> currentPlayers = SERVER.getPlayerList().getPlayers();

		for (ServerPlayer serverPlayer : currentPlayers)
		{
			if (player != serverPlayer)
			{
				skipCriterionEvent = true;
				serverPlayer.getAdvancements().award(advancement, criterionKey);
				skipCriterionEvent = false;
			}
		}
	}

	/**
	 * Synchronizes advancements of all players whenever a new one logs in.
	 * @param event The PlayerLoggedInEvent.
	 */
	public static void onPlayerLogin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server)
	{
		List<ServerPlayer> currentPlayers = SERVER.getPlayerList().getPlayers();
		ServerPlayer newPlayer = handler.player;

		// Loop through all the currently-connected players and synchronize their advancements.
		for (ServerPlayer player : currentPlayers)
		{
			if (newPlayer != player)
			{
				syncCriteria(newPlayer, player);
			}
		}
	}
}
