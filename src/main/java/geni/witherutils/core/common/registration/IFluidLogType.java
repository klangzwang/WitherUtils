package geni.witherutils.core.common.registration;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public interface IFluidLogType {

    default boolean isEmpty() {
        return getFluid() == Fluids.EMPTY;
    }

    Fluid getFluid();
}