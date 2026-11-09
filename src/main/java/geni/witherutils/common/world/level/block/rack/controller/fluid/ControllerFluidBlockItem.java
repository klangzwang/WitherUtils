package geni.witherutils.common.world.level.block.rack.controller.fluid;

import java.text.DecimalFormat;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ControllerFluidBlockItem extends BlockItem {

    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    public ControllerFluidBlockItem(Block blockIn, Properties builder)
    {
        super(blockIn, builder.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        FluidStack fstack = getFluid(stack);
        return !fstack.isEmpty() && fstack.getAmount() > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        FluidStack fstack = getFluid(stack);
        float current = fstack.getAmount();
        float max = ControllerFluidBlockEntity.FLUIDCAPACITY;
        return Math.round(13.0F * Math.min(current, max) / max);
    }

    public static FluidStack getFluid(ItemStack stack)
    {
        IFluidHandler storage = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (storage != null)
        {
            return storage.getFluidInTank(0);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return 0xADD8E6;
    }
	
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, context, tooltip, flagIn);
        
        IFluidHandler storage = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (storage != null)
        {
            FluidStack fs = storage.getFluidInTank(0);
            if (!fs.isEmpty())
            {
                tooltip.add(Component.literal(ChatFormatting.GOLD + "Fluid: " + ChatFormatting.WHITE + fs.getHoverName().getString()));
                tooltip.add(Component.literal(ChatFormatting.GOLD + "Contains: " + ChatFormatting.WHITE + FORMATTER.format(fs.getAmount()) + ChatFormatting.DARK_AQUA + " mB"));
            }
        }
        tooltip.add(Component.literal(ChatFormatting.GOLD + "Can Hold: " + ChatFormatting.WHITE + FORMATTER.format(ControllerFluidBlockEntity.FLUIDCAPACITY) + ChatFormatting.DARK_AQUA + " mB"));
    }
}
