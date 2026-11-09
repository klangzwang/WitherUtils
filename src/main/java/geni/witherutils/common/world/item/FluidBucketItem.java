package geni.witherutils.common.world.item;

import geni.witherapi.item.IFluidCapProvider;
import geni.withercore.init.WUTItems;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

public class FluidBucketItem extends BucketItem implements IFluidCapProvider {
	
    public FluidBucketItem(Fluid fluid)
    {
        super(fluid, WUTItems.filledBucketProps());
    }

    @Override
    public IFluidHandlerItem provideFluidCapability(ItemStack stack)
    {
        return new FluidBucketWrapper(stack);
    }
}
