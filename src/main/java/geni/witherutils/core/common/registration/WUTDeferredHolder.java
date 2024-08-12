package geni.witherutils.core.common.registration;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.DeferredHolder;

public class WUTDeferredHolder<R, T extends R> extends DeferredHolder<R, T> implements INamedEntry {

    public WUTDeferredHolder(ResourceKey<? extends Registry<R>> registryKey, ResourceLocation valueName)
    {
        this(ResourceKey.create(registryKey, valueName));
    }

    public WUTDeferredHolder(ResourceKey<R> key)
    {
        super(key);
    }

    @Override
    public String getName()
    {
        return INamedEntry.super.getName();
    }
}