package geni.witherutils.client.item.render;

import java.util.Map;

import codechicken.lib.colour.Colour;
import codechicken.lib.math.MathHelper;
import codechicken.lib.vec.Vector3;
import com.mojang.math.Axis;
import geni.withercore.common.helper.TickHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Matrix4;
import geni.witherutils.WitherUtils;
import geni.withercore.init.WUTBlocks;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderModularSolarPanel implements IItemRenderer {
	
	private static final RenderType typerender01 = RenderType.create("solar_type01", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_ENTITY_SOLID_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/shared/full16.png"), false, false))
            .setTransparencyState(RenderType.NO_TRANSPARENCY)
            .setLightmapState(RenderType.LIGHTMAP)
            .setOverlayState(RenderType.OVERLAY)
            .createCompositeState(false));
	
	private static final RenderType typerender02 = RenderType.create("solar_type02", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_ENTITY_SOLID_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/shared/full11.png"), false, false))
            .setTransparencyState(RenderType.NO_TRANSPARENCY)
            .setLightmapState(RenderType.LIGHTMAP)
            .setOverlayState(RenderType.OVERLAY)
            .createCompositeState(false));

	private static final RenderType paneltyperender = RenderType.create("panel_type", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, CompositeState.builder()
            .setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/solar/solar_nocolor.png"), false, false))
    		.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
            .createCompositeState(false));
	
    private final CCModel border;
    private final CCModel plane;

    private final Item item;

    public RenderModularSolarPanel(Item item)
    {
    	this.item = item;
        Map<String, CCModel> model = new OBJParser(WitherUtils.loc("models/wither/block/generator/solar/solar_bordered.obj")).ignoreMtl().parse();
        border = model.get("border").backfacedCopy().computeNormals();
        plane = model.get("plane").backfacedCopy().computeNormals();
    }
    
    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return TextureUtils.getTexture("witherutils:block/solar/solar_nocolor");
    }

    @Override
    public BakedModel applyTransform(ItemDisplayContext context, PoseStack pStack, boolean leftFlip) {

        PerspectiveModelState modelState = getModelState();
        if (modelState != null)
        {
            if (context == ItemDisplayContext.GUI)
            {
                pStack.scale(0.85F, 0.85F, 0.85F);
                pStack.mulPose(Axis.XP.rotationDegrees(90));
            }
            else if (context == ItemDisplayContext.GROUND)
            {
                pStack.translate(0.0, 0.15, 0.0);
                pStack.scale(0.5F, 0.5F, 0.5F);
            }
            else if (context == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND)
            {
                pStack.translate(0.2, 0.1, -0.1);
                pStack.scale(0.6F, 0.6F, 0.6F);
                pStack.mulPose(Axis.XP.rotationDegrees(10));
                pStack.mulPose(Axis.YP.rotationDegrees(-25));
            }
            else if (context == ItemDisplayContext.FIRST_PERSON_LEFT_HAND)
            {
                pStack.translate(-0.2, 0.1, -0.1);
                pStack.scale(0.6F, 0.6F, 0.6F);
                pStack.mulPose(Axis.XP.rotationDegrees(10));
                pStack.mulPose(Axis.YP.rotationDegrees(25));
            }
            else if (context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND)
            {
                pStack.translate(0.0, 0.15, 0.05);
                pStack.scale(0.5F, 0.5F, 0.5F);
                pStack.mulPose(Axis.XP.rotationDegrees(20));
                pStack.mulPose(Axis.YP.rotationDegrees(-15));
            }
            else if (context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND)
            {
                pStack.translate(0.0, 0.15, 0.05);
                pStack.scale(0.5F, 0.5F, 0.5F);
                pStack.mulPose(Axis.XP.rotationDegrees(20));
                pStack.mulPose(Axis.YP.rotationDegrees(15));
            }

            return this;
        }
        return IItemRenderer.super.applyTransform(context, pStack, leftFlip);
    }

    @Override
	public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay)
	{
        Matrix4 mat = new Matrix4(mStack);
        CCRenderState ccrs = CCRenderState.instance();
        
        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        Matrix4 matBorder = mat.copy();
        Matrix4 matPlane = mat.copy();

        ccrs.bind(typerender01, source);
        border.render(ccrs, matBorder);

        if(stack.getItem() == WUTBlocks.SOLARCASE.get().asItem())
        {
            ccrs.bind(typerender02, source);
            ccrs.baseColour = 0x666666FF;
            plane.render(ccrs, matPlane);
            return;
        }

        int panelcolor = 0xFFFFFFFF;

        if(stack.getItem() == WUTBlocks.SOLARBASIC.get().asItem())
        {
            panelcolor = 0x00ABE2FF;
        }
        if(stack.getItem() == WUTBlocks.SOLARADV.get().asItem())
        {
            panelcolor = 0x0BC200FF;
        }
        if(stack.getItem() == WUTBlocks.SOLARULTRA.get().asItem())
        {
            panelcolor = 0xFF2525FF;
        }

        ccrs.bind(paneltyperender, source);
        ccrs.baseColour = panelcolor;
        mat.translate(0, 0.025f, 0);
        plane.render(ccrs, mat);

		Minecraft mc = Minecraft.getInstance();
        float time = mc.level.getGameTime() + TickHelper.getPartialTick();
		double offset = 0.25f - Math.sin((float)time * 0.2F) / 8.0F;
        
        ccrs.bind(paneltyperender, source);
        ccrs.baseColour = Colour.packRGBA(1.0F, 1.0F, 1.0F, (float)offset);
        plane.render(ccrs, mat);

        ccrs.bind(typerender02, source);
        matPlane.translate(0.0f, 0.02f, 0.0f);
        matPlane.translate(0.5f, 0.0f, 0.5f);
        matPlane.rotate(180 * MathHelper.torad, new Vector3(1f, 0f, 0f));
        matPlane.translate(-0.5f, -0.0f, -0.5f);
        plane.render(ccrs, matPlane);
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() { return TransformUtils.DEFAULT_ITEM; }
    @Override public boolean useAmbientOcclusion() { return false; }
    @Override public boolean isGui3d() { return false; }
    @Override public boolean usesBlockLight() { return false; }

    public static class SOLAR_CASE extends RenderModularSolarPanel { public SOLAR_CASE() {super(WUTBlocks.SOLARCASE.asItem());}}
    public static class SOLAR_ADV extends RenderModularSolarPanel { public SOLAR_ADV() {super(WUTBlocks.SOLARADV.asItem());}}
    public static class SOLAR_BASIC extends RenderModularSolarPanel { public SOLAR_BASIC() {super(WUTBlocks.SOLARBASIC.asItem());}}
    public static class SOLAR_ULTRA extends RenderModularSolarPanel { public SOLAR_ULTRA() {super(WUTBlocks.SOLARULTRA.asItem());}}
}
