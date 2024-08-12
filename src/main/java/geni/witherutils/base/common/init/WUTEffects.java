package geni.witherutils.base.common.init;

import geni.witherutils.api.lib.Names;
import geni.witherutils.base.common.effect.BlindEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WUTEffects {

	public static final DeferredRegister<MobEffect> EFFECT_TYPES = DeferredRegister.create(Registries.MOB_EFFECT, Names.MODID);

	public static final Holder<MobEffect> BLIND = EFFECT_TYPES.register("blinded", () -> new BlindEffect(MobEffectCategory.HARMFUL, 0));
}
