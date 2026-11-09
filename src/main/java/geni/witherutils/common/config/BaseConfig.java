package geni.witherutils.common.config;

import org.apache.commons.lang3.tuple.Pair;

import geni.witherutils.common.config.client.BaseClientConfig;
import geni.witherutils.common.config.common.BaseCommonConfig;
import net.neoforged.neoforge.common.ModConfigSpec;

public class BaseConfig {
    
    public static final BaseCommonConfig COMMON;
    public static final ModConfigSpec COMMON_SPEC;

    public static final BaseClientConfig CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;

    static {
        
        Pair<BaseCommonConfig, ModConfigSpec> commonSpecPair = new ModConfigSpec.Builder().configure(BaseCommonConfig::new);
        COMMON = commonSpecPair.getLeft();
        COMMON_SPEC = commonSpecPair.getRight();

        Pair<BaseClientConfig, ModConfigSpec> clientSpecPair = new ModConfigSpec.Builder().configure(BaseClientConfig::new);
        CLIENT = clientSpecPair.getLeft();
        CLIENT_SPEC = clientSpecPair.getRight();
    }
}
