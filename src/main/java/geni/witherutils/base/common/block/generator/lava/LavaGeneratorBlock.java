package geni.witherutils.base.common.block.generator.lava;

import geni.witherutils.base.common.base.WitherAbstractBlock;
import geni.witherutils.base.common.init.WUTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class LavaGeneratorBlock extends WitherAbstractBlock implements EntityBlock {
	
	public LavaGeneratorBlock(BlockBehaviour.Properties props)
	{
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(false)));
		this.setHasSoulBankSlot();
		this.setHasScreenExtra();
		this.setHasTooltip();
	}
	
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, LIT);
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
	{
        Block block = state.getBlock();
        if(block != this)
        {
            return block.getLightEmission(state, level, pos);
        }
        return state.getValue(LIT) ? 7 : 0;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new LavaGeneratorBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, WUTEntities.LAVA_GENERATOR.get(), LavaGeneratorBlockEntity::tick);
	}
}