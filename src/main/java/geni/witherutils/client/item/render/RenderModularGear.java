package geni.witherutils.client.item.render;

import net.minecraft.client.resources.model.BakedModel;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import geni.witherutils.client.BasicModels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderModularGear implements IItemRenderer {

    public RenderModularGear()
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
        return TransformUtils.DEFAULT_ITEM;
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
	public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay)
	{
		Minecraft mc = Minecraft.getInstance();
		
		mStack.pushPose();
		mStack.scale(0.9f, 0.9f, 1.0f);
		mStack.translate(-0.09f, -0.09f, 0.0f);
		if(ctx == ItemDisplayContext.GUI) {
			mStack.mulPose(Axis.ZP.rotationDegrees((360.0F / 45) * (mc.player.clientLevel.getGameTime() % 45)));
		}
		Minecraft.getInstance().getItemRenderer().render(stack, ctx, false, mStack, source, packedLight, packedOverlay, BasicModels.STEEL_GEAR.getModel());
		mStack.popPose();
        
		mStack.pushPose();
		mStack.scale(0.8f, 0.8f, 1.0f);
		mStack.translate(0.15f, 0.15f, 0.0f);
		if(ctx == ItemDisplayContext.GUI) {
			mStack.mulPose(Axis.ZP.rotationDegrees((-360.0F / 45) * (mc.player.clientLevel.getGameTime() % 45)));
		}
		Minecraft.getInstance().getItemRenderer().render(stack, ctx, false, mStack, source, packedLight, packedOverlay, BasicModels.STEEL_GEAR.getModel());
		mStack.popPose();
	}
}
