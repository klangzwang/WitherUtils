package geni.witherutils.base.common.item.withersteel.wand;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import geni.witherutils.api.enchant.IWitherEnchantable;
import geni.witherutils.api.item.IRotatingItem;
import geni.witherutils.base.common.base.IWitherPoweredItem;
import geni.witherutils.base.common.base.WitherItem;
import geni.witherutils.base.common.base.WitherItemEnergy;
import geni.witherutils.base.common.config.common.ItemsConfig;
import geni.witherutils.base.common.entity.soulorb.SoulOrbProjectile;
import geni.witherutils.base.common.init.WUTEffects;
import geni.witherutils.base.common.init.WUTEnchants;
import geni.witherutils.base.common.init.WUTItems;
import geni.witherutils.base.common.init.WUTSounds;
import geni.witherutils.base.common.item.withersteel.IWitherSteelItem;
import geni.witherutils.base.common.item.withersteel.WitherSteelRecipeManager;
import geni.witherutils.core.common.util.EnergyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.EntityTeleportEvent;

public class WandSteelItem extends WitherItem implements IWitherSteelItem, IWitherEnchantable, IWitherPoweredItem, IRotatingItem {

    static
    {
        MinecraftForge.EVENT_BUS.register(new WitherSteelRecipeManager());
    }
    
	public WandSteelItem()
	{
		super(new Item.Properties().stacksTo(1).fireResistant());
    }
	
