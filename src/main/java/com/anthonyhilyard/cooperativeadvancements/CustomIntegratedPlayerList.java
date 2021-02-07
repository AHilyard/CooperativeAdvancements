package com.anthonyhilyard.cooperativeadvancements;

import java.lang.reflect.Field;
import java.io.File;
import java.util.UUID;
import java.util.Map;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.integrated.IntegratedPlayerList;
import net.minecraft.util.registry.DynamicRegistries.Impl;
import net.minecraft.world.storage.PlayerData;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.world.storage.FolderName;

public class CustomIntegratedPlayerList extends IntegratedPlayerList
{
	private IntegratedServer server;

	public CustomIntegratedPlayerList(IntegratedServer server, Impl p_i231425_2_, PlayerData p_i231425_3_)
	{
		super(server, p_i231425_2_, p_i231425_3_);
		this.server = server;
	}

	@SuppressWarnings("unchecked")
	public PlayerAdvancements getPlayerAdvancements(ServerPlayerEntity player)
	{
		// Unfortunately, have to access the advancements map via reflection.
		try
		{
			Field advancementsField = getClass().getSuperclass().getSuperclass().getDeclaredField("advancements");
			advancementsField.setAccessible(true);

			Map<UUID, PlayerAdvancements> advancements = (Map<UUID, PlayerAdvancements>) advancementsField.get(this);

			UUID uuid = player.getUniqueID();
			PlayerAdvancements playeradvancements = advancements.get(uuid);
			if (playeradvancements == null)
			{
				// Create an instance of our custom class.
				File file1 = this.server.func_240776_a_(FolderName.ADVANCEMENTS).toFile();
				File file2 = new File(file1, uuid + ".json");
				playeradvancements = new CustomPlayerAdvancements(this.server.getDataFixer(), this, this.server.getAdvancementManager(), file2, player);
				advancements.put(uuid, playeradvancements);
			}

			// Forge: don't overwrite active player with a fake one.
			if (!(player instanceof net.minecraftforge.common.util.FakePlayer))
			playeradvancements.setPlayer(player);
			return playeradvancements;
		}
		catch (NoSuchFieldException|IllegalAccessException e)
		{
			// If something bad happened, revert to standard behavior.
			return super.getPlayerAdvancements(player);
		}
	}
}