package geni.witherutils.common.world.item;

import geni.withercore.common.world.item.WitherItem;
import geni.withercore.init.WUTSounds;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SteelHammerItem extends WitherItem {

	public SteelHammerItem(Item.Properties props)
	{
		super(props);
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity)
	{
		if(entity.isAlive() && entity instanceof Mob mob)
		{
			mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 90, 2));
			if(!mob.hasEffect(MobEffects.MOVEMENT_SLOWDOWN))
				mob.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
		}
		player.level().playSound(null, entity.blockPosition(), WUTSounds.HAMMERHIT.get(), SoundSource.PLAYERS, 1.0F, 0.8F + player.level().getRandom().nextFloat() - 0.6F);
		return super.onLeftClickEntity(stack, player, entity);
	}

	/*@Override
	public int getUseDuration(ItemStack stack, LivingEntity entity)
	{
		return 100;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack)
	{
		return UseAnim.CROSSBOW;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
	{
		if (player.getXRot() < 0.0F)
		{
			player.startUsingItem(hand);
			level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_IDLE_AIR, SoundSource.PLAYERS, 1.0F, 0.8F);
			return InteractionResultHolder.consume(player.getItemInHand(hand));
		}
		return InteractionResultHolder.pass(player.getItemInHand(hand));
	}

	@Override
	public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft)
	{
		if (!(entity instanceof Player player)) return;

		int usedTicks = getUseDuration(stack, entity) - timeLeft;

		if (usedTicks >= 20)
		{
			float chargeRatio = (float) usedTicks / getUseDuration(stack, entity);

			float pitch = player.getXRot();
			if (pitch < 0.0F)
			{
				Vec3 lookVec = player.getLookAngle();

				double forwardForce = (1.5D + (chargeRatio * 1.5D));
				double upwardForce = (0.8D + (chargeRatio * 1.0D));

				Vec3 jumpVelocity = new Vec3(
						lookVec.x * forwardForce,
						Math.abs(lookVec.y) * upwardForce,
						lookVec.z * forwardForce
				);

				player.setDeltaMovement(jumpVelocity);
				player.hasImpulse = true;

				level.playSound(null, player.blockPosition(), SoundEvents.BREEZE_WIND_CHARGE_BURST.value(), SoundSource.PLAYERS, 1.2F, 0.9F);

				// NBT-Daten für Flug, Charge und Höhenberechnung speichern
				player.getPersistentData().putBoolean("WUT_HammerSlam", true);
				player.getPersistentData().putFloat("WUT_SlamCharge", chargeRatio);
				player.getPersistentData().putDouble("WUT_SlamStartY", player.getY());
				player.getPersistentData().putDouble("WUT_SlamMaxY", player.getY());

				player.getCooldowns().addCooldown(this, 60);
			}
		}
	}*/
}
