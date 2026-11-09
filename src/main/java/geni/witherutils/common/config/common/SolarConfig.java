package geni.witherutils.common.config.common;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SolarConfig {

    public static ModConfigSpec.ConfigValue<Boolean> CANCONNECT;
    public static ModConfigSpec.ConfigValue<Integer> RECALCSUNTICK;
    public static ModConfigSpec.ConfigValue<Integer> SOLARBASICINPUTRF;
    public static ModConfigSpec.ConfigValue<Integer> SOLARADVINPUTRF;
    public static ModConfigSpec.ConfigValue<Integer> SOLARULTRAINPUTRF;
    public static ModConfigSpec.ConfigValue<Integer> SOLARBASICOUTPUTRF;
    public static ModConfigSpec.ConfigValue<Integer> SOLARADVOUTPUTRF;
    public static ModConfigSpec.ConfigValue<Integer> SOLARULTRAOUTPUTRF;
    
    public SolarConfig(ModConfigSpec.Builder builder)
    {
    	builder.push("solar");
    	
	    	builder.push("all");
	    	
				builder.push("canConnectTogether");
				CANCONNECT = builder
		                .comment("When enabled, Panels of different kinds can join together as a multi-block.")
		                .define("canConnectTogether", true);
		        builder.pop();
		        
				builder.push("solarRecalcSunTick");
				RECALCSUNTICK = builder
		                .comment("How often (in ticks) the Panels should check the sun's angle.")
		                .define("solarRecalcSunTick", 5 * 20);
		        builder.pop();
		        
		    builder.pop();

	    	builder.push("basic");
	    	
				builder.push("basicInputRf");
				SOLARBASICINPUTRF = builder
		                .comment("Energy generated per TICK by Basic Panels.")
		                .define("basicInputRf", 4);
		        builder.pop();
		        
				builder.push("basicOutputRf");
				SOLARBASICOUTPUTRF = builder
		                .comment("Energy transfered by Basic Panels.")
		                .define("basicOutputRf", 100);
		        builder.pop();
		        
		    builder.pop();
		    
	    	builder.push("advanced");
	    	
				builder.push("advancedInputRf");
				SOLARADVINPUTRF = builder
		                .comment("Energy generated per TICK by Advanced Panels.")
		                .define("advancedInputRf", 11);
		        builder.pop();
		        
		        builder.push("advancedOutputRf");
				SOLARADVOUTPUTRF = builder
		                .comment("Energy transfered by Advanced Panels.")
		                .define("advancedOutputRf", 400);
		        builder.pop();
		        
		    builder.pop();

	    	builder.push("ultra");
	    	
				builder.push("ultraInputRf");
				SOLARULTRAINPUTRF = builder
		                .comment("Energy generated per TICK by Ultra Panels.")
		                .define("ultraInputRf", 28);
		        builder.pop();
		        
		        builder.push("ultraOutputRf");
				SOLARULTRAOUTPUTRF = builder
		                .comment("Energy transfered by Ultra Panels.")
		                .define("ultraOutputRf", 1000);
		        builder.pop();
		        
		    builder.pop();
		    
        builder.pop();
    }
}
