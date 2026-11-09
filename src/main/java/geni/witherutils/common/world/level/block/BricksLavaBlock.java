package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class BricksLavaBlock extends WitherAbstractBlock {

	public BricksLavaBlock(BlockBehaviour.Properties props)
	{
		super(props.hasPostProcess((bs, br, bp) -> true).emissiveRendering((bs, br, bp) -> true));
		this.registerDefaultState(this.stateDefinition.any().setValue(LIT, Boolean.valueOf(true)));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
    {
    	builder.add(LIT);
    }
    
	@Override
	public int getFireSpreadSpeed(BlockState state, BlockGetter world, BlockPos pos, Direction face)
	{
		return 10;
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
	{
        Block block = state.getBlock();
        if(block != this)
        {
            return block.getLightEmission(state, level, pos);
        }
        return state.getValue(LIT) ? 15 : 2;
	}
	
	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity)
	{
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity) // && !EnchantmentHelper.hasFrostWalker((LivingEntity)entity))
        {
            entity.hurt(level.damageSources().hotFloor(), 1.0F);
        }
        super.stepOn(level, pos, state, entity);
	}
	
	@Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState p_54826_, boolean p_54827_)
    {
        level.scheduleTick(pos, this, 20);
	}
	
	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		if(level.isRainingAt(pos) || level.isRainingAt(pos.west()) || level.isRainingAt(pos.east()) || level.isRainingAt(pos.north()) || level.isRainingAt(pos.south()))
		{
			BlockPos blockpos = pos.above();
	        if (random.nextInt(10) == 0)
	        {
	        	level.playSound((Player)null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
	        }
	        if (random.nextInt(2) == 0)
	        {
	        	level.sendParticles(ParticleTypes.LARGE_SMOKE,
	         		   (double)blockpos.getX() + 0.5D,
	         		   (double)blockpos.getY() + 0.1D,
	         		   (double)blockpos.getZ() + 0.5D,
	         		   8, 0.5D, 0.25D, 0.5D, 0.0D);
	        }
    		level.setBlockAndUpdate(pos, state.setValue(WitherAbstractBlock.LIT, false));
			level.scheduleTick(pos, this, 40);
		}
		else
		{
    		level.setBlockAndUpdate(pos, state.setValue(WitherAbstractBlock.LIT, true));
			level.scheduleTick(pos, this, 500);
		}
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random)
	{
		if(level.isRainingAt(pos) || level.isRainingAt(pos.west()) || level.isRainingAt(pos.east()) || level.isRainingAt(pos.north()) || level.isRainingAt(pos.south()))
			return;
		
        if (random.nextInt(25) == 0)
        {
        	level.playLocalSound((double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, SoundEvents.CAMPFIRE_CRACKLE, SoundSource.BLOCKS, 0.5F + random.nextFloat(), random.nextFloat() * 0.7F + 0.6F, false);
        }
        if (random.nextInt(25) == 0)
        {
            for(int i = 0; i < random.nextInt(1) + 1; ++i)
            {
            	level.addParticle(ParticleTypes.LAVA, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.1D, (double)pos.getZ() + 0.5D, (double)(random.nextFloat() / 2.0F), 5.0E-5D, (double)(random.nextFloat() / 2.0F));
            }
        }
	}
}