	@Override
	public void inventoryTick(ItemStack stack, Level level, Entity entity, int value, boolean isSelected)
	{
		if(entity instanceof Player player)
		{
			if(!isSelected)
				return;
	    	if(EnergyUtil.getEnergyStored(stack) < ItemsConfig.WANDENERGYUSE.get())
	    		return;
			if(player.getCooldowns().isOnCooldown(stack.getItem()))
				return;
			if(!player.isCrouching())
				return;
			
			if(!level.isClientSide)
			{
				float f = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
				float f3 = 1.0F * f;
				
				Vec3 look = player.getLookAngle();
				
				List<LivingEntity> targets = getTargetsInCone(player.getCommandSenderWorld(), player.position().subtract(look), player.getLookAngle().scale(9), 1.57079f, .5f);
				for(LivingEntity t : targets)
				{
					if (t != player && t != entity && !player.isAlliedTo(t) && (!(t instanceof ArmorStand) || !((ArmorStand)t).isMarker()))
					{
	                    t.knockback((double) 0.4F, (double) Mth.sin(player.getYRot() * ((float) Math.PI / 180F)), (double)(-Mth.cos(player.getYRot() * ((float) Math.PI / 180F))));
	                    t.hurt(level.damageSources().playerAttack(player), f3);
					}
				}
				
				player.getCooldowns().addCooldown(this, ItemsConfig.WANDPORTDELAY.get() * 2);
				player.addEffect(new MobEffectInstance(WUTEffects.BLIND.get(), 5, 0, false, false));
				EnergyUtil.extractEnergy(player.getMainHandItem(), getEnergyUse(stack), false);
				
				if(player.getMainHandItem() == stack)
					player.swing(InteractionHand.MAIN_HAND);
				else
					player.swing(InteractionHand.OFF_HAND);
				
            	for(int i = 0; i < 3; i++)
            	{
	            	SoulOrbProjectile orb = new SoulOrbProjectile(level, player);
	            	orb.shootFromRotation(player, player.getXRot(), player.getYRot() - 20 + (20 * i), 0.0F, 4.0F, 1.0F);
	            	orb.handleEntityEvent((byte)3);
	            	level.addFreshEntity(orb);
            	}
			}
			else
			{
	            player.playNotifySound(WUTSounds.SWOOSH.get(), SoundSource.NEUTRAL, 0.5F, 0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
	            player.playNotifySound(WUTSounds.FEAR.get(), SoundSource.NEUTRAL, 0.5F, 2F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
			}
		}
	}
	
	public static List<LivingEntity> getTargetsInCone(Level world, Vec3 start, Vec3 dir, float spreadAngle, float truncationLength)
	{
		double length = dir.length();
		Vec3 dirNorm = dir.normalize();
		double radius = Math.tan(spreadAngle/2)*length;

		Vec3 endLow = start.add(dir).subtract(radius, radius, radius);
		Vec3 endHigh = start.add(dir).add(radius, radius, radius);

		AABB box = new AABB(minInArray(start.x, endLow.x, endHigh.x), minInArray(start.y, endLow.y, endHigh.y), minInArray(start.z, endLow.z, endHigh.z),
				maxInArray(start.x, endLow.x, endHigh.x), maxInArray(start.y, endLow.y, endHigh.y), maxInArray(start.z, endLow.z, endHigh.z));

		List<LivingEntity> list = world.getEntitiesOfClass(LivingEntity.class, box);
		list.removeIf(e -> !isPointInCone(dirNorm, radius, length, truncationLength, e.position().subtract(start)));
		return list;
	}
	
	public static boolean isPointInCone(Vec3 normDirection, double radius, double length, float truncationLength, Vec3 relativePoint)
	{
		double projectedDist = relativePoint.dot(normDirection);
		if(projectedDist < truncationLength||projectedDist > length)
			return false;
		double radiusAtDist = projectedDist/length*radius;
		Vec3 orthVec = relativePoint.subtract(normDirection.scale(projectedDist));
		return orthVec.lengthSqr() < (radiusAtDist*radiusAtDist);
	}
	public static double minInArray(double... f)
	{
		if(f.length < 1)
			return 0;
		double min = f[0];
		for(int i = 1; i < f.length; i++)
			min = Math.min(min, f[i]);
		return min;
	}
	public static double maxInArray(double... f)
	{
		if(f.length < 1)
			return 0;
		double max = f[0];
		for(int i = 1; i < f.length; i++)
			max = Math.max(max, f[i]);
		return max;
	}

    @Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
    	ItemStack stack = player.getItemInHand(hand);

    	if(EnergyUtil.getEnergyStored(stack) < ItemsConfig.WANDENERGYUSE.get())
    		return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    	
    	player.addEffect(new MobEffectInstance(WUTEffects.BLIND.get(), 20, 0, false, false));
		player.getCooldowns().addCooldown(this, ItemsConfig.WANDPORTDELAY.get());
	    player.awardStat(Stats.ITEM_USED.get(this));

		if (!level.isClientSide)
		{
			if (shortTeleport(level, player, hand))
			{
				player.swing(hand);
				EnergyUtil.extractEnergy(player.getMainHandItem(), getEnergyUse(stack), false);
			}	
		}
		else
		{
            for(int i = 0; i < 20; ++i)
            {
            	player.level().addParticle(ParticleTypes.PORTAL,
            			player.getX((8.0D * level.random.nextDouble() - 1.0D) * 0.5D),
            			player.getY(level.random.nextDouble()) - 0.25D,
            			player.getZ((8.0D * level.random.nextDouble() - 1.0D) * 0.5D),
            			(level.random.nextDouble() - 0.5D) * 2.0D,
            			-level.random.nextDouble(),
            			(level.random.nextDouble() - 0.5D) * 2.0D);
            }

            player.playNotifySound(WUTSounds.SWOOSH.get(), SoundSource.PLAYERS, 1F, 1F);
            player.playNotifySound(WUTSounds.FEAR.get(), SoundSource.PLAYERS, 1F, 2F);
		}
    	return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
	}
    
    @SuppressWarnings("resource")
	public static boolean shortTeleport(Level level, Player player, InteractionHand hand)
    {
        Vec3 targetVec = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 lookVec = player.getLookAngle();
        BlockPos target = null;
        
        for (double i = 10; i >= 2; i -= 0.5)
        {
            Vec3 v3d = targetVec.add(lookVec.multiply(i, i, i));
            target = new BlockPos((int) Math.round(v3d.x), (int) Math.round(v3d.y), (int) Math.round(v3d.z));
            if (canTeleportTo(level, target.below()))
            {
                break;
            }
            else
            {
                target = null;
            }
        }
        
        if (target != null)
        {
            if (!player.level().isClientSide)
            {
                Vec3 teleportVec = checkTeleport(player, target);
                if (teleportVec == null)
                {
                    return false;
                }
                player.teleportTo(teleportVec.x(), teleportVec.y(), teleportVec.z());
            }
            player.fallDistance = 0;
            return true;
        }
        else
        {
            return false;
        }
    }

    public static boolean canTeleportTo(BlockGetter level, BlockPos target)
    {
        return !level.getBlockState(target.immutable().above(1)).canOcclude()
                && !level.getBlockState(target.immutable().above(2)).canOcclude()
                && target.getY() >= level.getMinBuildHeight();
    }

    @Nullable
    private static Vec3 checkTeleport(Player player, BlockPos target)
    {
        EntityTeleportEvent event = new EntityTeleportEvent(player, target.getX() + 0.5, target.getY(), target.getZ() + 0.5);
        if (MinecraftForge.EVENT_BUS.post(event))
        {
            return null;
        }
        return new Vec3(event.getTargetX(), event.getTargetY(), event.getTargetZ());
    }
    
    @Override
    public boolean isFoil(ItemStack pStack)
    {
        return false;
    }
    @Override
    public int getMaxStackSize(ItemStack stack)
    {
        return 1;
    }
    @Override
    public boolean isEnchantable(ItemStack stack)
    {
        return false;
    }
    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book)
    {
        return false;
    }
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair)
    {
        return repair.getItem() == WUTItems.SOULISHED_INGOT.get();
    }

    /*
     * 
     * IRotatingItem
     * 
     */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer)
    {
        setupBEWLR(consumer);
    }
    
    @Override
	public float getTicksPerRotation()
	{
		return 45;
	}
	@Override
	public boolean hasItemOnTop()
	{
		return false;
	}
	@Override
	public boolean isRotating()
	{
		return false;
	}

	@Override
	public boolean consumeByActive()
	{
		return false;
	}

	@Override
	public int getEnergyUse(ItemStack stack)
	{
		return ItemsConfig.WANDENERGYUSE.get();
	}

	@Override
	public int getMaxEnergy(ItemStack stack)
	{
		return getPowerLevel(stack) * ItemsConfig.WANDENERGY.get();
	}

	@Override
	public int getMaxTransfer(ItemStack stack)
	{
		return getMaxEnergy(stack) / 4;
	}
	
	@Override
	public boolean hasCharge(ItemStack stack)
	{
		return EnergyUtil.extractEnergy(stack, getEnergyUse(stack), true) > 0;
	}

	@Override
	public void consumeCharge(ItemStack stack)
	{
		EnergyUtil.extractEnergy(stack, getEnergyUse(stack), false);
	}

	@Override
    public int getPowerLevel(ItemStack stack)
    {
        int enchantlevel = 0;
        if(stack.isEmpty() == false && EnchantmentHelper.getEnchantments(stack) != null && EnchantmentHelper.getEnchantments(stack).containsKey(WUTEnchants.ENERGY.get()))
        {
            int newlevel = EnchantmentHelper.getEnchantments(stack).get(WUTEnchants.ENERGY.get());
            if(newlevel > enchantlevel)
            {
                enchantlevel = newlevel;
            }
        }
        return enchantlevel;
    }

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt)
	{
		return new WitherItemEnergy.Item.Provider(stack, getMaxEnergy(stack), getMaxTransfer(stack), getMaxTransfer(stack));
	}
	
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
    {
        if (slotChanged)
        {
            return super.shouldCauseReequipAnimation(oldStack, newStack, true);
        }
        return oldStack.getItem() != newStack.getItem();
    }
    
    @Override
    public boolean isBarVisible(ItemStack pStack)
    {
        return getPowerLevel(pStack) > 0;
    }
    @Override
    public int getBarWidth(ItemStack pStack)
    {
        return pStack
            .getCapability(ForgeCapabilities.ENERGY)
            .map(energyStorage -> Math.round(energyStorage.getEnergyStored() * 13.0F / energyStorage.getMaxEnergyStored()))
            .orElse(0);
    }
    @Override
    public int getBarColor(ItemStack pStack)
    {
        return 0x0000fff6;
    }
}
