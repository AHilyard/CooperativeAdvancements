package com.anthonyhilyard.cooperativeadvancements;

import java.util.Collection;
import java.util.List;

import com.anthonyhilyard.iceberg.events.CriterionCallback;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
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

import fuzs.forgeconfigapiport.api.config.v2.ForgeConfigRegistry;
import net.minecraftforge.fml.config.ModConfig;

@SuppressWarnings("deprecation")
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
		ForgeConfigRegistry.INSTANCE.register(MODID, ModConfig.Type.COMMON, CooperativeAdvancementsConfig.SPEC);

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
		Collection<AdvancementHolder> allAdvancements = SERVER.getAdvancements().getAllAdvancements();

		// Loop through every possible advancement.
		for (AdvancementHolder advancementHolder : allAdvancements)
		{
			Advancement advancement = advancementHolder.value();
			for (String criterion : advancement.criteria().keySet())
			{
				// We know these iterables are actually lists, so just cast them.
				List<String> firstCompleted = (List<String>) first.getAdvancements().getOrStartProgress(advancementHolder).getCompletedCriteria();
				List<String> secondCompleted = (List<String>) second.getAdvancements().getOrStartProgress(advancementHolder).getCompletedCriteria();

				skipCriterionEvent = true;
				// If the first player has completed this criteria and the second hasn't, grant it to the second.
				if (firstCompleted.contains(criterion) && !secondCompleted.contains(criterion))
				{
					second.getAdvancements().award(advancementHolder, criterion);
				}
				// Conversely, if the first hasn't completed it and the second has, grant it to the first.
				else if (!firstCompleted.contains(criterion) && secondCompleted.contains(criterion))
				{
					first.getAdvancements().award(advancementHolder, criterion);
				}
				skipCriterionEvent = false;
			}
		}
	}

	/**
	 * Tries to grant a criterion for an advancement to all players whenever a player gains a new one.
	 */
	public static void onCriterion(Player player, AdvancementHolder advancementHolder, String criterionKey)
	{
		if (skipCriterionEvent)
		{
			return;
		}

		if (!CooperativeAdvancementsConfig.INSTANCE.enabled.get())
		{
			return;
		}
		else
		{
			List<ServerPlayer> currentPlayers = SERVER.getPlayerList().getPlayers();

			for (ServerPlayer serverPlayer : currentPlayers)
			{
				if (player != serverPlayer)
				{
					// Only synchronize between team members if the config option is enabled.
					if (CooperativeAdvancementsConfig.INSTANCE.perTeam.get() &&
						player.getTeam() != null && serverPlayer.getTeam() != null &&
						player.getTeam().getName().equals(serverPlayer.getTeam().getName()))
					{
						continue;
					}

					skipCriterionEvent = true;
					serverPlayer.getAdvancements().award(advancementHolder, criterionKey);
					skipCriterionEvent = false;
				}
			}
		}
	}

	/**
	 * Synchronizes advancements of all players whenever a new one logs in.
	 * @param event The PlayerLoggedInEvent.
	 */
	public static void onPlayerLogin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server)
	{
		if (!CooperativeAdvancementsConfig.INSTANCE.enabled.get())
		{
			return;
		}
		else
		{
			List<ServerPlayer> currentPlayers = SERVER.getPlayerList().getPlayers();
			ServerPlayer player = handler.player;

			// Loop through all the currently-connected players and synchronize their advancements.
			for (ServerPlayer serverPlayer : currentPlayers)
			{
				if (player != serverPlayer)
				{
					// Only synchronize between team members if the config option is enabled.
					if (CooperativeAdvancementsConfig.INSTANCE.perTeam.get() &&
						player.getTeam() != null && serverPlayer.getTeam() != null &&
						player.getTeam().getName().equals(serverPlayer.getTeam().getName()))
					{
						continue;
					}

					syncCriteria(player, serverPlayer);
				}
			}
		}
	}
}
