package geni.witherutils.common.world.level.block;

import java.util.List;

import geni.withercore.common.world.level.block.item.ItemBlock;
import geni.withercore.client.tooltip.Tooltips;
import geni.witherutils.common.world.level.block.solar.ISolarPowered;
import geni.witherutils.common.world.level.block.solar.SolarType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

public class SolarPanelBlockItem extends ItemBlock<Block> implements ISolarPowered {

	private final SolarType type;
	
	public SolarPanelBlockItem(Block block, Properties prop, SolarType type)
	{
		super(block, prop);
		this.type = type;
	}
	
	@Override
	public void appendHoverText(ItemStack stack, TooltipContext pContext, List<Component> tooltip, TooltipFlag flag)
	{
		Tooltips.Tooltip.add(stack, pContext, tooltip, flag);
	}

	@Override
	public SolarType getType()
	{
		return type;
	}
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public int getProduction(SolarType type)
	{
		switch(type)
		{
			case BASIC:
				return 2;
			case ADVANCED:
				return 8;
			case ULTRA:
				return 18;
		}
		return 0;
	}
}
