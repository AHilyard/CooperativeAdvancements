package com.anthonyhilyard.cooperativeadvancements.forge;

import com.anthonyhilyard.cooperativeadvancements.CooperativeAdvancements;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CooperativeAdvancements.MODID)
public final class CooperativeAdvancementsForge
{
	public CooperativeAdvancementsForge(FMLJavaModLoadingContext context)
	{
		// Run our common setup.
		CooperativeAdvancements.init();

		context.registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
	}
}
