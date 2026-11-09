package geni.witherutils.common.world.level.block;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

public class CatwalkBlockItem extends BlockItem {

	public CatwalkBlockItem(Block block, Item.Properties properties)
	{
		super(block, properties);
	}

	@Override
	public InteractionResult useOn(UseOnContext context)
	{
		return InteractionResult.PASS;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
		BlockHitResult blockhitresult1 = blockhitresult.withPosition(blockhitresult.getBlockPos().above());
		InteractionResult interactionresult = super.useOn(new UseOnContext(player, hand, blockhitresult1));
		return new InteractionResultHolder<>(interactionresult, player.getItemInHand(hand));
	}
}
