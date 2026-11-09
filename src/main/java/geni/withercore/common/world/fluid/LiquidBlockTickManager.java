package geni.withercore.common.world.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Server-side tick coordinator managing:
 * 1. 10-second per-level fluid replenishment (7 -> 6 -> ... -> 0) when harvested by bucket.
 * 2. Gradual dissipation for isFlowLevelAway (no spread, decays to air).
 * 3. 2x spread limit and subsequent dissipation for isFlowAway.
 */
public class LiquidBlockTickManager {

    // --- REGENERATION ENTRIES (10s per level: 7 -> 0) ---
    private static class RegenTask {
        ResourceKey<Level> dimension;
        BlockPos pos;
        int currentLevel;
        int targetLevel;
        int ticksPerStep;
        int tickCounter;

        RegenTask(ResourceKey<Level> dim, BlockPos pos, int startLevel, int targetLevel, int ticksPerStep) {
            this.dimension = dim;
            this.pos = pos;
            this.currentLevel = startLevel;
            this.targetLevel = targetLevel;
            this.ticksPerStep = ticksPerStep;
            this.tickCounter = ticksPerStep;
        }
    }

    // --- DISSIPATION ENTRIES ---
    private static class DissipationTask {
        ResourceKey<Level> dimension;
        BlockPos pos;
        int ticksLeft;
        int stepInterval;

        DissipationTask(ResourceKey<Level> dim, BlockPos pos, int interval) {
            this.dimension = dim;
            this.pos = pos;
            this.stepInterval = interval;
            this.ticksLeft = interval;
        }
    }

    private static final Map<BlockPos, RegenTask> REGEN_MAP = new ConcurrentHashMap<>();
    private static final Map<BlockPos, Integer> FLOW_AWAY_SPREAD_COUNTS = new ConcurrentHashMap<>();
    private static final Map<BlockPos, DissipationTask> DISSIPATION_MAP = new ConcurrentHashMap<>();

    public static void registerRegeneration(Level level, BlockPos pos, int startLevel, int targetLevel, int ticksPerStep) {
        REGEN_MAP.put(pos.immutable(), new RegenTask(level.dimension(), pos.immutable(), startLevel, targetLevel, ticksPerStep));
    }

    public static void registerLevelAway(Level level, BlockPos pos, int interval) {
        DISSIPATION_MAP.put(pos.immutable(), new DissipationTask(level.dimension(), pos.immutable(), interval));
    }

    public static void registerFlowAway(Level level, BlockPos pos) {
        FLOW_AWAY_SPREAD_COUNTS.put(pos.immutable(), 0);
    }

    public static void recordSpread(BlockPos sourcePos) {
        FLOW_AWAY_SPREAD_COUNTS.compute(sourcePos.immutable(), (k, v) -> (v == null ? 1 : v + 1));
    }

    public static boolean isFlowAwaySpreadLimitReached(BlockPos sourcePos) {
        Integer count = FLOW_AWAY_SPREAD_COUNTS.get(sourcePos.immutable());
        return count != null && count >= 2;
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer() == null) return;

        // Process Fluid Regeneration (e.g. 7 -> 6 -> ... -> 0 every 200 ticks = 10s)
        Iterator<Map.Entry<BlockPos, RegenTask>> regenIt = REGEN_MAP.entrySet().iterator();
        while (regenIt.hasNext()) {
            Map.Entry<BlockPos, RegenTask> entry = regenIt.next();
            RegenTask task = entry.getValue();
            ServerLevel serverLevel = event.getServer().getLevel(task.dimension);
            if (serverLevel == null) continue;

            task.tickCounter--;
            if (task.tickCounter <= 0) {
                task.tickCounter = task.ticksPerStep; // Reset for next step

                // Step up (decreasing numeric LEVEL in vanilla convention 7 -> 0)
                if (task.currentLevel > task.targetLevel) {
                    task.currentLevel--;
                    BlockState state = serverLevel.getBlockState(task.pos);

                    if (state.getBlock() instanceof AbstractLiquidBlock liquidBlock) {
                        liquidBlock.setLevel(serverLevel, task.pos, task.currentLevel);
                        serverLevel.playSound(null, task.pos, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 0.5F, 1.2F);
                    } else {
                        // Block was broken or modified externally, cancel task
                        regenIt.remove();
                        continue;
                    }
                }

                // Check if target level (0 = full source) reached
                if (task.currentLevel <= task.targetLevel) {
                    serverLevel.playSound(null, task.pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 1.5F);
                    regenIt.remove();
                }
            }
        }

        // Process Dissipation for FlowAway after 2 spreads
        for (Map.Entry<BlockPos, Integer> spreadEntry : FLOW_AWAY_SPREAD_COUNTS.entrySet()) {
            if (spreadEntry.getValue() >= 2 && !DISSIPATION_MAP.containsKey(spreadEntry.getKey())) {
                // Register for gradual decay
                DISSIPATION_MAP.put(spreadEntry.getKey(), new DissipationTask(Level.OVERWORLD, spreadEntry.getKey(), 40));
            }
        }

        // Process Dissipation Tasks (gradually dropping level height until removed)
        Iterator<Map.Entry<BlockPos, DissipationTask>> dissIt = DISSIPATION_MAP.entrySet().iterator();
        while (dissIt.hasNext()) {
            Map.Entry<BlockPos, DissipationTask> entry = dissIt.next();
            DissipationTask task = entry.getValue();
            ServerLevel serverLevel = event.getServer().getLevel(task.dimension);
            if (serverLevel == null) continue;

            task.ticksLeft--;
            if (task.ticksLeft <= 0) {
                task.ticksLeft = task.stepInterval;
                BlockState state = serverLevel.getBlockState(task.pos);

                if (state.getBlock() instanceof AbstractLiquidBlock && state.hasProperty(BlockStateProperties.LEVEL)) {
                    int curLevel = state.getValue(BlockStateProperties.LEVEL);
                    if (curLevel < 7) {
                        serverLevel.setBlock(task.pos, state.setValue(BlockStateProperties.LEVEL, curLevel + 1), 3);
                    } else {
                        // Completely vanished
                        serverLevel.setBlock(task.pos, Blocks.AIR.defaultBlockState(), 3);
                        FLOW_AWAY_SPREAD_COUNTS.remove(task.pos);
                        dissIt.remove();
                    }
                } else {
                    dissIt.remove();
                }
            }
        }
    }
}
