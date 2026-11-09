package geni.witherutils.common.plugin;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public record SlicedConcreteRecipe(Block base, Block crusher, ItemStack input, ItemStack output) {
}
