package geni.witherutils.common.world.level.block;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;

import geni.withercore.init.WUTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SoulFireBlock extends Block {

    public static final int MAX_AGE = 15;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_15;
    private final Map<BlockState, VoxelShape> shapesCache;
    
    protected static final float AABB_OFFSET = 1.0F;
    protected static final VoxelShape DOWN_AABB = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    
	public SoulFireBlock(Properties props)
	{
		super(props);
		
        this.registerDefaultState(
                this.stateDefinition
                    .any()
                    .setValue(AGE, Integer.valueOf(0))
            );
            this.shapesCache = ImmutableMap.copyOf(
                this.stateDefinition
                    .getPossibleStates()
                    .stream()
                    .filter(p_53497_ -> p_53497_.getValue(AGE) == 0)
                    .collect(Collectors.toMap(Function.identity(), SoulFireBlock::calculateShape))
            );
	}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(AGE);
    }
    
	@Override
	protected void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom)
	{
		pLevel.scheduleTick(pPos, this, getFireTickDelay(pLevel.random));
        if (!canSurvive(pState, pLevel, pPos))
        {
            pLevel.removeBlock(pPos, false);
        }
		
        int i = pState.getValue(AGE);
        if (pRandom.nextFloat() < 0.2F + (float)i * 0.03F)
        {
            pLevel.removeBlock(pPos, false);
        }
        else
        {
            int j = Math.min(15, i + pRandom.nextInt(3) / 2);
            if (i != j)
            {
                pState = pState.setValue(AGE, Integer.valueOf(j));
                pLevel.setBlock(pPos, pState, 4);
            }
        }
	}
	
    private static VoxelShape calculateShape(BlockState p_53491_)
    {
        VoxelShape voxelshape = Shapes.empty();
        return voxelshape.isEmpty() ? DOWN_AABB : voxelshape;
    }

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext)
	{
        return getState(pContext.getLevel(), pContext.getClickedPos());
	}
    public static BlockState getState(BlockGetter pReader, BlockPos pPos)
    {
        return WUTBlocks.SOULFIRE.get().defaultBlockState();
    }

    @Override
    protected BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pNeighborPos)
    {
        return this.canSurvive(pState, pLevel, pCurrentPos)
                ? this.getStateWithAge(pLevel, pCurrentPos, pState.getValue(AGE))
                : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext)
    {
        return this.shapesCache.get(pState.setValue(AGE, Integer.valueOf(0)));
    }
    
    @Override
    protected boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos)
    {
        BlockPos blockpos = pPos.below();
        return pLevel.getBlockState(blockpos).isFaceSturdy(pLevel, blockpos, Direction.UP);
    }
    
    private BlockState getStateWithAge(LevelAccessor pLevel, BlockPos pPos, int pAge)
    {
        BlockState blockstate = getState(pLevel, pPos);
        return blockstate.is(WUTBlocks.SOULFIRE.get()) ? blockstate.setValue(AGE, Integer.valueOf(pAge)) : blockstate;
    }

    @Override
    public void animateTick(BlockState pState, Level level, BlockPos pos, RandomSource r)
    {
		for(Direction facing : Direction.values())
		{
			Direction.Axis axis = facing.getAxis();
			
			double d3 = axis == Direction.Axis.X ? facing.getStepX() * 0.02D : -facing.getStepX() * 0.02D;
			double d4 = axis == Direction.Axis.Z ? facing.getStepZ() * 0.02D : -facing.getStepZ() * 0.02D;
			
			for(int i = 0; i < 10; i++)
			{
				level.addParticle(ParticleTypes.SMOKE,
						pos.getX() + 0.5D + (facing == Direction.EAST ? 0.7D * r.nextFloat() : facing == Direction.WEST ? -0.7D * r.nextFloat() : 0.0D),
						pos.getY() + 0.4D * r.nextFloat(),
						pos.getZ() + 0.5D + (facing == Direction.SOUTH ? 0.7D * r.nextFloat() : facing == Direction.NORTH ? -0.7D * r.nextFloat() : 0.0D),
						d3, 0.04D, d4);
			}

			level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
					pos.getX() + r.nextDouble(),
					pos.getY() + 0.8 + r.nextDouble(),
					pos.getZ() + r.nextDouble(),
					0.0D, 0.09D, 0.0D);
			
			level.addParticle(ParticleTypes.SMOKE,
            		(double)pos.getX() + 0.5D + level.random.nextDouble() / 5.0D * (double)(level.random.nextBoolean() ? 1 : -1),
            		(double)pos.getY() + 1.8D,
            		(double)pos.getZ() + 0.5D + level.random.nextDouble() / 5.0D * (double)(level.random.nextBoolean() ? 1 : -1),
					0.0D, 0.01D, 0.0D);
            
    		if(r.nextDouble() < 0.1D)
    		{
    			level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F, false);
    			level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
    		}
		}
    }
    
    @Override
    protected void entityInside(BlockState pState, Level pLevel, BlockPos pPos, Entity pEntity)
    {
        if (!pEntity.fireImmune()) {
            pEntity.setRemainingFireTicks(pEntity.getRemainingFireTicks() + 1);
            if (pEntity.getRemainingFireTicks() == 0) {
                pEntity.igniteForSeconds(8.0F);
            }
        }

        pEntity.hurt(pLevel.damageSources().inFire(), 0);
        super.entityInside(pState, pLevel, pPos, pEntity);
    }
    
    @Override
    protected void spawnDestroyParticles(Level pLevel, Player pPlayer, BlockPos pPos, BlockState pState)
    {
    }
    
    @Override
    public BlockState playerWillDestroy(Level pLevel, BlockPos pPos, BlockState pState, Player pPlayer)
    {
        if (!pLevel.isClientSide())
        {
            pLevel.levelEvent(null, 1009, pPos, 0);
        }
    	return super.playerWillDestroy(pLevel, pPos, pState, pPlayer);
    }
    
    public static boolean canBePlacedAt(Level pLevel, BlockPos pPos, Direction pDirection)
    {
        BlockState blockstate = pLevel.getBlockState(pPos);
        return blockstate.isAir();
    }
    
    @Override
    protected void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pMovedByPiston)
    {
    	super.onPlace(pState, pLevel, pPos, pOldState, pMovedByPiston);
        pLevel.scheduleTick(pPos, this, getFireTickDelay(pLevel.random));
    }
    private static int getFireTickDelay(RandomSource pRandom)
    {
        return 30 + pRandom.nextInt(10);
    }
}
