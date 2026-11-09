package geni.witherutils.common.world.fluid;

import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTFluids;
import geni.withercore.init.WUTItems;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluidColdSlush {

    public static final FluidRenderProps RENDER_PROPS = new FluidRenderProps("coldslush_still", "coldslush_flow", "coldslush_overlay");

    private static BaseFlowingFluid.Properties props()
    {
        return new BaseFlowingFluid
        		.Properties(WUTFluids.COLDSLUSH_FLUID_TYPE, WUTFluids.COLDSLUSH, WUTFluids.COLDSLUSH_FLOWING)
        		.block(WUTBlocks.COLDSLUSH::get)
        		.bucket(WUTItems.COLDSLUSH_BUCKET)
                .slopeFindDistance(4)
                .levelDecreasePerBlock(1)
        		.tickRate(10);
    }
    
    public static class Source extends BaseFlowingFluid.Source
    {
        public Source()
        {
            super(props());
        }
    }
    
    public static class Flowing extends BaseFlowingFluid.Flowing
    {
        public Flowing()
        {
            super(props());
        }
    }
}
