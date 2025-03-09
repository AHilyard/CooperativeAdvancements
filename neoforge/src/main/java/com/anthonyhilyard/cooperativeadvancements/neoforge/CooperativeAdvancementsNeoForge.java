package com.anthonyhilyard.cooperativeadvancements.neoforge;

import com.anthonyhilyard.cooperativeadvancements.CooperativeAdvancements;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(CooperativeAdvancements.MODID)
public final class CooperativeAdvancementsNeoForge
{
	public CooperativeAdvancementsNeoForge(ModContainer container, IEventBus modBus)
	{
		// Run our common setup.
		CooperativeAdvancements.init();
	}
}

