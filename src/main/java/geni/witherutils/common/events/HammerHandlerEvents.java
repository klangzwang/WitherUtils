package geni.witherutils.common.events;

import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.item.SteelHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HammerHandlerEvents {

    @SubscribeEvent
    public void onHammerSmash(BlockEvent.BreakEvent event) {

        Player player = event.getPlayer();
        Level level = player.level();
        BlockPos pos = event.getPos();
        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        BlockState state = level.getBlockState(pos);

        final boolean isHoldingHammer = heldStack.getItem() == WUTItems.HAMMER.get();
        if (isHoldingHammer)
        {
            if (level.isClientSide || level.getServer() == null || player.isCreative())
                return;

            /*SteelHammerItem hammer = (SteelHammerItem) heldStack.getItem();*/

            if(state.getBlock() == Blocks.BLACK_CONCRETE ||
                    state.getBlock() == Blocks.GRAY_CONCRETE ||
                    state.getBlock() == Blocks.WHITE_CONCRETE) {

                onHammerSmashSlicer(level, pos, state);
            }
            else {

                onHammerUncraft(player, level, pos, state);
            }
        }
    }

    //
    // UNCRAFT
    //
    public void onHammerUncraft(Player player, Level level, BlockPos pos, BlockState state)
    {
        Recipe<?> match = findMatchingRecipe(level, new ItemStack(state.getBlock()));
        if (match != null && !player.getCooldowns().isOnCooldown(player.getItemInHand(InteractionHand.MAIN_HAND).getItem()))
        {
            if (uncraftRecipe(level, pos, match))
            {
                hammerSmash(level, player, pos, match);
            }
        }
    }

    public Recipe<?> findMatchingRecipe(Level world, ItemStack dropMe)
    {
        Collection<RecipeHolder<?>> list = world.getServer().getRecipeManager().getRecipes();
        for(RecipeHolder<?> recipe : list)
        {
            if(recipe.value().getType() == RecipeType.CRAFTING)
            {
                if(recipeMatches(world, dropMe, recipe.value()))
                {
                    return recipe.value();
                }
            }
        }
        return null;
    }

    public boolean recipeMatches(Level level, ItemStack stack, Recipe<?> recipe)
    {
        if(stack.isEmpty() || recipe == null || recipe.getResultItem(level.registryAccess()).isEmpty() || recipe.getResultItem(level.registryAccess()).getCount() > stack.getCount())
        {
            return false;
        }
        return stack.getItem() == recipe.getResultItem(level.registryAccess()).getItem() && ItemStack.matches(stack, recipe.getResultItem(level.registryAccess()));
    }

    public boolean uncraftRecipe(Level level, BlockPos pos, Recipe<?> match)
    {
        List<ItemStack> result = match.getIngredients().stream().flatMap(ingredient -> Arrays.stream(ingredient.getItems()).filter(stack -> !stack.hasCraftingRemainingItem()).findAny().map(Stream::of).orElseGet(Stream::empty)).collect(Collectors.toList());
        return !result.isEmpty();
    }

    public void hammerSmash(Level level, Player player, BlockPos pos, Recipe<?> match)
    {
        List<ItemStack> result = match.getIngredients().stream().flatMap(ingredient -> Arrays.stream(ingredient.getItems()).filter(stack -> !stack.hasCraftingRemainingItem()).findAny().map(Stream::of).orElseGet(Stream::empty)).collect(Collectors.toList());
        for(ItemStack r : result)
        {
            ItemStack itemstack = r.copy();
            double d0 = level.random.nextFloat() * 0.5F + 0.25D;
            double d1 = level.random.nextFloat() * 0.5F + 0.25D;
            double d2 = level.random.nextFloat() * 0.5F + 0.25D;
            ItemEntity entityitem = new ItemEntity(level, pos.getX() + d0, pos.getY() + d1, pos.getZ() + d2, itemstack);
            entityitem.setDefaultPickUpDelay();
            entityitem.hurt(level.damageSources().inFire(), -100);
            entityitem.setRemainingFireTicks(10);
            level.destroyBlock(pos, false);
            level.addFreshEntity(entityitem);
            player.getCooldowns().addCooldown(player.getItemInHand(InteractionHand.MAIN_HAND).getItem(), 12);
            player.getMainHandItem().setDamageValue(player.getMainHandItem().getDamageValue() + 5);
            if(player.getMainHandItem().getDamageValue() >= player.getMainHandItem().getMaxDamage())
            {
                player.getMainHandItem().shrink(1);
            }
        }
    }

    //
    // SLICER
    //
    public void onHammerSmashSlicer(Level level, BlockPos pos, BlockState state)
    {
        spawnEffect(level, pos, state);

        for(int j = 0; j < 8; j++)
        {
            if(state.getBlock() == Blocks.BLACK_CONCRETE)
                spawnCompletionItem(level, pos, new ItemStack(WUTBlocks.SLICEDCONCRETEBLACK.get()), 1);
            else if(state.getBlock() == Blocks.GRAY_CONCRETE)
                spawnCompletionItem(level, pos, new ItemStack(WUTBlocks.SLICEDCONCRETEGRAY.get()), 1);
            else if(state.getBlock() == Blocks.WHITE_CONCRETE)
                spawnCompletionItem(level, pos, new ItemStack(WUTBlocks.SLICEDCONCRETEWHITE.get()), 1);
        }
    }
    public void spawnCompletionItem(Level level, BlockPos pos, ItemStack stack, int amount)
    {
        ItemEntity i = new ItemEntity(level, 0, 0, 0, stack);
        double variance = 0.05F * 4;
        i.setPos(pos.getX(), pos.getY() + 1, pos.getZ());
        i.setDeltaMovement(Mth.nextDouble(level.random, -variance, variance), 2 / 20F, Mth.nextDouble(level.random, -variance, variance));
        i.setUnlimitedLifetime();
        level.addFreshEntity(i);
    }
    public void spawnEffect(Level level, BlockPos pos, BlockState state)
    {
        for(int i = 0; i < 1; i++)
        {
            level.levelEvent(2001, pos, Block.getId(state.getBlock().defaultBlockState()));
            if(level instanceof ServerLevel serverlevel)
            {
                for(int j = 0; j < 20; ++j)
                {
                    serverlevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Block.byItem(state.getBlock().asItem()))),
                            (double)pos.getX() + 0.5D,
                            (double)pos.getY() + 0.7D,
                            (double)pos.getZ() + 0.5D, 3,
                            ((double)level.random.nextFloat() - 0.5D) * 0.08D,
                            ((double)level.random.nextFloat() - 0.5D) * 0.08D,
                            ((double)level.random.nextFloat() - 0.5D) * 0.08D,
                            0.15F);
                }
                for(int j = 0; j < 20; ++j)
                {
                    serverlevel.sendParticles(ParticleTypes.EXPLOSION,
                            (double)pos.getX() + 0.5D,
                            (double)pos.getY() + 0.7D,
                            (double)pos.getZ() + 0.5D, 3,
                            ((double)level.random.nextFloat() - 0.5D) * 0.08D,
                            ((double)level.random.nextFloat() - 0.5D) * 0.08D,
                            ((double)level.random.nextFloat() - 0.5D) * 0.08D,
                            0.15F);
                }
            }
        }
    }

    //
    // SLAM
    //
    /*@SubscribeEvent
    public void onPlayerSlam(PlayerTickEvent.Post event) {

        Player player = event.getEntity();
        Level level = player.level();

        if (player.getPersistentData().getBoolean("WUT_HammerSlam"))
        {
            player.setPose(Pose.SITTING);

            double currentY = player.getY();
            double maxY = player.getPersistentData().getDouble("WUT_SlamMaxY");
            if (currentY > maxY)
            {
                player.getPersistentData().putDouble("WUT_SlamMaxY", currentY);
            }

            if ((player.onGround() || player.isInWater()) && !level.isClientSide())
            {
                player.getPersistentData().remove("WUT_HammerSlam");
                player.setPose(Pose.STANDING);

                executeHammerSlamImpact((ServerLevel) level, player, player.blockPosition());
            }
        }
    }

    private void executeHammerSlamImpact(ServerLevel level, Player player, BlockPos centerPos)
    {
        float chargeRatio = player.getPersistentData().getFloat("WUT_SlamCharge");
        double startY = player.getPersistentData().getDouble("WUT_SlamStartY");
        double maxY = player.getPersistentData().getDouble("WUT_SlamMaxY");

        double fallDistance = Math.max(1.0D, maxY - centerPos.getY());

        player.getPersistentData().remove("WUT_SlamCharge");
        player.getPersistentData().remove("WUT_SlamStartY");
        player.getPersistentData().remove("WUT_SlamMaxY");

        int radius = Math.min(5, (int) (2 + (chargeRatio * 2) + (fallDistance / 5.0D)));

        level.playSound(null, centerPos, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.2F, 0.7F);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                centerPos.getX() + 0.5D, centerPos.getY() + 0.5D, centerPos.getZ() + 0.5D,
                radius, 0.5D, 0.5D, 0.5D, 0.1D);

        BlockPos impactSurface = centerPos.below();
        for (int x = -radius; x <= radius; x++)
        {
            for (int z = -radius; z <= radius; z++)
            {
                if ((x * x + z * z) <= (radius * radius))
                {
                    BlockPos targetPos = impactSurface.offset(x, 0, z);
                    BlockState state = level.getBlockState(targetPos);

                    if (state.is(BlockTags.DIRT) || state.is(BlockTags.SAND) || state.is(BlockTags.LEAVES))
                    {
                        level.destroyBlock(targetPos, true);
                    }
                }
            }
        }

        double killChance = (chargeRatio * 0.5D) + Math.min(0.5D, (fallDistance / 20.0D));

        AABB area = new AABB(centerPos).inflate(radius + 1);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                e -> e != player && e.isAlive() && (e instanceof Mob || e instanceof Player));

        for (LivingEntity target : targets)
        {
            boolean instantKill = level.random.nextDouble() < killChance;

            if (instantKill)
            {
                target.hurt(level.damageSources().playerAttack(player), target.getMaxHealth() * 100.0F);
            }
            else
            {
                float baseDamage = (float) (10.0D + (fallDistance * 2.0D));
                float remainingHealth = Math.max(1.0F, target.getHealth() - baseDamage);

                target.setHealth(remainingHealth);
                target.hurt(level.damageSources().playerAttack(player), 1.0F);
            }
        }
    }*/
}