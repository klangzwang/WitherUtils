package geni.witherutils.common.events;

import java.util.List;

import geni.withercore.common.util.SoundUtil;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTEffects;
import geni.withercore.init.WUTEntities;
import geni.withercore.init.WUTItems;
import geni.withercore.init.WUTSounds;
import geni.witherutils.common.world.entity.bolt.CursedLightningBolt;
import geni.witherutils.common.world.level.block.SoulFireBlock;
import geni.witherutils.common.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickBlock;

public class AnvilCauldronEvents {

    /*
     * 
     * ANVILCAULDRON
     * 
     */
    @SubscribeEvent
    public void curseTheAnvilCauldron(RightClickBlock event)
    {
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        Player player = event.getEntity();
        Direction facing = event.getHitVec().getDirection();

        InteractionHand hand = event.getHand();

        ItemStack heldStack = player.getItemInHand(hand);
        if (!heldStack.isEmpty() && heldStack.getItem() == WUTItems.HAMMER.get())
        {
            if(level.getBlockState(pos).getBlock() == Blocks.ANVIL ||
            level.getBlockState(pos).getBlock() == Blocks.CHIPPED_ANVIL ||
            level.getBlockState(pos).getBlock() == Blocks.DAMAGED_ANVIL ||
            level.getBlockState(pos).getBlock() == Blocks.CAULDRON)
            {
                player.swing(hand);
                event.setCanceled(true);

                calcTransform(level, player, pos, level.getBlockState(pos).getBlock(), facing);
            }
        }
    }
    
    public void calcTransform(Level level, Player player, BlockPos pos, Block block, Direction facing)
    {
		if(level instanceof ServerLevel serverLevel)
		{
        	SoundUtil.playSoundDistrib(level, pos, SoundEvents.TRIDENT_THUNDER.value(), 0.75f, 1.0F, false, true);
        	SoundUtil.playSoundDistrib(level, pos, WUTSounds.HAMMERHIT.get(), 0.75f, 1.0F, false, false);
			
            if (player instanceof ServerPlayer serverPlayer)
            {
                CriteriaTriggers.ITEM_USED_ON_BLOCK.trigger(serverPlayer, pos, serverPlayer.getItemInHand(serverPlayer.getUsedItemHand()));
                
            	doLightningStrike(serverLevel, serverPlayer, pos);
            	
            	MobEffectInstance effect = new MobEffectInstance(WUTEffects.BLIND, 25, 50, true, true);
            	serverPlayer.addEffect(effect);
            }
            
			doTransform(serverLevel, pos, block, player, facing);
		}
		else
		{
            Vec3 vec3 = player.getLookAngle().normalize();
            float i = 6 / 20F;
            i = (i * i + i * 2) / 3;
            i *= 4;
            if (i > 6)
            {
                i = 6;
            }
            i *= 1;
            player.push(vec3.x * -i, vec3.y * -i / 2 + 0.25f, vec3.z * -i);
		}
    }
    
    public void doLightningStrike(Level level, Player player, BlockPos pos)
    {
        CursedLightningBolt lightningbolt = WUTEntities.CURSEDBOLT.get().create(level);
        lightningbolt.moveTo(Vec3.atBottomCenterOf(pos.above()));
        level.addFreshEntity(lightningbolt);
        
        if (level instanceof ServerLevel serverLevel)
        {
			serverLevel.sendParticles(ParticleTypes.SMOKE,
					pos.above().getX() + 0.5D,
					pos.above().getY() + 0.0D,
					pos.above().getZ() + 0.5D,
					200, 0, 0, 0, 0.025D);

            List<Entity> list1 = serverLevel.getEntities(player,
            		new AABB(player.getX() - 3.0, player.getY() - 3.0, player.getZ() - 3.0,
            				 player.getX() + 3.0, player.getY() + 6.0 + 3.0, player.getZ() + 3.0),
            				 Entity::isAlive);
            
            for (Entity entity : list1)
            {
                if (!net.neoforged.neoforge.event.EventHooks.onEntityStruckByLightning(entity, lightningbolt))
                entity.thunderHit(serverLevel, lightningbolt);
            }
        }

        for(int i = 0; i < 20; ++i)
        {
            BlockPos blockpos = pos.offset(-2 + level.random.nextInt(4), level.random.nextInt(3) - 1, -2 + level.random.nextInt(4));
            BlockState blockstate = SoulFireBlock.getState(level, blockpos);
            if(level.getBlockState(blockpos).isAir() && blockstate.canSurvive(level, blockpos))
            {
                level.setBlockAndUpdate(blockpos, blockstate);
            }
        }
    }

    public void doTransform(ServerLevel serverLevel, BlockPos pos, Block block, Player player, Direction facing)
    {
        BlockState oldState = serverLevel.getBlockState(pos);

        BlockState newState = WUTBlocks.ANVIL.get().defaultBlockState();
        if (oldState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {

            Direction currentFacing = oldState.getValue(BlockStateProperties.HORIZONTAL_FACING);
            newState = newState.setValue(BlockStateProperties.HORIZONTAL_FACING, currentFacing.getClockWise());
        }

        serverLevel.setBlockAndUpdate(pos, newState);

        BlockEntity be = serverLevel.getBlockEntity(pos);
        if(be instanceof AnvilBlockEntity avBe)
        {
            avBe.setHotCounter(1.0f);
        }
    }
}
