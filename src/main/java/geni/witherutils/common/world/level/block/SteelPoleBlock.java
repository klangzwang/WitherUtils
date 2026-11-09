package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SteelPoleBlock extends WitherBlock
{
    public static final BooleanProperty FACINGDOWN = BooleanProperty.create("facingdown");
    public static final BooleanProperty FOOT = BooleanProperty.create("foot");

    public static final BooleanProperty NORTHCONN = BooleanProperty.create("northconn");
    public static final BooleanProperty EASTCONN  = BooleanProperty.create("eastconn");
    public static final BooleanProperty SOUTHCONN = BooleanProperty.create("southconn");
    public static final BooleanProperty WESTCONN  = BooleanProperty.create("westconn");

    private static final VoxelShape PILLAR_SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D);

    private static final VoxelShape FOOT_FLOOR_BASE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    private static final VoxelShape FOOT_FLOOR_BRACE = Block.box(2.5D, 2.0D, 2.5D, 13.5D, 10.0D, 13.5D);
    private static final VoxelShape FOOT_FLOOR_SHAPE = Shapes.or(FOOT_FLOOR_BASE, FOOT_FLOOR_BRACE);

    private static final VoxelShape FOOT_CEIL_BASE = Block.box(0.0D, 14.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape FOOT_CEIL_BRACE = Block.box(2.5D, 6.0D, 2.5D, 13.5D, 14.0D, 13.5D);
    private static final VoxelShape FOOT_CEIL_SHAPE = Shapes.or(FOOT_CEIL_BASE, FOOT_CEIL_BRACE);

    private static final VoxelShape CONN_NORTH = Block.box(4.0D, 2.0D, 0.0D, 12.0D, 14.0D, 4.0D);
    private static final VoxelShape CONN_EAST  = Block.box(12.0D, 2.0D, 4.0D, 16.0D, 14.0D, 12.0D);
    private static final VoxelShape CONN_SOUTH = Block.box(4.0D, 2.0D, 12.0D, 12.0D, 14.0D, 16.0D);
    private static final VoxelShape CONN_WEST  = Block.box(0.0D, 2.0D, 4.0D, 4.0D, 14.0D, 12.0D);

    private static final VoxelShape[] SHAPES = new VoxelShape[64];

    static
    {
        for (int i = 0; i < 64; i++)
        {
            boolean facingDown = (i & 1) != 0;
            boolean foot       = (i & 2) != 0;
            boolean north      = (i & 4) != 0;
            boolean east       = (i & 8) != 0;
            boolean south      = (i & 16) != 0;
            boolean west       = (i & 32) != 0;

            VoxelShape shape = PILLAR_SHAPE;

            if (foot)
            {
                shape = Shapes.or(shape, facingDown ? FOOT_CEIL_SHAPE : FOOT_FLOOR_SHAPE);
            }
            if (north)
            {
                shape = Shapes.or(shape, CONN_NORTH);
            }
            if (east)
            {
                shape = Shapes.or(shape, CONN_EAST);
            }
            if (south)
            {
                shape = Shapes.or(shape, CONN_SOUTH);
            }
            if (west)
            {
                shape = Shapes.or(shape, CONN_WEST);
            }

            SHAPES[i] = shape;
        }
    }

    public SteelPoleBlock(BlockBehaviour.Properties builder)
    {
        super(builder);
        registerDefaultState(super.defaultBlockState()
                .setValue(FACINGDOWN, false)
                .setValue(FOOT, false)
                .setValue(NORTHCONN, false)
                .setValue(EASTCONN, false)
                .setValue(SOUTHCONN, false)
                .setValue(WESTCONN, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(FACINGDOWN);
        builder.add(FOOT);
        builder.add(NORTHCONN);
        builder.add(EASTCONN);
        builder.add(SOUTHCONN);
        builder.add(WESTCONN);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction facing = context.getClickedFace();
        BlockState state = super.getStateForPlacement(context);

        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (state == null)
        {
            state = defaultBlockState();
        }

        boolean isClickedFaceDown = facing == Direction.DOWN;

        if (player != null)
        {
            state = state.setValue(FOOT, player.isCrouching());
            if (isClickedFaceDown || world.getBlockState(pos.below()).getBlock() instanceof SteelPoleBlock)
            {
                state = state.setValue(FACINGDOWN, true);
            }
        }

        state = state.setValue(NORTHCONN, world.getBlockState(pos.north()).getBlock() instanceof SteelBeamBlock)
                .setValue(EASTCONN,  world.getBlockState(pos.east()).getBlock() instanceof SteelBeamBlock)
                .setValue(SOUTHCONN, world.getBlockState(pos.south()).getBlock() instanceof SteelBeamBlock)
                .setValue(WESTCONN,  world.getBlockState(pos.west()).getBlock() instanceof SteelBeamBlock);

        return state;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos currentPos, BlockPos neighborPos)
    {
        if (direction == Direction.NORTH)
        {
            state = state.setValue(NORTHCONN, neighborState.getBlock() instanceof SteelBeamBlock);
        }
        else if (direction == Direction.EAST)
        {
            state = state.setValue(EASTCONN, neighborState.getBlock() instanceof SteelBeamBlock);
        }
        else if (direction == Direction.SOUTH)
        {
            state = state.setValue(SOUTHCONN, neighborState.getBlock() instanceof SteelBeamBlock);
        }
        else if (direction == Direction.WEST)
        {
            state = state.setValue(WESTCONN, neighborState.getBlock() instanceof SteelBeamBlock);
        }

        return super.updateShape(state, direction, neighborState, level, currentPos, neighborPos);
    }

    private static int getIndex(BlockState state)
    {
        int index = 0;
        if (state.getValue(FACINGDOWN)) index |= 1;
        if (state.getValue(FOOT))       index |= 2;
        if (state.getValue(NORTHCONN))  index |= 4;
        if (state.getValue(EASTCONN))   index |= 8;
        if (state.getValue(SOUTHCONN))  index |= 16;
        if (state.getValue(WESTCONN))   index |= 32;
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
