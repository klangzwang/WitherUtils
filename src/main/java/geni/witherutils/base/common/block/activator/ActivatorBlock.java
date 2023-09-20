package geni.witherutils.base.common.block.activator;

import geni.witherutils.base.common.base.WitherAbstractBlock;
import geni.witherutils.base.common.init.WUTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ActivatorBlock extends WitherAbstractBlock implements EntityBlock {

	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	private static final VoxelShape BASE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 12.0D, 12.0D);
    
	public ActivatorBlock(BlockBehaviour.Properties props)
	{
		super(props);
	    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
		this.setHasTooltip();
	    this.setHasScreenExtra();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
	{
		return BASE;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
	{
		builder.add(FACING, LIT);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		Direction facing = context.getClickedFace();
		return this.defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
	{
        return level.getBlockState(pos.relative(state.getValue(FACING).getOpposite())).getBlock() != this;
	}
	
	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
	{
        Block block = state.getBlock();
        if(block != this)
        {
            return block.getLightEmission(state, level, pos);
        }
        return state.getValue(LIT) ? 6 : 0;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if(state.getValue(LIT))
		{
			double d1 = (double) pos.getX() + random.nextDouble();
			double d2 = (double) pos.getY() + random.nextDouble();
			double d3 = (double) pos.getZ() + random.nextDouble();
			if(random.nextFloat() < 0.25F)
			{
				level.addParticle(new DustParticleOptions(DustParticleOptions.REDSTONE_PARTICLE_COLOR, 1.0F), d1, d2, d3, 0.0D, 0.1D, 0.0D);
			}
		}
	}

    @Override
    public boolean isSignalSource(BlockState state)
    {
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
        {
        	ActivatorBlockEntity activator = (ActivatorBlockEntity) level.getExistingBlockEntity(pos);
        	if(state.getValue(FACING) != facing)
        		return 0;
        	return activator.power > 0 ? activator.power : 0;
        }
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new ActivatorBlockEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type)
	{
		return createTickerHelper(type, WUTEntities.ACTIVATOR.get(), ActivatorBlockEntity::tick);
	}
}