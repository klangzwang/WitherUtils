package geni.witherutils.common.world.level.block;

import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.witherutils.common.world.level.block.entity.SolarPanelBlockEntity;
import geni.witherutils.common.world.level.block.solar.SolarType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SolarPanelBlock extends WitherAbstractBlock implements WitherEntityBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty CORNER_NORTH_WEST = BooleanProperty.create("corner_north_west");
    public static final BooleanProperty CORNER_NORTH_EAST = BooleanProperty.create("corner_north_east");
    public static final BooleanProperty CORNER_SOUTH_EAST = BooleanProperty.create("corner_south_east");
    public static final BooleanProperty CORNER_SOUTH_WEST = BooleanProperty.create("corner_south_west");

    private Enum<SolarType> type = SolarType.NONE;

	public SolarPanelBlock(Enum<SolarType> type, Properties props)
	{
		super(props);
		this.registerDefaultState(this.getStateDefinition().any().setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false).setValue(CORNER_NORTH_WEST, false).setValue(CORNER_NORTH_EAST, false).setValue(CORNER_SOUTH_EAST, false).setValue(CORNER_SOUTH_WEST, false));
		this.type = type;
	}

	@Override
	public SolarPanelBlockItem getBlockItem(Item.Properties properties)
	{
		return new SolarPanelBlockItem(this, properties.stacksTo(12), SolarType.getTheType());
	}
	
	public Enum<SolarType> getType()
	{
		return type;
	}
	
    private VoxelShape getShape(BlockState state)
    {
        return Block.box(0, 0, 0, 16, 1, 16);
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext)
    {
    	return Block.box(0, 0, 0, 16, 1, 16);
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn)
    {
    }
    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles)
    {
        return true;
    }
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        if(!level.isClientSide())
        {
            BlockEntity tileEntity = level.getBlockEntity(pos);
            if(tileEntity instanceof SolarPanelBlockEntity)
            {
                ((SolarPanelBlockEntity) tileEntity).updateCount();
            }
        }
    }
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos)
    {
        return this.getSolarState(state, level, pos);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.getSolarState(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
    }
    private BlockState getSolarState(BlockState state, LevelAccessor level, BlockPos pos)
    {
        boolean north = level.getBlockState(pos.north()).getBlock() == this;
        boolean east = level.getBlockState(pos.east()).getBlock() == this;
        boolean south = level.getBlockState(pos.south()).getBlock() == this;
        boolean west = level.getBlockState(pos.west()).getBlock() == this;
        boolean cornerNorthWest = north && west && level.getBlockState(pos.north().west()).getBlock() != this;
        boolean cornerNorthEast = north && east && level.getBlockState(pos.north().east()).getBlock() != this;
        boolean cornerSouthEast = south && east && level.getBlockState(pos.south().east()).getBlock() != this;
        boolean cornerSouthWest = south && west && level.getBlockState(pos.south().west()).getBlock() != this;
        return state.setValue(NORTH, north).setValue(EAST, east).setValue(SOUTH, south).setValue(WEST, west).setValue(CORNER_NORTH_WEST, cornerNorthWest).setValue(CORNER_NORTH_EAST, cornerNorthEast).setValue(CORNER_SOUTH_EAST, cornerSouthEast).setValue(CORNER_SOUTH_WEST, cornerSouthWest);
    }
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(!state.is(newState.getBlock()))
        {
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH);
        builder.add(EAST);
        builder.add(SOUTH);
        builder.add(WEST);
        builder.add(CORNER_NORTH_WEST);
        builder.add(CORNER_NORTH_EAST);
        builder.add(CORNER_SOUTH_EAST);
        builder.add(CORNER_SOUTH_WEST);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return this.getShape(state);
    }
    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return this.getShape(state);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos)
    {
    	if (level.getBlockState(pos.below()).getBlock() != this &&
    			level.getBlockState(pos.above()).getBlock() != this)
    		return true;
    	return false;
    }
    
//    @Override
//    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
//    {
//		Block block = state.getBlock();
//		if(block != this)
//		{
//			return block.getLightEmission(state, level, pos);
//		}
//        return level.getBlockEntity(pos) instanceof SolarPanelBlockEntity solarpanel && solarpanel.isGenerating() ? 15 : 0;
//    }
    
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
        BlockEntity tileEntity = level.getBlockEntity(pos);
        if(!(tileEntity instanceof SolarPanelBlockEntity solarpanel))
        	return;
    	if(!solarpanel.isGenerating())
    		return;		

		double d0 = pos.getX() + 0.5D + (Math.random() - 0.5D) * 0.5D;
		double d1 = pos.getY() + 0.1D;
		double d2 = pos.getZ() + 0.5D + (Math.random() - 0.5D) * 0.5D;

		if(getType() == SolarType.ADVANCED)
			level.addParticle(new DustParticleOptions(new Vec3(0.25, 1.0, 0.25).toVector3f(), 0.6F), d0, d1, d2, 0x47 / 255d, 0x9f / 255d, 0xa3 / 255d);
		else if(getType() == SolarType.ULTRA)
			level.addParticle(new DustParticleOptions(new Vec3(1.0, 0.25, 0.25).toVector3f(), 0.6F), d0, d1, d2, 0x47 / 255d, 0x9f / 255d, 0xa3 / 255d);
		else
			level.addParticle(new DustParticleOptions(new Vec3(0.25, 0.5, 1.0).toVector3f(), 0.6F), d0, d1, d2, 0x47 / 255d, 0x9f / 255d, 0xa3 / 255d);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		if(this.getType() == SolarType.ADVANCED)
			return new SolarPanelBlockEntity.Advanced(pos, state);
		else if(this.getType() == SolarType.ULTRA)
			return new SolarPanelBlockEntity.Ultra(pos, state);
		else
			return new SolarPanelBlockEntity.Basic(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType)
	{
        return (level, blockPos, blockState, be) -> {
            if (be instanceof SolarPanelBlockEntity.Basic tickable)
            {
                if (level.isClientSide())
                    tickable.clientTick();
                else
                    tickable.serverTick();
            }
            if (be instanceof SolarPanelBlockEntity.Advanced tickable)
            {
                if (level.isClientSide())
                    tickable.clientTick();
                else
                    tickable.serverTick();
            }
            if (be instanceof SolarPanelBlockEntity.Ultra tickable)
            {
                if (level.isClientSide())
                    tickable.clientTick();
                else
                    tickable.serverTick();
            }
        };
	}
}
