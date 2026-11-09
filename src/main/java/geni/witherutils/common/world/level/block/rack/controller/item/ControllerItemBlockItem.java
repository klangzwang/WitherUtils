package geni.witherutils.common.world.level.block.rack.controller.item;

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

public class ControllerItemBlockItem extends BlockItem {

    public static final int ITEM_BAR_RGB = 0xFFAA00;
    private static final DecimalFormat FORMATTER = new DecimalFormat("#,###");

    public ControllerItemBlockItem(Block blockIn, Properties builder)
    {
        super(blockIn, builder.stacksTo(1));
    }

    @Override
    public boolean isBarVisible(ItemStack stack)
    {
        return getItemStackStored(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack)
    {
        float current = getItemStackStored(stack);
        float max = ControllerItemBlockEntity.ITEMCAPACITY;
        return Math.round(13.0F * Math.min(current, max) / max);
    }

    @Override
    public int getBarColor(ItemStack stack)
    {
        return ITEM_BAR_RGB;
    }

    public static int getItemStackStored(ItemStack stack)
    {
        ControllerItemBlockItemHandlerCapability handler = new ControllerItemBlockItemHandlerCapability(stack, ControllerItemBlockEntity.ITEMCAPACITY);
        return handler.getStored();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flagIn)
    {
        super.appendHoverText(stack, context, tooltip, flagIn);

        ControllerItemBlockItemHandlerCapability handler = new ControllerItemBlockItemHandlerCapability(stack, ControllerItemBlockEntity.ITEMCAPACITY, context.registries());
        ItemStack contain = handler.getStack(context.registries());
        int stored = handler.getStored();

        if (!contain.isEmpty() && stored > 0)
        {
            tooltip.add(Component.literal(ChatFormatting.GOLD + "Item: " + ChatFormatting.WHITE + contain.getHoverName().getString()));
            tooltip.add(Component.literal(ChatFormatting.GOLD + "Contains: " + ChatFormatting.WHITE + FORMATTER.format(stored) + ChatFormatting.DARK_AQUA + " items"));
        }
        tooltip.add(Component.literal(ChatFormatting.GOLD + "Can Hold: " + ChatFormatting.WHITE + FORMATTER.format(ControllerItemBlockEntity.ITEMCAPACITY) + ChatFormatting.DARK_AQUA + " items"));
    }
}
