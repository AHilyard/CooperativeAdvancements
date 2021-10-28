package com.anthonyhilyard.cooperativeadvancements;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = CooperativeAdvancements.MODID)
public class CooperativeAdvancementsConfig implements ConfigData
{
	@ConfigEntry.Gui.Excluded
	public static CooperativeAdvancementsConfig INSTANCE;

	public static void init()
	{
		AutoConfig.register(CooperativeAdvancementsConfig.class, JanksonConfigSerializer::new);
		INSTANCE = AutoConfig.getConfigHolder(CooperativeAdvancementsConfig.class).getConfig();
	}

	@Comment("If advancements should be synchronized between players.  Recommended when playing with friends!")
	public boolean enabled = true;
}
