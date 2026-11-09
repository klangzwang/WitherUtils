package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.withercore.common.util.BlockEntityUtil;
import geni.withercore.common.util.SoundUtil;
import geni.witherutils.common.world.item.WrenchItem;
import geni.witherutils.common.world.level.block.entity.FloorSensorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FloorSensorBlock extends WitherAbstractBlock implements WitherEntityBlock {
	
    public static final BooleanProperty COVERED = BooleanProperty.create("covered");

	public FloorSensorBlock(BlockBehaviour.Properties props)
	{
		super(props);
        this.registerDefaultState(this.stateDefinition.any()
        		.setValue(FACING, Direction.NORTH)
        		.setValue(LIT, Boolean.valueOf(false))
        		.setValue(COVERED, Boolean.valueOf(false)));
	}

	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		return false;
	}

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return state.getValue(COVERED) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, LIT, COVERED);
    }

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new FloorSensorBlockEntity(pos, state);
	}

	@Override
	public ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult)
	{
		var hand = pPlayer.getItemInHand(pHand);
		if (!hand.isEmpty()) {
			if(pHitResult.getDirection() == Direction.UP)
			{
				if(!(hand.getItem() instanceof WrenchItem))
				{
					return ItemInteractionResult.FAIL;
				}
				else
				{
					return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
				}
			}
		}

		var te = BlockEntityUtil.getBlockEntity(FloorSensorBlockEntity.class, pLevel, pPos);
		if (te == null)
			return ItemInteractionResult.FAIL;

		if (te instanceof FloorSensorBlockEntity sensor)
		{
			if (te.cover == null) {
				if (hand.getItem() instanceof BlockItem) {
					if (!pLevel.isClientSide) {
						var useOnContext = new UseOnContext(pPlayer, pHand, pHitResult);
						var blockContext = new BlockPlaceContext(useOnContext);
						var block = ((BlockItem) hand.getItem()).getBlock();
						var cover = block.getStateForPlacement(blockContext);
						if (cover != null && !(block instanceof EntityBlock)) {

							if (!cover.isSolid() || !cover.isCollisionShapeFullBlock(pLevel, pPos)) {
								return ItemInteractionResult.FAIL;
							}

							te.cover = cover;
							pLevel.setBlockAndUpdate(pPos, pState.setValue(COVERED, true));
							FloorSensorBlockEntity.sendBlockEntityToClients(te);
							if (!pPlayer.isCreative())
							{
								hand.shrink(1);
							}
							pLevel.playSound(null, pPos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS, 1, 1);
							SoundUtil.playSoundDistrib(pLevel, pPos, SoundEvents.PLAYER_ATTACK_STRONG, 0.75f, 1.0F, false, true);
						}
					}
					return ItemInteractionResult.sidedSuccess(pLevel.isClientSide);
				}
			}
		}
		return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
	}

	@Override
	public boolean useShapeForLightOcclusion(BlockState state) {
		return true;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public boolean hasDynamicLightEmission(BlockState state) {
		return true;
	}

	@Override
	public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction facing)
	{
		return state.getSignal(level, pos, facing);
	}

	@Override
	public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction facing)
	{
		if(!state.getValue(LIT))
			return 0;
		else
			return state.getValue(LIT) && facing == Direction.DOWN ? 15 : 0;
	}

	public static boolean isPowered(BlockState state)
	{
		return state.getValue(LIT);
	}

	@Override
	public boolean isSignalSource(BlockState state)
	{
		return true;
	}

	public static void notifyPower(Level level, BlockPos pos, BlockState state)
	{
		Block block = state.getBlock();
		level.updateNeighborsAt(pos, block);
		for (int dy = 0; dy <= 2; dy++)
		{
			BlockPos target = pos.above(dy);
			level.updateNeighborsAt(target, block);
			for (Direction dir : Direction.values())
			{
				level.updateNeighborsAt(target.relative(dir), block);
			}
		}
	}

	@Override
	public void destroy(LevelAccessor world, BlockPos pos, BlockState state)
	{
		if(isPowered(state))
			notifyPower((Level) world, pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean boolbool)
	{
		if(!boolbool && !state.is(oldState.getBlock()))
		{
			if(state.getValue(LIT))
			{
				this.notifyPower(level, pos, state);
			}
			super.onRemove(state, level, pos, oldState, boolbool);
		}
	}
}
