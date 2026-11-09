package geni.witherutils.common.world.fluid;

import geni.witherutils.WitherUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import org.jetbrains.annotations.Nullable;

public class FluidRenderProps implements IClientFluidTypeExtensions {
	
    private final ResourceLocation still;
    private final ResourceLocation flowing;
    private final ResourceLocation overlay;
    private final int colorTint;

    public FluidRenderProps(String still, String flowing, String overlay)
    {
        this(still, flowing, overlay, 0xFFFFFFFF);
    }

    public FluidRenderProps(String still, String flowing, String overlay, int colorTint)
    {
        this.still = still.indexOf(':') > 0 ? ResourceLocation.parse(still) : WitherUtils.loc("block/fluid/" + still);
        this.flowing = flowing.indexOf(':') > 0 ? ResourceLocation.parse(flowing) : WitherUtils.loc("block/fluid/" + flowing);
        this.overlay = overlay.indexOf(':') > 0 ? ResourceLocation.parse(overlay) : WitherUtils.loc("block/fluid/" + overlay);
        this.colorTint = colorTint;
    }

    @Override
    public ResourceLocation getStillTexture()
    {
        return still;
    }

    @Override
    public ResourceLocation getFlowingTexture()
    {
        return flowing;
    }

    @Override
    public ResourceLocation getOverlayTexture()
    {
        return overlay;
    }

    @Override
    public int getTintColor()
    {
        return colorTint;
    }
}
