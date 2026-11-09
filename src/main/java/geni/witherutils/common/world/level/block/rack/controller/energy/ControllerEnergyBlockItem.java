package geni.witherutils.common.world.level.block.rack.controller.energy;

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
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ControllerEnergyBlockItem extends BlockItem {

    public static final int ENERGY_BAR_RGB = 0xE52222;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    public ControllerEnergyBlockItem(Block blockIn, Properties builder)
    {
        super(blockIn, builder.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return getEnergyStored(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        float current = getEnergyStored(stack);
        float max = ControllerEnergyBlockEntity.ENERGY_CAPACITY;
        return Math.round(13.0F * Math.min(current, max) / max);
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return ENERGY_BAR_RGB;
    }

    public static int getEnergyStored(ItemStack stack)
    {
        IEnergyStorage energyStorage = stack.getCapability(Capabilities.EnergyStorage.ITEM);
        if (energyStorage != null)
        {
            return energyStorage.getEnergyStored();
        }
        ControllerEnergyBlockItemEnergyCapability cap = new ControllerEnergyBlockItemEnergyCapability(
                stack,
                ControllerEnergyBlockEntity.ENERGY_CAPACITY,
                ControllerEnergyBlockEntity.MAX_TRANSFER
        );
        return cap.getEnergyStored();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, context, tooltip, flagIn);

        int stored = getEnergyStored(stack);
        if (stored > 0)
        {
            tooltip.add(Component.literal(ChatFormatting.GOLD + "Energy: " + ChatFormatting.WHITE + FORMATTER.format(stored) + ChatFormatting.RED + " RF"));
        }
        tooltip.add(Component.literal(ChatFormatting.GOLD + "Can Hold: " + ChatFormatting.WHITE + FORMATTER.format(ControllerEnergyBlockEntity.ENERGY_CAPACITY) + ChatFormatting.RED + " RF"));
        tooltip.add(Component.literal(ChatFormatting.GOLD + "Transfer Rate: " + ChatFormatting.WHITE + FORMATTER.format(ControllerEnergyBlockEntity.MAX_TRANSFER) + ChatFormatting.RED + " RF/t"));
    }
}
