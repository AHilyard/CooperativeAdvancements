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
	public final BooleanValue perTeam;

	public CooperativeAdvancementsConfig(ForgeConfigSpec.Builder build)
	{
		build.comment("Common Configuration").push("options");

		enabled = build.comment(" Enables the entire mod (defaults to true).  Useful option for modpack makers.").define("enabled", true);
		perTeam = build.comment(" Set to true to only share advancements between members of the same team.").define("per_team", false);

		build.pop();
	}
}
