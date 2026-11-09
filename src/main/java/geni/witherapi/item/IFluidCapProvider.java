package geni.witherapi.item;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public interface IFluidCapProvider {
	
    IFluidHandlerItem provideFluidCapability(ItemStack stack);
}
