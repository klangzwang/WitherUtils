package geni.witherutils.client.item.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import geni.witherutils.client.BasicModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class RenderModularPole implements IItemRenderer {

    public RenderModularPole()
    {
        super();
    }

    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return TextureUtils.getTexture("witherutils:blocks/core");
    }

    @Override
    @Nullable
    public PerspectiveModelState getModelState()
    {
        return TransformUtils.DEFAULT_BLOCK;
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean usesBlockLight()
    {
        return false;
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pStack, boolean leftFlip) {

        PerspectiveModelState modelState = getModelState();
        if (modelState != null)
        {
            pStack.translate(0.5, 0.5, 0.5);
            return this;
        }
        return IItemRenderer.super.applyTransform(context, pStack, leftFlip);
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transform, PoseStack matrix, MultiBufferSource buffer, int light, int overlayLight)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        boolean isCrouching = player.isCrouching();

        BakedModel poleModel = BasicModels.STEEL_POLE.getModel();
        BakedModel footModel = BasicModels.STEEL_FOOT.getModel();

        matrix.pushPose();

        Minecraft.getInstance().getItemRenderer().render(stack, transform, false, matrix, buffer, light, overlayLight, poleModel);
        if(isCrouching) {
            Minecraft.getInstance().getItemRenderer().render(stack, transform, false, matrix, buffer, light, overlayLight, footModel);
        }
        matrix.popPose();
    }
}
