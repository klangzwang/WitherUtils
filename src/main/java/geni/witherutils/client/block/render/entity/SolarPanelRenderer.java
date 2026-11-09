package geni.witherutils.client.block.render.entity;

import java.util.Map;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import codechicken.lib.colour.Colour;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import geni.withercore.common.helper.TickHelper;
import geni.withercore.init.WUTBlocks;
import geni.withercore.client.block.render.entity.AbstractBlockEntityRenderer;
import geni.witherutils.WitherUtils;
import geni.witherutils.common.world.level.block.entity.SolarPanelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class SolarPanelRenderer extends AbstractBlockEntityRenderer<SolarPanelBlockEntity> {

	private static final RenderType paneltypecolor = RenderType.create("panel_type_color", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, CompositeState.builder()
			.setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/solar/solar_nocolor.png"), false, false))
			.setTransparencyState(RenderType.NO_TRANSPARENCY)
			.createCompositeState(false));

	private static final RenderType paneltypepulse = RenderType.create("panel_type_pulse", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, CompositeState.builder()
			.setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/solar/solar_nocolor.png"), false, false))
			.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
			.createCompositeState(false));

	private final CCModel plane;

	public static class Basic extends SolarPanelRenderer
	{
		public Basic(Context context)
		{
			super(context);
		}
	}

	public static class Advanced extends SolarPanelRenderer
	{
		public Advanced(Context context)
		{
			super(context);
		}
	}

	public static class Ultra extends SolarPanelRenderer
	{
		public Ultra(Context context)
		{
			super(context);
		}
	}

	private static CCModel standalonePlane;

	public SolarPanelRenderer(Context context)
	{
		super(context);
		Map<String, CCModel> model = new OBJParser(WitherUtils.loc("models/wither/block/generator/solar/solar_bordered.obj")).ignoreMtl().parse();
		plane = model.get("plane").backfacedCopy().computeNormals();
		standalonePlane = plane;
	}

	private static CCModel getOrLoadPlane()
	{
		if (standalonePlane == null)
		{
			Map<String, CCModel> model = new OBJParser(WitherUtils.loc("models/wither/block/generator/solar/solar_bordered.obj")).ignoreMtl().parse();
			standalonePlane = model.get("plane").backfacedCopy().computeNormals();
		}
		return standalonePlane;
	}

	public static void renderStandalone(BlockState state, float alphaProgress, float animTime, PoseStack mStack, MultiBufferSource source, int packedLight, int packedOverlay)
	{
		CCModel modelPlane = getOrLoadPlane();
		if (modelPlane == null)
			return;

		Matrix4 mat = new Matrix4(mStack);
		CCRenderState ccrs = CCRenderState.instance();

		ccrs.reset();
		ccrs.brightness = packedLight;
		ccrs.overlay = packedOverlay;

		Matrix4 matColor = mat.copy();
		Matrix4 matPulse = mat.copy();

		int targetR = 0x22, targetG = 0x22, targetB = 0x22;
		if (state.getBlock() == WUTBlocks.SOLARBASIC.get()) {
			targetR = 0x00; targetG = 0xAB; targetB = 0xE2;
		} else if (state.getBlock() == WUTBlocks.SOLARADV.get()) {
			targetR = 0x0B; targetG = 0xC2; targetB = 0x00;
		} else if (state.getBlock() == WUTBlocks.SOLARULTRA.get()) {
			targetR = 0xFF; targetG = 0x25; targetB = 0x25;
		}

		int baseR = 0x22, baseG = 0x22, baseB = 0x22;
		int r = (int) Mth.lerp(alphaProgress, baseR, targetR);
		int g = (int) Mth.lerp(alphaProgress, baseG, targetG);
		int b = (int) Mth.lerp(alphaProgress, baseB, targetB);

		matColor.translate(0.0F, 0.01255f, 0.0f);

		ccrs.bind(paneltypecolor, source);
		ccrs.baseColour = Colour.packRGBA(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
		modelPlane.render(ccrs, matColor);

		if (alphaProgress > 0.01f)
		{
			double offset = 0.25f - Math.sin(animTime * 0.2F) / 8.0F;

			ccrs.bind(paneltypepulse, source);
			ccrs.baseColour = Colour.packRGBA(1.0F, 1.0F, 1.0F, (float) offset);
			matPulse.translate(0.0F, 0.02510f, 0.0f);
			modelPlane.render(ccrs, matPulse);
		}
	}

	@Override
	public void render(SolarPanelBlockEntity te, float partialTick, PoseStack mStack, MultiBufferSource source, Minecraft mc, ClientLevel level, LocalPlayer player, int packedLight, int packedOverlay)
	{
		if(te.getLevel() == null)
			return;

		BlockState state = te.getLevel().getBlockState(te.getBlockPos());

		float alphaProgress = te.getRenderAlpha();

		Matrix4 mat = new Matrix4(mStack);
		CCRenderState ccrs = CCRenderState.instance();

		ccrs.reset();
		ccrs.brightness = packedLight;
		ccrs.overlay = packedOverlay;

		Matrix4 matColor = mat.copy();
		Matrix4 matPulse = mat.copy();

		//
		// COLOR PANEL
		//
		int targetR = 0x22, targetG = 0x22, targetB = 0x22;
		if(state.getBlock() == WUTBlocks.SOLARBASIC.get()) {
			targetR = 0x00; targetG = 0xAB; targetB = 0xE2;
		} else if(state.getBlock() == WUTBlocks.SOLARADV.get()) {
			targetR = 0x0B; targetG = 0xC2; targetB = 0x00;
		} else if(state.getBlock() == WUTBlocks.SOLARULTRA.get()) {
			targetR = 0xFF; targetG = 0x25; targetB = 0x25;
		}

		int baseR = 0x22, baseG = 0x22, baseB = 0x22;

		int r = (int) Mth.lerp(alphaProgress, baseR, targetR);
		int g = (int) Mth.lerp(alphaProgress, baseG, targetG);
		int b = (int) Mth.lerp(alphaProgress, baseB, targetB);

		matColor.translate(0.0F, 0.01255f, 0.0f);

		ccrs.bind(paneltypecolor, source);
		ccrs.baseColour = Colour.packRGBA(r / 255.0f, g / 255.0f, b / 255.0f, 1.0f);
		plane.render(ccrs, matColor);

		//
		// PULSE PANEL
		//
		if(alphaProgress > 0.01f)
		{
			float time = mc.level.getGameTime() + TickHelper.getPartialTick();
			double offset = 0.25f - Math.sin((float)time * 0.2F) / 8.0F;

			ccrs.bind(paneltypepulse, source);
			ccrs.baseColour = Colour.packRGBA(1.0F, 1.0F, 1.0F, (float)offset);
			matPulse.translate(0.0F, 0.02510f, 0.0f);
			plane.render(ccrs, matPulse);
		}
	}
}
