package geni.witherutils.common.world.level.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.WitherAbstractRailingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams.Builder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class RailingBlock extends WitherAbstractRailingBlock {
	
	public RailingBlock(BlockBehaviour.Properties properties)
	{
		this(properties, new AABB(0, 0, 0, 1, 0.0625, 1), new AABB(0, 0, 0, 1, 1, 0.125));
	}

	public RailingBlock(BlockBehaviour.Properties properties, final AABB base_aabb, final AABB railing_aabb)
	{
		super(properties, base_aabb, railing_aabb, 0);
	}
    
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
	{
		return true;
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext)
	{
		return (useContext.getItemInHand().getItem() == asItem()) || super.canBeReplaced(state, useContext);
	}

	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		if (context.getClickedFace() != Direction.UP)
			return null;
		BlockState state = context.getLevel().getBlockState(context.getClickedPos());
		if (state.getBlock() != this)
			state = super.getStateForPlacement(context);
		final Vec3 rhv = context.getClickLocation().subtract(Vec3.atCenterOf(context.getClickedPos()));
		BooleanProperty side = getDirectionProperty(Direction.getNearest(rhv.x, 0, rhv.z));
		return state.setValue(side, true);
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (stack.getItem() != asItem())
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		
		Direction face = hit.getDirection();
		
		if (!face.getAxis().isHorizontal())
			return ItemInteractionResult.sidedSuccess(world.isClientSide());
		
		final Vec3 rhv = hit.getLocation().subtract(Vec3.atCenterOf(hit.getBlockPos()));
		
		if (rhv.multiply(Vec3.atLowerCornerOf(face.getNormal())).scale(2).lengthSqr() < 0.99)
			face = face.getOpposite();
		
		BooleanProperty railing = getDirectionProperty(face);
		
		boolean add = (!state.getValue(railing));
		
		state = state.setValue(railing, add);
		
		if ((!state.getValue(NORTH)) && (!state.getValue(EAST)) && (!state.getValue(SOUTH)) && (!state.getValue(WEST)))
		{
			state = (world.getFluidState(pos).getType() == Fluids.WATER) ? Blocks.WATER.defaultBlockState() : (Blocks.AIR.defaultBlockState());
		}
		world.setBlock(pos, state, 3);
		if (add && !player.isCreative())
		{
			stack.shrink(1);
		}
		return ItemInteractionResult.sidedSuccess(world.isClientSide());
	}

	@Override
	public List<ItemStack> getDrops(BlockState state, Builder builder)
	{
		if (builder.getLevel().isClientSide())
			return Collections.singletonList(ItemStack.EMPTY);
		List<ItemStack> drops = new ArrayList<>();
		int n = (state.getValue(NORTH) ? 1 : 0) + (state.getValue(EAST) ? 1 : 0) + (state.getValue(SOUTH) ? 1 : 0)
				+ (state.getValue(WEST) ? 1 : 0);
		drops.add(new ItemStack(state.getBlock().asItem(), Math.max(n, 1)));
		return drops;
	}
}
