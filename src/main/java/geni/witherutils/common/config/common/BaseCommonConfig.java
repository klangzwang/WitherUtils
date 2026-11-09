package geni.witherutils.common.config.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BaseCommonConfig {
	
    public final LootConfig LOOT;
	public final SolarConfig SOLAR;
	public final BlockConfig BLOCK;
	
	public BaseCommonConfig(ModConfigSpec.Builder builder)
	{
        LOOT = new LootConfig(builder);
		SOLAR = new SolarConfig(builder);
		BLOCK = new BlockConfig(builder);
	}
}
