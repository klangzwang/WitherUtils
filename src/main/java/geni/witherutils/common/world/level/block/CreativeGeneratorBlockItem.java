package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.item.ItemBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CreativeGeneratorBlockItem extends ItemBlock<Block> {

	public CreativeGeneratorBlockItem(Block blockIn, Properties builder)
	{
		super(blockIn, builder);
	}

	@Override
	public boolean isFoil(ItemStack pStack)
	{
		return true;
	}
}
