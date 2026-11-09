package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherBlock;
import geni.withercore.init.WUTItems;
import geni.withercore.init.WUTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SteelBeamBlock extends WitherBlock
{
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    public static final BooleanProperty NORTHCONN = BooleanProperty.create("northconn");
    public static final BooleanProperty EASTCONN  = BooleanProperty.create("eastconn");
    public static final BooleanProperty SOUTHCONN = BooleanProperty.create("southconn");
    public static final BooleanProperty WESTCONN  = BooleanProperty.create("westconn");

    public static final IntegerProperty VARIANT = IntegerProperty.create("variant", 0, 2);

    // Base Beam Shapes
    private static final VoxelShape BEAM_Z_AXIS = Block.box(5.0D, 2.0D, 0.0D, 11.0D, 14.0D, 16.0D);
    private static final VoxelShape BEAM_X_AXIS = Block.box(0.0D, 2.0D, 5.0D, 16.0D, 14.0D, 11.0D);

    // Wall Bracket Shapes (at rear/opposite of FACING)
    private static final VoxelShape WALL_PLATE_SOUTH = Block.box(4.5D, 1.0D, 13.0D, 11.5D, 15.0D, 16.0D); // when facing North
    private static final VoxelShape WALL_PLATE_NORTH = Block.box(4.5D, 1.0D, 0.0D, 11.5D, 15.0D, 3.0D);   // when facing South
    private static final VoxelShape WALL_PLATE_WEST  = Block.box(0.0D, 1.0D, 4.5D, 3.0D, 15.0D, 11.5D);   // when facing East
    private static final VoxelShape WALL_PLATE_EAST  = Block.box(13.0D, 1.0D, 4.5D, 16.0D, 15.0D, 11.5D); // when facing West

    // Lateral Connector Shapes
    private static final VoxelShape CONN_NORTH = Block.box(4.0D, 2.0D, 0.0D, 12.0D, 14.0D, 4.0D);
    private static final VoxelShape CONN_EAST  = Block.box(12.0D, 2.0D, 4.0D, 16.0D, 14.0D, 12.0D);
    private static final VoxelShape CONN_SOUTH = Block.box(4.0D, 2.0D, 12.0D, 12.0D, 14.0D, 16.0D);
    private static final VoxelShape CONN_WEST  = Block.box(0.0D, 2.0D, 4.0D, 4.0D, 14.0D, 12.0D);

    private static final VoxelShape[] SHAPES = new VoxelShape[128];

    static
    {
        for (int i = 0; i < 128; i++)
        {
            int facing2D  = i & 3; // 0: South, 1: West, 2: North, 3: East
            boolean wall  = (i & 4) != 0;
            boolean north = (i & 8) != 0;
            boolean east  = (i & 16) != 0;
            boolean south = (i & 32) != 0;
            boolean west  = (i & 64) != 0;

            Direction facing = Direction.from2DDataValue(facing2D);
            boolean isZAxis = facing.getAxis() == Direction.Axis.Z;

            VoxelShape shape = isZAxis ? BEAM_Z_AXIS : BEAM_X_AXIS;

            if (wall)
            {
                switch (facing)
                {
                    case NORTH -> shape = Shapes.or(shape, WALL_PLATE_SOUTH);
                    case SOUTH -> shape = Shapes.or(shape, WALL_PLATE_NORTH);
                    case EAST  -> shape = Shapes.or(shape, WALL_PLATE_WEST);
                    case WEST  -> shape = Shapes.or(shape, WALL_PLATE_EAST);
                    default -> {}
                }
            }
            else
            {
                if (north) shape = Shapes.or(shape, CONN_NORTH);
                if (east)  shape = Shapes.or(shape, CONN_EAST);
                if (south) shape = Shapes.or(shape, CONN_SOUTH);
                if (west)  shape = Shapes.or(shape, CONN_WEST);
            }

            SHAPES[i] = shape;
        }
    }

    public SteelBeamBlock(BlockBehaviour.Properties builder)
    {
        super(builder);
        registerDefaultState(super.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(WALL, false)
                .setValue(NORTHCONN, false)
                .setValue(EASTCONN, false)
                .setValue(SOUTHCONN, false)
                .setValue(WESTCONN, false)
                .setValue(VARIANT, 0)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
        builder.add(WALL);
        builder.add(NORTHCONN);
        builder.add(EASTCONN);
        builder.add(SOUTHCONN);
        builder.add(WESTCONN);
        builder.add(VARIANT);
    }

    @Override
    protected ItemInteractionResult useItemOn(
            net.minecraft.world.item.ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult) {

        if (!stack.isEmpty() && stack.is(WUTItems.HAMMER.get())) {
            if (!level.isClientSide()) {

                if(!state.getValue(WALL) &&
                        !state.getValue(NORTHCONN) &&
                        !state.getValue(WESTCONN) &&
                        !state.getValue(EASTCONN) &&
                        !state.getValue(SOUTHCONN)) {
                    int currentVariant = state.getValue(VARIANT);
                    int nextVariant = (currentVariant + 1) % 3;
                    level.setBlock(pos, state.setValue(VARIANT, nextVariant), 3);

                    float randomPitch = 0.95F + level.random.nextFloat() * 0.10F;

                    level.playSound(
                            null,
                            pos,
                            WUTSounds.HAMMERHIT.get(),
                            SoundSource.BLOCKS,
                            0.75F,
                            randomPitch
                    );

                    if (level instanceof ServerLevel serverLevel) {
                        Vec3 hit = hitResult.getLocation();
                        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state),
                                hit.x, hit.y, hit.z,
                                8,
                                0.1D, 0.1D, 0.1D,
                                0.05D
                        );
                        serverLevel.sendParticles(
                                ParticleTypes.CRIT,
                                hit.x, hit.y, hit.z,
                                5,
                                0.1D, 0.1D, 0.1D,
                                0.1D
                        );
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();
        BlockPos clickedPos = pos.relative(clickedFace.getOpposite());
        BlockState clickedState = level.getBlockState(clickedPos);
        Block clickedBlock = clickedState.getBlock();

        BlockState state = defaultBlockState();
        Direction facing;
        boolean isWall;

        boolean isSteelBeam = clickedBlock instanceof SteelBeamBlock;
        boolean isSteelPole = clickedBlock instanceof SteelPoleBlock;

        if (clickedFace.getAxis().isHorizontal())
        {
            if (!isSteelBeam && !isSteelPole)
            {
                // Rule 2: Clicked against something other than the steel blocks -> WALL is always true
                facing = clickedFace;
                isWall = true;
            }
            else if (isSteelPole)
            {
                // Rule 1: Placed against SteelPoleBlock. If behind the beam is a wall -> WALL = true
                BlockPos behindPos = pos.relative(clickedFace);
                BlockState behindState = level.getBlockState(behindPos);
                boolean wallBehind = isWallBlock(behindState, level, behindPos, clickedFace.getOpposite());

                if (wallBehind)
                {
                    facing = clickedFace.getOpposite();
                    isWall = true;
                }
                else
                {
                    facing = clickedFace;
                    isWall = false;
                }
            }
            else
            {
                // Clicked against SteelBeamBlock
                BlockPos behindPos = pos.relative(clickedFace);
                BlockState behindState = level.getBlockState(behindPos);
                boolean wallBehind = isWallBlock(behindState, level, behindPos, clickedFace.getOpposite());

                if (wallBehind)
                {
                    facing = clickedFace.getOpposite();
                    isWall = true;
                }
                else
                {
                    facing = clickedFace;
                    isWall = false;
                }
            }
        }
        else
        {
            // Clicked top or bottom (floor / ceiling)
            Direction horizontalFacing = context.getHorizontalDirection();
            BlockPos behindPos = pos.relative(horizontalFacing.getOpposite());
            BlockState behindState = level.getBlockState(behindPos);

            if (isWallBlock(behindState, level, behindPos, horizontalFacing))
            {
                facing = horizontalFacing;
                isWall = true;
            }
            else
            {
                facing = horizontalFacing;
                isWall = false;
            }
        }

        state = state.setValue(FACING, facing).setValue(WALL, isWall);

        // Rule 3: Lateral connections with CONN, unless WALL is true
        if (!isWall)
        {
            Direction.Axis beamAxis = facing.getAxis();
            boolean north = (beamAxis != Direction.Axis.Z) && isConnectable(level.getBlockState(pos.north()).getBlock());
            boolean east  = (beamAxis != Direction.Axis.X) && isConnectable(level.getBlockState(pos.east()).getBlock());
            boolean south = (beamAxis != Direction.Axis.Z) && isConnectable(level.getBlockState(pos.south()).getBlock());
            boolean west  = (beamAxis != Direction.Axis.X) && isConnectable(level.getBlockState(pos.west()).getBlock());

            state = state.setValue(NORTHCONN, north)
                    .setValue(EASTCONN, east)
                    .setValue(SOUTHCONN, south)
                    .setValue(WESTCONN, west);
        }
        else
        {
            state = state.setValue(NORTHCONN, false)
                    .setValue(EASTCONN, false)
                    .setValue(SOUTHCONN, false)
                    .setValue(WESTCONN, false);
        }

        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos)
    {
        if (state.getValue(WALL))
        {
            return state.setValue(NORTHCONN, false)
                    .setValue(EASTCONN, false)
                    .setValue(SOUTHCONN, false)
                    .setValue(WESTCONN, false);
        }

        Direction facing = state.getValue(FACING);
        Direction.Axis beamAxis = facing.getAxis();

        // Update lateral connections perpendicular to the beam axis
        if (direction.getAxis().isHorizontal() && direction.getAxis() != beamAxis)
        {
            boolean connects = isConnectable(neighborState.getBlock());
            if (direction == Direction.NORTH)
                state = state.setValue(NORTHCONN, connects);
            else if (direction == Direction.EAST)
                state = state.setValue(EASTCONN, connects);
            else if (direction == Direction.SOUTH)
                state = state.setValue(SOUTHCONN, connects);
            else if (direction == Direction.WEST)
                state = state.setValue(WESTCONN, connects);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    public static boolean isConnectable(Block block)
    {
        return block instanceof SteelBeamBlock || block instanceof SteelPoleBlock;
    }

    public static boolean isWallBlock(BlockState state, BlockGetter level, BlockPos pos, Direction face)
    {
        if (state.isAir()) return false;
        Block block = state.getBlock();
        if (block instanceof SteelBeamBlock || block instanceof SteelPoleBlock) return false;
        return state.isFaceSturdy(level, pos, face) || state.isSolid();
    }

    private static int getIndex(BlockState state)
    {
        int index = state.getValue(FACING).get2DDataValue(); // 0..3 (2 bits)
        if (state.getValue(WALL))      index |= 4;
        if (state.getValue(NORTHCONN)) index |= 8;
        if (state.getValue(EASTCONN))  index |= 16;
        if (state.getValue(SOUTHCONN)) index |= 32;
        if (state.getValue(WESTCONN))  index |= 64;
        return index;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[getIndex(state)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPES[getIndex(state)];
    }
}
