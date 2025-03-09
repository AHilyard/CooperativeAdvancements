package com.anthonyhilyard.cooperativeadvancements.config;

import java.util.function.Supplier;

import com.anthonyhilyard.cooperativeadvancements.CooperativeAdvancements;
import com.anthonyhilyard.iceberg.config.IcebergConfig;
import com.anthonyhilyard.iceberg.services.IIcebergConfigSpecBuilder;


public class CooperativeAdvancementsConfig extends IcebergConfig<CooperativeAdvancementsConfig>
{
	public static CooperativeAdvancementsConfig getInstance() { return (CooperativeAdvancementsConfig)configInstances.get(CooperativeAdvancements.MODID); }


	public final Supplier<Boolean> enabled;
	public final Supplier<Boolean> perTeam;

	public CooperativeAdvancementsConfig(IIcebergConfigSpecBuilder build)
	{
		build.comment("Common Configuration").push("options");

		enabled = build.comment(" Enables the entire mod (defaults to true).  Useful option for modpack makers.").add("enabled", true);
		perTeam = build.comment(" Set to true to only share advancements between members of the same team. (Supports vanilla teams or FTB Teams)").add("per_team", false);

		build.pop();
	}
}