package com.anthonyhilyard.cooperativeadvancements;

import java.io.File;

import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.advancements.Advancement;

public class CustomPlayerAdvancements extends PlayerAdvancements {

	private ServerPlayerEntity player;

	public CustomPlayerAdvancements(MinecraftServer server, File progressFileIn, ServerPlayerEntity player)
	{
		super(server, progressFileIn, player);
		this.player = player;
	}

	public boolean grantCriterion(Advancement advancementIn, String criterionKey)
	{
		boolean result = super.grantCriterion(advancementIn, criterionKey);
		
		// If the player successfully gained a criterion, post an event.
		if (result)
		{
			MinecraftForge.EVENT_BUS.post(new CriterionEvent(player, advancementIn, criterionKey));
		}
		return result;
	}

}
