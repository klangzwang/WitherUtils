package geni.witherutils.client.gui.texture;

import codechicken.lib.gui.modular.sprite.Material;
import codechicken.lib.gui.modular.sprite.ModAtlasHolder;
import geni.witherutils.WitherUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AtlasTexturesGui {

    private static final ModAtlasHolder ATLAS_HOLDER = new ModAtlasHolder(WitherUtils.MODID, "textures/atlas/gui.png", "gui");
    private static final Map<String, Material> MATERIAL_CACHE = new HashMap<>();

    public static ModAtlasHolder getAtlasHolder()
    {
        return ATLAS_HOLDER;
    }

    public static void onResourceReload(RegisterClientReloadListenersEvent event)
    {
        event.registerReloadListener(getAtlasHolder());
    }

    public static Material get(String texture) {
        return MATERIAL_CACHE.computeIfAbsent(WitherUtils.MODID + ":" + texture, e -> getUncached(texture));
    }

    public static Supplier<Material> getter(Supplier<String> texture)
    {
        return () -> get(texture.get());
    }

    public static Supplier<Material> getter(String texture)
    {
        return () -> get(texture);
    }

    public static Material getUncached(String texture)
    {
        return new Material(ATLAS_HOLDER.atlasLocation(), ResourceLocation.fromNamespaceAndPath(WitherUtils.MODID, "gui/" + texture), ATLAS_HOLDER::getSprite);
    }

    public static Material getThemed(String location)
    {
        return get("/" + location);
    }

    public static Supplier<Material> themedGetter(String location)
    {
        return () -> get("/" + location);
    }
}
