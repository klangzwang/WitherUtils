package geni.witherutils.common.world.level.block.fluid;

import java.util.HashMap;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import geni.withercore.common.world.fluid.AbstractFlowingFluid;
import geni.withercore.common.world.fluid.AbstractLiquidBlock;
import geni.withercore.init.WUTEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

public class ColdSlushBlock extends AbstractLiquidBlock {

	private final HashMap<BlockState, BlockState> collisionMap = new HashMap<>();
	private final HashMap<Block, BlockState> anyState = new HashMap<>();

    public ColdSlushBlock(Supplier<? extends FlowingFluid> supplier, Properties props)
    {
        super(supplier, props);
		this.isFlowAway = false;
		this.isFlowLevelAway = false;
		this.possibleFlowDirections = ImmutableList.of(
				Direction.NORTH/*,
				Direction.SOUTH,
				Direction.EAST,
				Direction.WEST,
				Direction.DOWN*/
		);
		addInteractions();
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos)
	{
		return 3;
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos)
	{
		return true;
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		super.animateTick(state, level, pos, random);

		if (random.nextInt(3) == 0) {
			double startX = pos.getX() + random.nextDouble();
			double startZ = pos.getZ() + random.nextDouble();
			int heightOffset = 4 + random.nextInt(3);
			BlockPos overheadPos = pos.above(heightOffset);

			if (level.getBlockState(overheadPos).isAir()) {
				double startY = overheadPos.getY() + 0.5;
				level.addParticle(ParticleTypes.SNOWFLAKE, startX, startY, startZ, 0.0, -0.06, 0.0);
			}
		}

		if (random.nextInt(6) == 0) {
			double x = pos.getX() + random.nextDouble();
			double y = pos.getY() + 0.85;
			double z = pos.getZ() + random.nextDouble();
			level.addParticle(ParticleTypes.WHITE_SMOKE, x, y, z, 0.0, 0.015, 0.0);
		}
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		super.entityInside(state, level, pos, entity);

		if (!(entity instanceof LivingEntity living)) return;

		if (living instanceof Player player) {
			int maxFreeze = player.getTicksRequiredToFreeze() + 60;
			player.setTicksFrozen(maxFreeze);
			player.addEffect(new MobEffectInstance(WUTEffects.FROSTBITE.getDelegate(), 120, 1, false, true, true));
		} else if (isHumanoidOrBiped(living)) {
			if (level.random.nextFloat() < 0.25F) {
				transmuteToFrozenSnowGolem(living, level);
			}
		} else {
			living.setTicksFrozen(Math.min(living.getTicksRequiredToFreeze() + 40, living.getTicksFrozen() + 15));
		}
	}

	public static boolean isHumanoidOrBiped(LivingEntity entity) {

		if (entity instanceof Player) {
			return false;
		}
		return entity instanceof Zombie
				|| entity instanceof AbstractSkeleton
				|| entity instanceof AbstractPiglin
				|| entity instanceof AbstractVillager
				|| entity.getType() == EntityType.DROWNED
				|| entity.getType() == EntityType.HUSK
				|| entity.getType() == EntityType.STRAY
				|| entity.getType() == EntityType.WITHER_SKELETON
				|| entity.getType() == EntityType.PIGLIN_BRUTE
				|| entity.getType() == EntityType.ZOMBIFIED_PIGLIN
				|| entity.getType() == EntityType.WITCH
				|| entity.getType() == EntityType.PILLAGER
				|| entity.getType() == EntityType.VINDICATOR
				|| entity.getType() == EntityType.EVOKER;
	}

