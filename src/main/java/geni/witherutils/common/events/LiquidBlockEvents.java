package geni.witherutils.common.events;

import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber()
public class LiquidBlockEvents {

    //
    // COLD SLUSH
    //
    /*@SubscribeEvent
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level)) return;
        if (level.isClientSide) return;

        BlockState placedState = event.getPlacedBlock();
        if (!placedState.is(Blocks.ICE)) return;
        if (level.dimension() != Level.NETHER) return;

        BlockPos pos = event.getPos();
        BlockState belowState = level.getBlockState(pos.below());
        if (!belowState.is(Blocks.NETHERRACK)) return;

        int radius = 16;
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-radius, -4, -radius), pos.offset(radius, 4, radius))) {
            if (checkPos.equals(pos)) continue;
            BlockState checkState = level.getBlockState(checkPos);
            if (checkState.is(Blocks.ICE) || checkState.is(WitherTest.COLD_SLUSH_RITUAL_BLOCK.get())) {
                return;
            }
        }

        level.setBlock(pos, WitherTest.COLD_SLUSH_RITUAL_BLOCK.get().defaultBlockState(), 3);
        level.playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 0.6F);
    }*/

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();
        if (level.isClientSide || player.isCreative() || player.isSpectator()) return;

        if (player.tickCount % 10 != 0) return;

        BlockPos playerPos = player.blockPosition();
        int searchRadius = 4;
        boolean nearColdSlush = false;
        double closestDistSq = Double.MAX_VALUE;

        for (BlockPos p : BlockPos.betweenClosed(playerPos.offset(-searchRadius, -2, -searchRadius), playerPos.offset(searchRadius, 2, searchRadius))) {
            if (level.getBlockState(p).is(WUTBlocks.COLDSLUSH.get())) {
                nearColdSlush = true;
                double dSq = playerPos.distSqr(p);
                if (dSq < closestDistSq) closestDistSq = dSq;
            }
        }

        if (nearColdSlush) {
            double distance = Math.sqrt(closestDistSq);
            int freezeAdd = (int) Math.max(10, (5.0 - distance) * 25);
            player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze() + 80, player.getTicksFrozen() + freezeAdd));

            if (distance <= 2.5) {
                player.addEffect(new MobEffectInstance(WUTEffects.FROSTBITE.getDelegate(), 80, 0, false, true, true));
            }
        }
    }

    /*@SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getState().is(WitherTest.COLD_SLUSH_RITUAL_BLOCK.get())) {
            event.setCanceled(true);
        }
    }*/
}
