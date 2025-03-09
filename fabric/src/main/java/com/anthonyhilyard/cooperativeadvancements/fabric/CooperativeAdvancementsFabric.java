package com.anthonyhilyard.cooperativeadvancements.fabric;

import com.anthonyhilyard.cooperativeadvancements.CooperativeAdvancements;

import net.fabricmc.api.ModInitializer;

public final class CooperativeAdvancementsFabric implements ModInitializer
{
	@Override
	public void onInitialize()
	{
		// Run our common setup.
		CooperativeAdvancements.init();
	}
}
