package geni.witherutils.common.config.common;

import java.util.function.Predicate;

import net.neoforged.neoforge.common.ModConfigSpec;

public class BlockConfig {
    
	private static final Predicate<Object> STRING_PREDICATE = s -> s instanceof String;
	
	public static ModConfigSpec.IntValue COLLECTORRANGE;
	public static ModConfigSpec.BooleanValue GREENHOUSEMOISTURE;
	public static ModConfigSpec.DoubleValue GREENHOUSECHANCE;
	public static ModConfigSpec.IntValue ITEMCONTROLLERCAPACITY;
	public static ModConfigSpec.IntValue FLUIDCONTROLLERCAPACITY;
	
	public BlockConfig(ModConfigSpec.Builder builder)
    {
    	builder.push("blocks");
    	COLLECTORRANGE = builder.comment("The range of the collector in blocks").defineInRange("collectorRange", 8, 1, 32);
    	GREENHOUSEMOISTURE = builder.comment("Whether greenhouse glass requires hydrated farmland below to accelerate crops")
    			.define("greenhouseMoisture", true);
		GREENHOUSECHANCE = builder.comment("Extra Growth Chance for Greenhouse (0.0 = NoChance, 1.0 = Always)")
				.defineInRange("greenhouseChance", 0.5, 0, 1.0);
    	ITEMCONTROLLERCAPACITY = builder.comment("The maximum item capacity of the item rack controller")
    			.defineInRange("itemControllerCapacity", 262144, 64, Integer.MAX_VALUE);
    	FLUIDCONTROLLERCAPACITY = builder.comment("The maximum fluid capacity of the fluid rack controller (mB)")
    			.defineInRange("fluidControllerCapacity", 64000, 1000, Integer.MAX_VALUE);
        builder.pop();
    }

	public static int getItemControllerCapacity()
	{
		return ITEMCONTROLLERCAPACITY != null ? ITEMCONTROLLERCAPACITY.get() : 262144;
	}

	public static int getFluidControllerCapacity()
	{
		return FLUIDCONTROLLERCAPACITY != null ? FLUIDCONTROLLERCAPACITY.get() : 64000;
	}
}
