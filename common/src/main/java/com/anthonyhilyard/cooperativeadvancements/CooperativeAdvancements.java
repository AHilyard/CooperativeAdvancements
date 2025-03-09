package com.anthonyhilyard.cooperativeadvancements;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.anthonyhilyard.cooperativeadvancements.config.CooperativeAdvancementsConfig;
import com.anthonyhilyard.iceberg.events.common.CriterionEvent;
import com.anthonyhilyard.iceberg.events.server.PlayerLoginEvent;
import com.anthonyhilyard.iceberg.services.Services;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.MinecraftServer;


public class CooperativeAdvancements
{
	public static final String MODID = "cooperativeadvancements";
	public static final Logger LOGGER = LogManager.getLogger(MODID);

	private static boolean skipCriterionEvent = false;

	public static void init()
	{
		CooperativeAdvancementsConfig.register(CooperativeAdvancementsConfig.class, MODID);

		PlayerLoginEvent.EVENT.register(CooperativeAdvancements::onPlayerLogin);
		CriterionEvent.EVENT.register(CooperativeAdvancements::onCriterion);
	}

	/**
	 * Synchronizes the criteria of advancements for two players.
	 * @param first The first player.
	 * @param second The second player.
	 */
	public static void syncCriteria(ServerPlayer first, ServerPlayer second)
	{
		MinecraftServer server = first.getServer();
		Collection<AdvancementHolder> allAdvancements = server.getAdvancements().getAllAdvancements();

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

	private static boolean onSameTeam(ServerPlayer player1, ServerPlayer player2)
	{
		boolean sameVanillaTeam = player1.getTeam() != null && player2.getTeam() != null && player1.getTeam().getName().equals(player2.getTeam().getName());
		boolean sameFTBTeam = false;

		// Handle FTB Teams if it is present.
		if (Services.getPlatformHelper().isModLoaded("ftbteams"))
		{
			try
			{
				sameFTBTeam = (boolean) Class.forName("com.anthonyhilyard.cooperativeadvancements.compat.FTBTeamsHandler").getMethod("onSameTeam", ServerPlayer.class, ServerPlayer.class).invoke(null, new Object[] { player1, player2 });
			}
			catch (Exception e) {}
		}

		return sameVanillaTeam || sameFTBTeam;
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

		if (!CooperativeAdvancementsConfig.getInstance().enabled.get())
		{
			return;
		}
		else
		{
			MinecraftServer server = player.getServer();
			List<ServerPlayer> currentPlayers = server.getPlayerList().getPlayers();

			for (ServerPlayer serverPlayer : currentPlayers)
			{
				if (player != serverPlayer)
				{
					// Only synchronize between team members if the config option is enabled.
					if (CooperativeAdvancementsConfig.getInstance().perTeam.get() && !onSameTeam((ServerPlayer)player, serverPlayer))
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
	public static void onPlayerLogin(ServerPlayer player, MinecraftServer server)
	{
		if (!CooperativeAdvancementsConfig.getInstance().enabled.get())
		{
			return;
		}
		else
		{
			List<ServerPlayer> currentPlayers = server.getPlayerList().getPlayers();

			// Loop through all the currently-connected players and synchronize their advancements.
			for (ServerPlayer serverPlayer : currentPlayers)
			{
				if (player != serverPlayer)
				{
					// Only synchronize between team members if the config option is enabled.
					if (CooperativeAdvancementsConfig.getInstance().perTeam.get() && !onSameTeam(player, serverPlayer))
					{
						continue;
					}

					syncCriteria(player, serverPlayer);
				}
			}
		}
	}
}
