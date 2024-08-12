package geni.witherutils.core.common.util;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import geni.witherutils.core.common.fakeplayer.WUTFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Vec3i;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class FakePlayerUtil {

    public static void setupFakePlayerForUse(WUTFakePlayer player, BlockPos pos, Direction direction, ItemStack toHold, boolean sneaking)
    {
        player.getInventory().items.set(player.getInventory().selected, toHold);
        float pitch = direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0;
        float yaw = direction == Direction.SOUTH ? 0 : direction == Direction.WEST ? 90 : direction == Direction.NORTH ? 180 : -90;
        Vec3i sideVec = direction.getNormal();
        Axis a = direction.getAxis();
        AxisDirection ad = direction.getAxisDirection();
        double x = a == Axis.X && ad == AxisDirection.NEGATIVE ? -.5 : .5 + sideVec.getX() / 1.9D;
        double y = 0.5 + sideVec.getY() / 1.9D;
        double z = a == Axis.Z && ad == AxisDirection.NEGATIVE ? -.5 : .5 + sideVec.getZ() / 1.9D;
        player.moveTo(pos.getX() + x, pos.getY() + y - player.getEyeHeight(), pos.getZ() + z, yaw, pitch);
        player.setShiftKeyDown(sneaking);
    }

    public static void cleanupFakePlayerFromUse(WUTFakePlayer player, ItemStack resultStack, ItemStack oldStack, Consumer<ItemStack> stackCallback)
    {
//        if (!oldStack.isEmpty()) player.getAttributes().removeAttributeModifiers(oldStack.getAttributeModifiers(EquipmentSlot.MAINHAND));
//        player.getInventory().items.set(player.getInventory().selected, ItemStack.EMPTY);
//        stackCallback.accept(resultStack);
//        if (!player.getInventory().isEmpty()) player.getInventory().dropAll();
        player.setShiftKeyDown(false);
    }

    public static ItemStack rightClickInDirection(WUTFakePlayer player, Level world, BlockPos pos, Direction side, BlockState sourceState)
    {
//        HitResult toUse = rayTrace(player, world, player.getAttributeValue(ForgeMod.BLOCK_REACH.get()));
//        if (toUse == null) return player.getMainHandItem();
//
//        ItemStack itemstack = player.getMainHandItem();
//        if (toUse.getType() == HitResult.Type.ENTITY) {
//            if (processUseEntity(player, world, ((EntityHitResult) toUse).getEntity(), toUse, InteractionType.INTERACT_AT)) return player.getMainHandItem();
//            else if (processUseEntity(player, world, ((EntityHitResult) toUse).getEntity(), null, InteractionType.INTERACT)) return player.getMainHandItem();
//        }
//        else if (toUse.getType() == HitResult.Type.BLOCK) {
//            BlockPos blockpos = ((BlockHitResult) toUse).getBlockPos();
//            BlockState state = world.getBlockState(blockpos);
//            if (state != sourceState && !state.isAir()) {
//                InteractionResult type = player.gameMode.useItemOn(player, world, itemstack, InteractionHand.MAIN_HAND, (BlockHitResult) toUse);
//                if (type == InteractionResult.SUCCESS) return player.getMainHandItem();
//            }
//        }
//
//        if (toUse == null || toUse.getType() == HitResult.Type.MISS) {
//            for (int i = 1; i <= 5; i++) {
//                BlockState state = world.getBlockState(pos.relative(side, i));
//                if (state != sourceState && !state.isAir()) {
//                    player.gameMode.useItemOn(player, world, itemstack, InteractionHand.MAIN_HAND, (BlockHitResult) toUse);
//                    return player.getMainHandItem();
//                }
//            }
//        }
//
//        if (itemstack.isEmpty() && (toUse == null || toUse.getType() == HitResult.Type.MISS)) ForgeHooks.onEmptyClick(player, InteractionHand.MAIN_HAND);
//        if (!itemstack.isEmpty()) player.gameMode.useItem(player, world, itemstack, InteractionHand.MAIN_HAND);
        return player.getMainHandItem();
    }

    public static ItemStack leftClickInDirection(WUTFakePlayer player, Level world, BlockPos pos, Direction side, BlockState sourceState)
    {
//        HitResult toUse = rayTrace(player, world, player.getAttributeValue(ForgeMod.BLOCK_REACH.get()));
//        if (toUse == null) return player.getMainHandItem();
//
//        if (toUse.getType() == HitResult.Type.ENTITY) {
//            if (processUseEntity(player, world, ((EntityHitResult) toUse).getEntity(), null, InteractionType.ATTACK)) return player.getMainHandItem();
//        }
//        else if (toUse.getType() == HitResult.Type.BLOCK) {
//            BlockPos blockpos = ((BlockHitResult) toUse).getBlockPos();
//            BlockState state = world.getBlockState(blockpos);
//            if (state != sourceState && !state.isAir()) {
//                player.gameMode.handleBlockBreakAction(blockpos, Action.START_DESTROY_BLOCK, ((BlockHitResult) toUse).getDirection(), player.level().getMaxBuildHeight(), 0);
//                return player.getMainHandItem();
//            }
//        }
//
//        if (toUse == null || toUse.getType() == HitResult.Type.MISS) {
//            for (int i = 1; i <= 5; i++) {
//                BlockState state = world.getBlockState(pos.relative(side, i));
//                if (state != sourceState && !state.isAir()) {
//                    player.gameMode.handleBlockBreakAction(pos.relative(side, i), Action.START_DESTROY_BLOCK, side.getOpposite(), player.level().getMaxBuildHeight(), 0);
//                    return player.getMainHandItem();
//                }
//            }
//        }

        return player.getMainHandItem();
    }

    public static HitResult traceEntities(WUTFakePlayer player, Vec3 base, Vec3 target, Level world)
    {
        Entity pointedEntity = null;
        HitResult result = null;
        Vec3 vec3d3 = null;
        AABB search = new AABB(base.x, base.y, base.z, target.x, target.y, target.z).inflate(.5, .5, .5);
        List<Entity> list = world.getEntities(player, search, entity -> EntitySelector.NO_SPECTATORS.test(entity) && entity != null && entity.isPickable());
        double d2 = 5;

        for (int j = 0; j < list.size(); ++j) {
            Entity entity1 = list.get(j);

            AABB aabb = entity1.getBoundingBox().inflate(entity1.getPickRadius());
            Optional<Vec3> optVec = aabb.clip(base, target);

            if (aabb.contains(base)) {
                if (d2 >= 0.0D) {
                    pointedEntity = entity1;
                    vec3d3 = optVec.orElse(base);
                    d2 = 0.0D;
                }
            }
            else if (optVec.isPresent()) {
                double d3 = base.distanceTo(optVec.get());

                if (d3 < d2 || d2 == 0.0D) {
                    if (entity1.getRootVehicle() == player.getRootVehicle() && !entity1.canRiderInteract()) {
                        if (d2 == 0.0D) {
                            pointedEntity = entity1;
                            vec3d3 = optVec.get();
                        }
                    }
                    else {
                        pointedEntity = entity1;
                        vec3d3 = optVec.get();
                        d2 = d3;
                    }
                }
            }
        }

        if (pointedEntity != null && base.distanceTo(vec3d3) > 5) {
            pointedEntity = null;
            result = BlockHitResult.miss(vec3d3, null, BlockPos.containing(vec3d3));
        }

        if (pointedEntity != null) {
            result = new EntityHitResult(pointedEntity, vec3d3);
        }

        return result;
    }

    public static boolean processUseEntity(WUTFakePlayer player, Level world, Entity entity, @Nullable HitResult result, InteractionType action)
    {
        if (entity != null) {
            if (player.distanceToSqr(entity) < 36) {
                if (action == InteractionType.INTERACT) {
                    return player.interactOn(entity, InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS;
                }
                else if (action == InteractionType.INTERACT_AT) {
//                    if (EventHooks.onInteractEntityAt(player, entity, result.getLocation(), InteractionHand.MAIN_HAND) != null) return false;
                    return entity.interactAt(player, result.getLocation(), InteractionHand.MAIN_HAND) == InteractionResult.SUCCESS;
                }
                else if (action == InteractionType.ATTACK) {
                    if (entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof Arrow || entity == player) return false;
                    player.attack(entity);
                    return true;
                }
            }
        }
        return false;
    }

    public static HitResult rayTrace(WUTFakePlayer player, Level level, double reachDist)
    {
        Vec3 base = new Vec3(player.getX(), player.getEyeY(), player.getZ());
        Vec3 look = player.getLookAngle();
        Vec3 target = base.add(look.x * reachDist, look.y * reachDist, look.z * reachDist);
        HitResult trace = level.clip(new ClipContext(base, target, Block.OUTLINE, Fluid.NONE, player));
        HitResult traceEntity = traceEntities(player, base, target, level);
        HitResult toUse = trace == null ? traceEntity : trace;

        if (trace != null && traceEntity != null) {
            double d1 = trace.getLocation().distanceTo(base);
            double d2 = traceEntity.getLocation().distanceTo(base);
            toUse = traceEntity.getType() == HitResult.Type.ENTITY && d1 > d2 ? traceEntity : trace;
        }

        return toUse;
    }

    public enum InteractionType {
        INTERACT,
        INTERACT_AT,
        ATTACK
    }
}
