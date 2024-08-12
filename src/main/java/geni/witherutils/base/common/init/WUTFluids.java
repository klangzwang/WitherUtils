package geni.witherutils.base.common.init;

import java.util.function.Consumer;
import java.util.function.Supplier;

import geni.witherutils.api.lib.Names;
import geni.witherutils.base.common.fluid.FluidBlueLimbo;
import geni.witherutils.base.common.fluid.FluidColdSlush;
import geni.witherutils.base.common.fluid.FluidExperience;
import geni.witherutils.base.common.fluid.FluidFertilizer;
import geni.witherutils.base.common.fluid.FluidPortium;
import geni.witherutils.base.common.fluid.FluidRedResin;
import geni.witherutils.base.common.fluid.FluidSoulful;
import geni.witherutils.base.common.fluid.FluidWitherWater;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class WUTFluids {

    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Names.MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, Names.MODID);
    
    public static final Supplier<FluidType> BLUELIMBO_FLUID_TYPE = registerFluidType("bluelimbo", standardProps(15, 600, 6000), FluidBlueLimbo.RENDER_PROPS);
    public static final Supplier<FluidType> COLDSLUSH_FLUID_TYPE = registerFluidType("coldslush", standardProps(15, 600, 6000), FluidColdSlush.RENDER_PROPS);
    public static final Supplier<FluidType> EXPERIENCE_FLUID_TYPE = registerFluidType("experience", standardProps(15, 600, 6000), FluidExperience.RENDER_PROPS);
    public static final Supplier<FluidType> FERTILIZER_FLUID_TYPE = registerFluidType("fertilizer", standardProps(15, 600, 6000), FluidFertilizer.RENDER_PROPS);
    public static final Supplier<FluidType> PORTIUM_FLUID_TYPE = registerFluidType("portium", standardProps(15, 600, 6000), FluidPortium.RENDER_PROPS);
    public static final Supplier<FluidType> REDRESIN_FLUID_TYPE = registerFluidType("redresin", standardProps(15, 600, 6000), FluidRedResin.RENDER_PROPS);
    public static final Supplier<FluidType> SOULFUL_FLUID_TYPE = registerFluidType("soulful", standardProps(15, 600, 6000), FluidSoulful.RENDER_PROPS);
    public static final Supplier<FluidType> WITHERWATER_FLUID_TYPE = registerFluidType("witherwater", standardProps(15, 600, 6000), FluidWitherWater.RENDER_PROPS);
    
    public static final Supplier<FlowingFluid> BLUELIMBO = register("bluelimbo", FluidBlueLimbo.Source::new);
    public static final Supplier<FlowingFluid> BLUELIMBO_FLOWING = register("bluelimbo_flowing", FluidBlueLimbo.Flowing::new);

    public static final Supplier<FlowingFluid> COLDSLUSH = register("coldslush", FluidColdSlush.Source::new);
    public static final Supplier<FlowingFluid> COLDSLUSH_FLOWING = register("coldslush_flowing", FluidColdSlush.Flowing::new);

    public static final Supplier<FlowingFluid> EXPERIENCE = register("experience", FluidExperience.Source::new);
    public static final Supplier<FlowingFluid> EXPERIENCE_FLOWING = register("experience_flowing", FluidExperience.Flowing::new);

    public static final Supplier<FlowingFluid> FERTILIZER = register("fertilizer", FluidFertilizer.Source::new);
    public static final Supplier<FlowingFluid> FERTILIZER_FLOWING = register("fertilizer_flowing", FluidFertilizer.Flowing::new);

    public static final Supplier<FlowingFluid> PORTIUM = register("portium", FluidPortium.Source::new);
    public static final Supplier<FlowingFluid> PORTIUM_FLOWING = register("portium_flowing", FluidPortium.Flowing::new);

    public static final Supplier<FlowingFluid> REDRESIN = register("redresin", FluidRedResin.Source::new);
    public static final Supplier<FlowingFluid> REDRESIN_FLOWING = register("redresin_flowing", FluidRedResin.Flowing::new);

    public static final Supplier<FlowingFluid> SOULFUL = register("soulful", FluidSoulful.Source::new);
    public static final Supplier<FlowingFluid> SOULFUL_FLOWING = register("soulful_flowing", FluidSoulful.Flowing::new);

    public static final Supplier<FlowingFluid> WITHERWATER = register("witherwater", FluidWitherWater.Source::new);
    public static final Supplier<FlowingFluid> WITHERWATER_FLOWING = register("witherwater_flowing", FluidWitherWater.Flowing::new);

    private static <T extends Fluid> Supplier<T> register(String name, final Supplier<T> sup)
    {
        return FLUIDS.register(name, sup);
    }

    private static Supplier<FluidType> registerFluidType(String name, FluidType.Properties props, IClientFluidTypeExtensions renderProps)
    {
        return FLUID_TYPES.register(name, () -> new FluidType(props)
        {
            @Override
            public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer)
            {
                consumer.accept(renderProps);
            }
        });
    }
    
    private static FluidType.Properties standardProps(int lightlevel, int density, int viscosity)
    {
        return FluidType.Properties.create()
        		.lightLevel(lightlevel)
                .density(density)
                .viscosity(viscosity)
                .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH);
    }
}
