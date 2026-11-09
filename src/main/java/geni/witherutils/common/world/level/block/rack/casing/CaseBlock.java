package geni.witherutils.common.world.level.block.rack.casing;

import geni.withercore.common.world.level.block.state.BlockStateProperties;
import geni.withercore.common.world.level.block.state.MultiBlockState;
import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.withercore.common.util.BlockEntityUtil;
import geni.witherapi.block.IBlockMulti;
import geni.witherutils.common.world.level.block.rack.terminal.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import static geni.withercore.common.world.level.block.state.BlockStateProperties.FORMED;
import static geni.withercore.common.world.level.block.state.BlockStateProperties.MBSTATE;

public class CaseBlock extends WitherAbstractBlock implements IBlockMulti, WitherEntityBlock {
	
    public CaseBlock(BlockBehaviour.Properties props)
	{
		super(props);
        registerDefaultState(defaultBlockState().setValue(BlockStateProperties.MBSTATE, MultiBlockState.NONE));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand interactionHand, BlockHitResult hit) {
        return ItemInteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.FAIL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.MBSTATE);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack)
    {
        super.setPlacedBy(level, pos, state, par5EntityLiving, iStack);
        if (!level.isClientSide && TerminalBlockEntity.checkForming(level, pos)) {};
    }

	@Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock() && !world.isClientSide)
        	BlockEntityUtil.getBlockEntityAt(world, pos, CaseBlockEntity.class).ifPresent(CaseBlockEntity::onBlockBreak);
        super.onRemove(state, world, pos, newState, isMoving);
    }

	public static boolean isFormed(BlockState state)
    {
        if (state.hasProperty(MBSTATE))
        {
            return state.getValue(MBSTATE) != MultiBlockState.NONE;
        }
        else if (state.hasProperty(FORMED))
        {
            return state.getValue(FORMED);
        }
        else
        {
            return false;
        }
    }
    
	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos)
	{
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side)
	{
		return adjacentBlockState.getBlock() == this;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getShadeBrightness(BlockState state, BlockGetter worldIn, BlockPos pos)
	{
		return 1.0F;
	}
	
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new CaseBlockEntity(pPos, pState);
    }
}
