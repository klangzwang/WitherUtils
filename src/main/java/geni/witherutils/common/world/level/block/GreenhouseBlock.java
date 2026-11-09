package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherutils.common.config.common.BlockConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import org.joml.Vector3f;

public class GreenhouseBlock extends WitherAbstractBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty CORNER_NORTH_WEST = BooleanProperty.create("corner_north_west");
    public static final BooleanProperty CORNER_NORTH_EAST = BooleanProperty.create("corner_north_east");
    public static final BooleanProperty CORNER_SOUTH_EAST = BooleanProperty.create("corner_south_east");
    public static final BooleanProperty CORNER_SOUTH_WEST = BooleanProperty.create("corner_south_west");
    public static final BooleanProperty CORNER_ROD = BooleanProperty.create("corner_rod");

    public GreenhouseBlock(BlockBehaviour.Properties props)
	{
		super(props.randomTicks());
		this.registerDefaultState(this.getStateDefinition().any()
				.setValue(NORTH, false)
				.setValue(EAST, false)
				.setValue(SOUTH, false)
				.setValue(WEST, false)
				.setValue(CORNER_NORTH_WEST, false)
				.setValue(CORNER_NORTH_EAST, false)
				.setValue(CORNER_SOUTH_EAST, false)
				.setValue(CORNER_SOUTH_WEST, false)
				.setValue(CORNER_ROD, false));
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean pMovedByPiston) {
        if(level.getBlockState(pos).getBlock() instanceof CropBlock) {
            for(int i = 1; i < 3; i++) {
                if(level.getBlockState(pos.above(i)).isEmpty()) {
                    super.onPlace(state, level, pos.above(i), oldState, pMovedByPiston);
                    break;
                }
            }
        }
        else
            super.onPlace(state, level, pos, oldState, pMovedByPiston);
    }

    private BlockState getPanelState(BlockState state, LevelAccessor level, BlockPos pos)
    {
        boolean north = level.getBlockState(pos.north()).getBlock() == this;
        boolean east = level.getBlockState(pos.east()).getBlock() == this;
        boolean south = level.getBlockState(pos.south()).getBlock() == this;
        boolean west = level.getBlockState(pos.west()).getBlock() == this;
        boolean cornerNorthWest = north && west && level.getBlockState(pos.north().west()).getBlock() != this;
        boolean cornerNorthEast = north && east && level.getBlockState(pos.north().east()).getBlock() != this;
        boolean cornerSouthEast = south && east && level.getBlockState(pos.south().east()).getBlock() != this;
        boolean cornerSouthWest = south && west && level.getBlockState(pos.south().west()).getBlock() != this;

        boolean cornerRod = !level.getBlockState(pos.below()).isViewBlocking(level, pos);

        return state.setValue(NORTH, north)
        		.setValue(EAST, east)
        		.setValue(SOUTH, south)
        		.setValue(WEST, west)
        		.setValue(CORNER_NORTH_WEST, cornerNorthWest)
        		.setValue(CORNER_NORTH_EAST, cornerNorthEast)
        		.setValue(CORNER_SOUTH_EAST, cornerSouthEast)
        		.setValue(CORNER_SOUTH_WEST, cornerSouthWest)
        		.setValue(CORNER_ROD, cornerRod);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos)
    {
        return this.getPanelState(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.getPanelState(this.defaultBlockState(), context.getLevel(), context.getClickedPos());
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
        builder.add(CORNER_ROD);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand) {
        super.randomTick(state, world, pos, rand);

        if(!world.isAreaLoaded(pos, 1))
            return;

        if (world.isClientSide)
            return;

        // 1. Muss echten Himmelsblick haben (kein Fackellicht, keine Höhlen)
        if (world.canSeeSkyFromBelowWater(pos)) {

            // 2. Tageszeit auf dem Server ermitteln (0 bis 24000)
            long dayTime = world.getDayTime() % 24000L;

            // Definition: Tag ist von ca. 0 (Sonnenaufgang/6:00 Uhr) bis 12000 (Sonnenuntergang/18:00 Uhr)
            // Exakter Mittag ist bei 6000 ticks.
            if (dayTime < 0L || dayTime > 12000L)
                return; // Nachts komplett abbrechen (kein Fackellicht!)

            // Sonnenfaktor berechnen: Steigt morgens an, hat bei 6000 (Mittag) den Höchstwert (1.0) und fällt abends auf 0
            float sunFactor = (float) Math.sin(((dayTime) / 12000.0) * Math.PI);

            BlockPos cropPos = pos.below();
            BlockState cropState = world.getBlockState(cropPos);
            Block cropBlock = cropState.getBlock();

            boolean isCrop = cropBlock instanceof CropBlock || cropState.is(BlockTags.CROPS);
            if(!isCrop)
                return;

            BlockPos farmPos = cropPos.below();
            BlockState farmState = world.getBlockState(farmPos);
            Block farmBlock = farmState.getBlock();

            boolean isFarmland = farmBlock instanceof FarmBlock || farmState.is(Blocks.FARMLAND);
            int moisture = 0;
            if(farmState.hasProperty(FarmBlock.MOISTURE))
            {
                moisture = farmState.getValue(FarmBlock.MOISTURE);
            }

            if(BlockConfig.GREENHOUSEMOISTURE.get())
            {
                if(isFarmland && moisture == 0)
                    return;
            }

            if(cropBlock instanceof CropBlock crop)
            {
                final int currentAge = crop.getAge(cropState);
                final int maxAge = crop.getMaxAge();

                if(currentAge < maxAge)
                {
                    float baseChance = BlockConfig.GREENHOUSECHANCE.get().floatValue();
                    float dynamicChance = baseChance * sunFactor * 1.25f;

                    float f = rand.nextFloat();
                    if(CommonHooks.canCropGrow(world, cropPos, cropState, f < dynamicChance))
                    {
                        BlockState nextAgeState = crop.getStateForAge(currentAge + 1);
                        world.setBlock(cropPos, nextAgeState, Block.UPDATE_CLIENTS);
                        world.levelEvent(1505, pos.below(), 0);

                        // Mittagszeit liegt grob zwischen Tick 5000 und 7000 (ca. 11:30 bis 12:30 Uhr In-Game-Zeit)
                        boolean isMidday = dayTime >= 5000L && dayTime <= 7000L;
                        int particleCount = isMidday ? 12 : 5;

                        // Warmer, goldener Sonnenstrahl-Partikel, der nach unten sinkt und ausfadet
                        DustParticleOptions sunRayParticle = new DustParticleOptions(new Vector3f(1.0F, 0.85F, 0.4F), 1.2F);

                        world.sendParticles(
                                sunRayParticle,
                                pos.below().getX() + 0.5D,
                                pos.below().getY() + 0.8D,
                                pos.below().getZ() + 0.5D,
                                particleCount,
                                0.3D, 0.2D, 0.3D,
                                -0.02D
                        );

                        // Dezenter Sound zur Mittagszeit unter Last
                        if (isMidday && rand.nextFloat() < 0.4f) {
                            world.playSound(
                                    null,
                                    pos,
                                    net.minecraft.sounds.SoundEvents.BEACON_AMBIENT,
                                    net.minecraft.sounds.SoundSource.BLOCKS,
                                    0.3F,
                                    1.8F
                            );
                        }
                    }
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return Block.box(0, 0, 0, 16, 2, 16);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return Block.box(0, 0, 0, 16, 2, 16);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) ? true : super.skipRendering(state, adjacentState, side);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext)
    {
        return Block.box(0, 0, 0, 16, 2, 16);
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
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if(!state.is(newState.getBlock()))
        {
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }
}
