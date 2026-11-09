package geni.withercore.mixin;

import geni.witherutils.common.world.level.block.WallSensorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignalGetter.class)
public interface SignalGetterMixin extends BlockGetter {

    /**
     * Checks whether the given block position is the wall block directly behind a LIT WallSensorBlock,
     * or up to 2 blocks below that wall block (behind, behind.below(1), behind.below(2)).
     */
    @Unique
    default boolean witherutils$isPoweredByWallSensorColumn(BlockPos pos)
    {
        for (int dy = 0; dy <= 2; dy++)
        {
            BlockPos wallTopCandidate = pos.above(dy);
            for (Direction facing : Direction.Plane.HORIZONTAL)
            {
                BlockPos sensorPos = wallTopCandidate.relative(facing);
                BlockState sensorState = this.getBlockState(sensorPos);
                if (sensorState.getBlock() instanceof WallSensorBlock
                        && sensorState.getValue(WallSensorBlock.LIT)
                        && sensorState.getValue(WallSensorBlock.FACING) == facing)
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Inject(method = "getDirectSignalTo", at = @At("HEAD"), cancellable = true)
    default void witherutils$onGetDirectSignalTo(BlockPos pos, CallbackInfoReturnable<Integer> cir)
    {
        if (witherutils$isPoweredByWallSensorColumn(pos))
        {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "getSignal", at = @At("HEAD"), cancellable = true)
    default void witherutils$onGetSignal(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir)
    {
        if (witherutils$isPoweredByWallSensorColumn(pos))
        {
            cir.setReturnValue(15);
        }
    }

    @Inject(method = "hasNeighborSignal", at = @At("HEAD"), cancellable = true)
    default void witherutils$onHasNeighborSignal(BlockPos pos, CallbackInfoReturnable<Boolean> cir)
    {
        if (witherutils$isPoweredByWallSensorColumn(pos))
        {
            cir.setReturnValue(true);
        }
    }
}
