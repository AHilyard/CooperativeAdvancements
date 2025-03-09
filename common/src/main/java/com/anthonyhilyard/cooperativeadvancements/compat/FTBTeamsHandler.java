package com.anthonyhilyard.cooperativeadvancements.compat;

import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.server.level.ServerPlayer;

public class FTBTeamsHandler
{
	public static boolean onSameTeam(ServerPlayer player1, ServerPlayer player2)
	{
		return FTBTeamsAPI.api().getManager().arePlayersInSameTeam(player1.getUUID(), player2.getUUID());
	}
}