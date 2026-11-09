package geni.witherutils.common.config.common;

import java.util.function.Predicate;

import net.neoforged.neoforge.common.ModConfigSpec;

public class LootConfig {

	private static final Predicate<Object> STRING_PREDICATE = s -> s instanceof String;

	public LootConfig(ModConfigSpec.Builder builder)
    {
    	builder.push("loot");
        builder.pop();
    }
}