	/**
	 * Transmutes a humanoid mob into an immobilized Snow Golem frozen in place.
	 */
	public static void transmuteToFrozenSnowGolem(LivingEntity entity, Level level) {
		if (!(level instanceof ServerLevel serverLevel)) return;

		double x = entity.getX();
		double y = entity.getY();
		double z = entity.getZ();
		float yRot = entity.getYRot();

		serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, x, y + 0.8, z, 35, 0.4, 0.6, 0.4, 0.08);
		serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.4, z, 15, 0.3, 0.3, 0.3, 0.02);
		level.playSound(null, x, y, z, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 0.9F, 0.7F);
		level.playSound(null, x, y, z, SoundEvents.PLAYER_HURT_FREEZE, SoundSource.HOSTILE, 1.0F, 0.5F);

		float damageAmount = 9.0F;

		if (entity.getHealth() > damageAmount) {
			entity.hurt(serverLevel.damageSources().freeze(), damageAmount);
		} else {
			SnowGolem snowGolem = EntityType.SNOW_GOLEM.create(serverLevel);
			if (snowGolem != null) {
				snowGolem.moveTo(x, y, z, yRot, 0.0F);

				snowGolem.setYRot(yRot);
				snowGolem.setYHeadRot(yRot);
				snowGolem.yBodyRot = yRot;

				snowGolem.setNoAi(true);
				snowGolem.setTicksFrozen(snowGolem.getTicksRequiredToFreeze() + 1000);
				snowGolem.setPumpkin(false);
				snowGolem.setCustomName(net.minecraft.network.chat.Component.literal("Frozen Husk"));
				snowGolem.setCustomNameVisible(false);

				entity.discard();
				serverLevel.addFreshEntity(snowGolem);
			}
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldstate, boolean moving)
	{
		super.onPlace(state, level, pos, oldstate, moving);
		level.scheduleTick(pos, this, 10);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random)
	{
		super.tick(state, level, pos, random);
		if(level == null || level.isClientSide)
			return;
		checkForInteraction(level, pos);
		/*level.scheduleTick(pos, this, 10);*/
	}

	public boolean addInteraction(Block preBlock, Block postBlock)
	{
		if(preBlock == null || postBlock == null)
		{
			return false;
		}
		return addInteraction(preBlock.defaultBlockState(), postBlock.defaultBlockState(), true);
	}

	public boolean addInteraction(BlockState pre, BlockState post)
	{
		return addInteraction(pre, post, false);
	}

	public boolean addInteraction(BlockState pre, BlockState post, boolean anyState)
	{
		if(pre == null || post == null)
		{
			return false;
		}
		if(anyState)
		{
			this.anyState.put(pre.getBlock(), post);
		}
		else
		{
			collisionMap.put(pre, post);
		}
		return true;
	}

	public boolean addInteraction(BlockState pre, Block postBlock)
	{
		return addInteraction(pre, postBlock.defaultBlockState(), false);
	}

	public boolean hasInteraction(BlockState state)
	{
		return collisionMap.containsKey(state) || anyState.containsKey(state.getBlock());
	}

	protected void checkForInteraction(Level world, BlockPos pos)
	{
		if(world.getBlockState(pos).getBlock() != this)
		{
			return;
		}
		for(Direction face : Direction.values())
		{
			interactWithBlock(world, pos.relative(face));
		}
		interactWithBlock(world, pos.offset(-1, 0, -1));
		interactWithBlock(world, pos.offset(-1, 0, 1));
		interactWithBlock(world, pos.offset(1, 0, -1));
		interactWithBlock(world, pos.offset(1, 0, 1));
	}

	public BlockState getInteraction(BlockState state)
	{
		if(collisionMap.containsKey(state))
		{
			return collisionMap.get(state);
		}
		return anyState.get(state.getBlock());
	}

	protected void interactWithBlock(Level world, BlockPos pos)
	{
		BlockState state = world.getBlockState(pos);
		if(state.isAir() || state.getBlock() == this)
			return;

		if(hasInteraction(state))
		{
			world.setBlock(pos, getInteraction(state), 3);
		}
		else if(state.isSolidRender(world, pos) && world.isEmptyBlock(pos.relative(Direction.UP)))
		{
			world.setBlock(pos.relative(Direction.UP), Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, world.random.nextInt(3) + 1), 3);
		}
	}

	public void addInteractions()
	{
		addInteraction(Blocks.GRASS_BLOCK, Blocks.DIRT);
		addInteraction(Blocks.WATER.defaultBlockState(), Blocks.ICE);
		addInteraction(Blocks.WATER, Blocks.SNOW);
		addInteraction(Blocks.WATER.defaultBlockState(), Blocks.POWDER_SNOW);
		addInteraction(Blocks.LAVA.defaultBlockState(), Blocks.OBSIDIAN);
		addInteraction(Blocks.TALL_GRASS, Blocks.AIR);
		addInteraction(Blocks.LAVA, Blocks.STONE);
		addInteraction(Blocks.FIRE, Blocks.AIR);
	}
}
