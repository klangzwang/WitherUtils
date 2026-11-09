package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CatwalkBlock extends WitherAbstractBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");
    public static final BooleanProperty CORNER_NORTH_WEST = BooleanProperty.create("corner_north_west");
    public static final BooleanProperty CORNER_NORTH_EAST = BooleanProperty.create("corner_north_east");
    public static final BooleanProperty CORNER_SOUTH_EAST = BooleanProperty.create("corner_south_east");
    public static final BooleanProperty CORNER_SOUTH_WEST = BooleanProperty.create("corner_south_west");
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    // Slab property
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    // Beam connection (nur relevant für Half=Top, true wenn darunter ein SteelBeamBlock ist)
    public static final BooleanProperty BEAMCONN = BooleanProperty.create("beamconn");

    // Zwei separate Shapes für unten und oben
    private static final VoxelShape BOTTOM_SHAPE = Block.box(0, 0, 0, 16, 2, 16);
    private static final VoxelShape TOP_SHAPE = Block.box(0, 14, 0, 16, 16, 16);

    public CatwalkBlock(BlockBehaviour.Properties props)
    {
        super(props);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(NORTH, false)
                .setValue(EAST, false)
                .setValue(SOUTH, false)
                .setValue(WEST, false)
                .setValue(CORNER_NORTH_WEST, false)
                .setValue(CORNER_NORTH_EAST, false)
                .setValue(CORNER_SOUTH_EAST, false)
                .setValue(CORNER_SOUTH_WEST, false)
                .setValue(WATERLOGGED, false)
                .setValue(HALF, Half.BOTTOM)
                .setValue(BEAMCONN, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext context)
    {
        return state.getValue(HALF) == Half.TOP ? TOP_SHAPE : BOTTOM_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext selectionContext)
    {
        return state.getValue(HALF) == Half.TOP ? TOP_SHAPE : BOTTOM_SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter reader, BlockPos pos)
    {
        return state.getValue(HALF) == Half.TOP ? TOP_SHAPE : BOTTOM_SHAPE;
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter worldIn, Entity entityIn)
    {
        super.updateEntityAfterFallOn(worldIn, entityIn);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance)
    {
        if (fallDistance > 1.0F && !level.isClientSide() && level instanceof ServerLevel serverLevel)
        {
            spawnLandingParticles(serverLevel, state, entity);
        }
        super.fallOn(level, state, pos, entity, fallDistance * 0.5F);
    }

    @Override
    public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles)
    {
        spawnLandingParticles(level, state1, entity);
        return true;
    }

    private void spawnLandingParticles(ServerLevel level, BlockState state, Entity entity)
    {
        double x = entity.getX();
        double y = entity.getY() + (state.getValue(HALF) == Half.TOP ? 0.9D : 0.05D);
        double z = entity.getZ();

        // Metallische Funken und Stoß-Partikel an den Füßen des Models
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 14, 0.25, 0.04, 0.25, 0.12);
        level.sendParticles(ParticleTypes.CRIT, x, y, z, 10, 0.2, 0.04, 0.2, 0.1);
        level.sendParticles(ParticleTypes.SMOKE, x, y, z, 8, 0.18, 0.02, 0.18, 0.02);
    }

    @Override
    public FluidState getFluidState(BlockState state)
    {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState newState, LevelAccessor level, BlockPos pos, BlockPos newPos)
    {
        if (state.getValue(WATERLOGGED))
        {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return this.getPanelState(state, level, pos);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidstate.getType() == Fluids.WATER;
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
        {
            state = this.defaultBlockState();
        }

        // Berechne, ob obere oder untere Hälfte platziert werden soll
        Direction direction = context.getClickedFace();
        BlockPos blockpos = context.getClickedPos();
        Half half = direction != Direction.DOWN && (direction == Direction.UP || context.getClickLocation().y - (double)blockpos.getY() <= 0.5D) ? Half.BOTTOM : Half.TOP;

        if(!context.getPlayer().isCrouching() &&
                context.getLevel().getBlockState(blockpos.relative(context.getClickedFace().getOpposite())).getBlock() instanceof SteelPoleBlock ||
                context.getLevel().getBlockState(blockpos.relative(context.getClickedFace().getOpposite())).getBlock() instanceof SteelBeamBlock) {
            half = Half.TOP;
        }

        return this.getPanelState(state.setValue(WATERLOGGED, waterlogged).setValue(HALF, half), context.getLevel(), context.getClickedPos());
    }

    private BlockState getPanelState(BlockState state, LevelAccessor level, BlockPos pos)
    {
        Half currentHalf = state.getValue(HALF);

        // Verbinde nur mit Nachbarn, wenn sie auch ein Catwalk-Block SIND und in DERSELBEN Hälfte liegen
        boolean north = level.getBlockState(pos.north()).getBlock() == this && level.getBlockState(pos.north()).getValue(HALF) == currentHalf;
        boolean east = level.getBlockState(pos.east()).getBlock() == this && level.getBlockState(pos.east()).getValue(HALF) == currentHalf;
        boolean south = level.getBlockState(pos.south()).getBlock() == this && level.getBlockState(pos.south()).getValue(HALF) == currentHalf;
        boolean west = level.getBlockState(pos.west()).getBlock() == this && level.getBlockState(pos.west()).getValue(HALF) == currentHalf;

        boolean cornerNorthWest = north && west && level.getBlockState(pos.north().west()).getBlock() != this;
        boolean cornerNorthEast = north && east && level.getBlockState(pos.north().east()).getBlock() != this;
        boolean cornerSouthEast = south && east && level.getBlockState(pos.south().east()).getBlock() != this;
        boolean cornerSouthWest = south && west && level.getBlockState(pos.south().west()).getBlock() != this;

        boolean hasPoleInArea = false;
        if(north && south) {
            if(level.getBlockState(pos.below().north()).getBlock() instanceof SteelPoleBlock ||
                    level.getBlockState(pos.below().south()).getBlock() instanceof SteelPoleBlock) {
                hasPoleInArea = true;
            }
        }
        if(east && west) {
            if(level.getBlockState(pos.below().east()).getBlock() instanceof SteelPoleBlock ||
                    level.getBlockState(pos.below().west()).getBlock() instanceof SteelPoleBlock) {
                hasPoleInArea = true;
            }
        }

        boolean beamConn = currentHalf == Half.TOP && hasPoleInArea;

        return state.setValue(NORTH, north)
                .setValue(EAST, east)
                .setValue(SOUTH, south)
                .setValue(WEST, west)
                .setValue(CORNER_NORTH_WEST, cornerNorthWest)
                .setValue(CORNER_NORTH_EAST, cornerNorthEast)
                .setValue(CORNER_SOUTH_EAST, cornerSouthEast)
                .setValue(CORNER_SOUTH_WEST, cornerSouthWest)
                .setValue(BEAMCONN, beamConn);
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
        builder.add(WATERLOGGED);
        builder.add(HALF);
        builder.add(BEAMCONN);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState newstate, Direction face)
    {
        // Optionale Anpassung: Wenn es Half.TOP ist, rendert man möglicherweise die untere Fläche nicht weg.
        // Das hängt aber vom genauen Model ab.
        return face == (state.getValue(HALF) == Half.TOP ? Direction.UP : Direction.DOWN);
    }
}