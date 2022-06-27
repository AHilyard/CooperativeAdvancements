package com.anthonyhilyard.cooperativeadvancements;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;

import com.electronwill.nightconfig.core.Config;

public class CooperativeAdvancementsConfig
{
	public static final ForgeConfigSpec SPEC;
	public static final CooperativeAdvancementsConfig INSTANCE;
	static
	{
		Config.setInsertionOrderPreserved(true);
		Pair<CooperativeAdvancementsConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CooperativeAdvancementsConfig::new);
		SPEC = specPair.getRight();
		INSTANCE = specPair.getLeft();
	}

	public final BooleanValue enabled;

	public CooperativeAdvancementsConfig(ForgeConfigSpec.Builder build)
	{
		build.comment("Client Configuration").push("client").push("options");

		enabled = build.comment(" If advancements should be synchronized between players.  Recommended when playing with friends!").define("enabled", true);

		build.pop().pop();
	}
}