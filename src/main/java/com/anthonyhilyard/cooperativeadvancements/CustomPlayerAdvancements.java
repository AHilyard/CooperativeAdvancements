package com.anthonyhilyard.cooperativeadvancements;

import java.io.File;

import com.mojang.datafixers.DataFixer;

import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.advancements.Advancement;

public class CustomPlayerAdvancements extends PlayerAdvancements {

	private ServerPlayerEntity player;

	public CustomPlayerAdvancements(DataFixer dataFixer, PlayerList playerList, AdvancementManager advancementManager,
			File progressFile, ServerPlayerEntity player) {
		super(dataFixer, playerList, advancementManager, progressFile, player);
		this.player = player;
	}

	public boolean grantCriterion(Advancement advancementIn, String criterionKey) {
		boolean result = super.grantCriterion(advancementIn, criterionKey);
		
		// If the player successfully gained a criterion, post an event.
		if (result)
		{
			MinecraftForge.EVENT_BUS.post(new CriterionEvent(player, advancementIn, criterionKey));
		}
		return result;
	}

}
