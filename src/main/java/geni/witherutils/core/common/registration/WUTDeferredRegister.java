package geni.witherutils.core.common.registration;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredRegister;

public class WUTDeferredRegister<T> extends DeferredRegister<T> {

    private final Function<ResourceKey<T>, ? extends WUTDeferredHolder<T, ?>> holderCreator;

    public WUTDeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace)
    {
        this(registryKey, namespace, WUTDeferredHolder::new);
    }

    public WUTDeferredRegister(ResourceKey<? extends Registry<T>> registryKey, String namespace,
          Function<ResourceKey<T>, ? extends WUTDeferredHolder<T, ? extends T>> holderCreator)
    {
        super(registryKey, namespace);
        this.holderCreator = holderCreator;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends T> WUTDeferredHolder<T, I> register(String name, Function<ResourceLocation, ? extends I> func)
    {
        return (WUTDeferredHolder<T, I>) super.register(name, func);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends T> WUTDeferredHolder<T, I> register(String name, Supplier<? extends I> sup)
    {
        return (WUTDeferredHolder<T, I>) super.register(name, sup);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <I extends T> WUTDeferredHolder<T, I> createHolder(ResourceKey<? extends Registry<T>> registryKey, ResourceLocation key)
    {
        return (WUTDeferredHolder<T, I>) holderCreator.apply(ResourceKey.create(registryKey, key));
    }
}