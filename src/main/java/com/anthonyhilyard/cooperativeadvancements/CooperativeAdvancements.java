package com.anthonyhilyard.cooperativeadvancements;

import java.util.Collection;
import java.util.List;

import com.anthonyhilyard.iceberg.events.CriterionEvent;

import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("cooperativeadvancements")
public class CooperativeAdvancements
{
	@SuppressWarnings("unused")
	private static final Logger LOGGER = LogManager.getLogger();
	private static MinecraftServer SERVER;
	
	private static boolean skipCriterionEvent = false;

	public CooperativeAdvancements()
	{
		MinecraftForge.EVENT_BUS.register(this);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CooperativeAdvancementsConfig.SPEC);
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
	}

	@SubscribeEvent
	public void onServerAboutToStart(FMLServerAboutToStartEvent event)
	{
		SERVER = event.getServer();
	}

	/**
	 * Synchronizes the criteria of advancements for two players.
	 * @param first The first player.
	 * @param second The second player.
	 */
	public static void syncCriteria(ServerPlayerEntity first, ServerPlayerEntity second)
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



	@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.FORGE)
	public static class AdvancementEvents
	{
		/**
		 * Tries to grant a criterion for an advancement to all players whenever a player gains a new one.
		 */
		@SubscribeEvent
		public static void onCriterion(final CriterionEvent event)
		{
			if (skipCriterionEvent)
			{
				return;
			}

			if (!CooperativeAdvancementsConfig.INSTANCE.enabled.get())
			{
				event.setResult(Result.DENY);
			}
			else
			{
				List<ServerPlayerEntity> currentPlayers = SERVER.getPlayerList().getPlayers();
				Advancement advancement = event.getAdvancement();
				String criterion = event.getCriterionKey();
				PlayerEntity player = event.getPlayer();

				for (ServerPlayerEntity serverPlayer : currentPlayers)
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
						serverPlayer.getAdvancements().award(advancement, criterion);
						skipCriterionEvent = false;
					}
				}
				event.setResult(Result.ALLOW);
			}
		}

		/**
		 * Synchronizes advancements of all players whenever a new one logs in.
		 * @param event The PlayerLoggedInEvent.
		 */
		@SubscribeEvent
		public static void onPlayerLogIn(final PlayerLoggedInEvent event)
		{
			if (!CooperativeAdvancementsConfig.INSTANCE.enabled.get())
			{
				event.setResult(Result.DENY);
			}
			else
			{
				List<ServerPlayerEntity> currentPlayers = SERVER.getPlayerList().getPlayers();
				ServerPlayerEntity player = (ServerPlayerEntity)event.getPlayer();

				// Loop through all the currently-connected players and synchronize their advancements.
				for (ServerPlayerEntity serverPlayer : currentPlayers)
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
				event.setResult(Result.ALLOW);
			}
		}
	}
}
