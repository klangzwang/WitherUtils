package geni.witherutils.common.world.level.block.rack;

import java.util.ArrayList;
import java.util.List;

import geni.witherapi.block.IBlockMulti;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlockEntity;
import geni.witherutils.common.world.level.block.rack.controller.energy.ControllerEnergyBlock;
import geni.witherutils.common.world.level.block.rack.controller.fluid.ControllerFluidBlock;
import geni.witherutils.common.world.level.block.rack.controller.item.ControllerItemBlock;
import geni.witherutils.common.world.level.block.rack.terminal.TerminalBlock;
import geni.witherutils.common.world.level.block.rack.terminal.TerminalBlockEntity;
import geni.withercore.common.world.level.block.entity.WitherBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import static geni.withercore.common.world.level.block.state.BlockStateProperties.FORMED;

public class MultiBlockHelper {

    public static boolean checkForming(Level level, BlockPos pos) {
        int blockPosX = pos.getX();
        int blockPosY = pos.getY();
        int blockPosZ = pos.getZ();

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                if (checkTheCube(level, blockPosX, blockPosY - y, blockPosZ - x))
                    return true;
                if (checkTheCube(level, blockPosX, blockPosY + y, blockPosZ + x))
                    return true;
                if (checkTheCube(level, blockPosX - x, blockPosY - y, blockPosZ))
                    return true;
                if (checkTheCube(level, blockPosX + x, blockPosY + y, blockPosZ))
                    return true;
                if (checkTheCube(level, blockPosX - x, blockPosY, blockPosZ - y))
                    return true;
                if (checkTheCube(level, blockPosX + x, blockPosY, blockPosZ + y))
                    return true;

                if (checkTheCube(level, blockPosX - 2, blockPosY - y, blockPosZ - x))
                    return true;
                if (checkTheCube(level, blockPosX - 2, blockPosY + y, blockPosZ + x))
                    return true;
                if (checkTheCube(level, blockPosX - x, blockPosY - y, blockPosZ - 2))
                    return true;
                if (checkTheCube(level, blockPosX + x, blockPosY + y, blockPosZ - 2))
                    return true;
                if (checkTheCube(level, blockPosX - x, blockPosY - 2, blockPosZ - y))
                    return true;
                if (checkTheCube(level, blockPosX + x, blockPosY - 2, blockPosZ + y))
                    return true;
            }
        }
        return false;
    }

    private static boolean checkTheCube(Level world, int blockPosX, int blockPosY, int blockPosZ) {
        List<TerminalBlockEntity> terminalList = new ArrayList<>();
        BlockPos.MutableBlockPos mPos = new BlockPos.MutableBlockPos();

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {

                    mPos = mPos.set(x + blockPosX, y + blockPosY, z + blockPosZ);
                    BlockState state = world.getBlockState(mPos);

                    if (x != 0 && x != 2 && y != 0 && y != 2 && z != 0 && z != 2) {
                        if (!(world.getBlockState(mPos).getBlock() instanceof ControllerItemBlock) &&
                                !(world.getBlockState(mPos).getBlock() instanceof ControllerFluidBlock) &&
                                !(world.getBlockState(mPos).getBlock() instanceof ControllerEnergyBlock))
                            return false;
                    } else if (!(state.getBlock() instanceof IBlockMulti)) {
                        return false;
                    } else if (state.getBlock() instanceof TerminalBlock) {
                        boolean xMid = x != 0 && x != 2;
                        boolean yMid = y != 0 && y != 2;
                        boolean zMid = z != 0 && z != 2;

                        Direction facing = state.getValue(BlockStateProperties.FACING);

                        if (xMid && yMid && facing.getAxis() == Direction.Axis.Z
                                || xMid && zMid && facing.getAxis() == Direction.Axis.Y
                                || yMid && zMid && facing.getAxis() == Direction.Axis.X) {
                            BlockEntity te = world.getBlockEntity(mPos);
                            if (te instanceof TerminalBlockEntity) {
                                terminalList.add((TerminalBlockEntity) te);
                            }
                        } else {
                            return false;
                        }
                    } else {
                        BlockEntity te = world.getBlockEntity(mPos);
                        if (te instanceof CaseBlockEntity) {
                            BlockEntity caseBe = ((CaseBlockEntity) te).getPrimaryTerminal();
                            if (caseBe != null)
                                return false;
                        }
                    }
                }
            }
        }

        if (terminalList.isEmpty())
            return false;

        TerminalBlockEntity primaryTerminal = terminalList.get(terminalList.size() - 1);
        terminalList.forEach(terminal -> terminal.accessoryTerminals = new ArrayList<>(terminalList));

        primaryTerminal.multiBlockSize = 3;
        primaryTerminal.multiBlockX = blockPosX;
        primaryTerminal.multiBlockY = blockPosY;
        primaryTerminal.multiBlockZ = blockPosZ;

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 3; z++) {

                    BlockEntity be = world.getBlockEntity(new BlockPos(x + blockPosX, y + blockPosY, z + blockPosZ));

                    if (be instanceof CaseBlockEntity wall) {
                        wall.setPrimaryTerminal(primaryTerminal);
                    } else if (be instanceof TerminalBlockEntity) {
                        BlockState state = world.getBlockState(be.getBlockPos());
                        world.setBlock(be.getBlockPos(), state.setValue(FORMED, primaryTerminal.multiBlockSize > 0),
                                Block.UPDATE_CLIENTS);
                    }
                    if (be != null && world instanceof ServerLevel) {
                        ServerLevel serverLevel = (ServerLevel) world;
                        double dx = x == 0 ? -0.1 : 0.1;
                        double dz = z == 0 ? -0.1 : 0.1;
                        for (int i = 0; i < 5; i++) {
                            serverLevel.sendParticles(ParticleTypes.POOF,
                                    be.getBlockPos().getX() + 0.5,
                                    be.getBlockPos().getY() + 0.5,
                                    be.getBlockPos().getZ() + 0.5, 5,
                                    dx, 0.3, dz,
                                    0);
                        }
                    }
                }
            }
        }
        terminalList.forEach(WitherBlockEntity::setChanged);
        return true;
    }

    public static BlockPos getBehindBlockPos(BlockEntity terminal, Direction facing) {
        return terminal.getBlockPos().relative(facing.getOpposite());
    }

    public static BlockPos getBehindBlockPos(BlockEntity terminal, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        return terminal.getBlockPos().relative(facing.getOpposite());
    }

    public static BlockPos getBehindBlockPos(BlockPos terminalPos, Direction facing) {
        return terminalPos.relative(facing.getOpposite());
    }

    public static BlockPos getBehindBlockPos(BlockPos terminalPos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        return terminalPos.relative(facing.getOpposite());
    }
}
