package geni.witherutils.client.item.render;

import codechicken.lib.model.PerspectiveModelState;
import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.item.IItemRenderer;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.util.TransformUtils;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;

import geni.withercore.common.helper.TickHelper;
import geni.witherutils.WitherUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RenderModularIcon implements IItemRenderer {

	private static final RenderType dryheadtype = RenderType.create("dryhead_type", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, RenderType.CompositeState.builder()
			.setShaderState(RenderType.RENDERTYPE_ENTITY_CUTOUT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/entity/dryhead.png"), false, false))
			.setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
			.setLightmapState(RenderType.LIGHTMAP)
			.setOverlayState(RenderType.OVERLAY)
			.createCompositeState(false));

	private static final RenderType dryeyestype = RenderType.create("eyes_type", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, RenderType.CompositeState.builder()
			.setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(WitherUtils.loc("textures/block/emissive/blue.png"), false, false))
			.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
			.createCompositeState(false));

	private static final RenderType dryhairtype = RenderType.create("hair_type", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.TRIANGLES, 256, true, false, RenderType.CompositeState.builder()
			.setShaderState(RenderType.RENDERTYPE_EYES_SHADER)
			.setTextureState(RenderStateShard.BLOCK_SHEET)
			.setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
			.setLightmapState(RenderType.LIGHTMAP)
			.setOverlayState(RenderType.OVERLAY)
			.createCompositeState(false));

	private final CCModel head;
	private final CCModel eyes;
	private final CCModel hair;
	private final CCModel jaw;

	public RenderModularIcon()
	{
		super();
		Map<String, CCModel> model = new OBJParser(WitherUtils.loc("models/wither/entity/dryhead/dryhead.obj")).ignoreMtl().parse();
		head = model.get("head").backfacedCopy().computeNormals();
		eyes = model.get("eyes").backfacedCopy().computeNormals();
		hair = model.get("hair").backfacedCopy().computeNormals();
		jaw = model.get("jaw").backfacedCopy().computeNormals();
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
			pStack.scale(1.5F, 1.5F, 1.5F);
			return this;
		}
		return IItemRenderer.super.applyTransform(context, pStack, leftFlip);
	}

	@Override
	public void renderItem(ItemStack stack, ItemDisplayContext transform, PoseStack matrix, MultiBufferSource source, int light, int overlayLight)
	{
		Matrix4 mat = new Matrix4(matrix);
		CCRenderState ccrs = CCRenderState.instance();

		ccrs.reset();
		ccrs.brightness = light;
		ccrs.overlay = overlayLight;

		LocalPlayer player = Minecraft.getInstance().player;
		float time = (player != null ? player.tickCount : 0) + TickHelper.getPartialTick();

		float hoverY = Mth.sin(time * 0.05F) * 0.06F;

		float lookYaw = Mth.sin(time * 0.03F) * 22.0F + Mth.sin(time * 0.071F) * 10.0F;
		float lookPitch = Mth.cos(time * 0.025F) * 12.0F + Mth.sin(time * 0.053F) * 6.0F;
		float lookRoll = Mth.sin(time * 0.018F) * 4.0F;

		mat.translate(0.0, hoverY, 0.0);

		Vector3 pivot = new Vector3(0.5, 0.45, 0.5);
		mat.translate(pivot);
		mat.rotate(Math.toRadians(lookYaw), new Vector3(0, 1, 0));   // Yaw (Links / Rechts)
		mat.rotate(Math.toRadians(lookPitch), new Vector3(1, 0, 0)); // Pitch (Hoch / Runter)
		mat.rotate(Math.toRadians(lookRoll), new Vector3(0, 0, 1));  // Roll (Leichtes Neigen)
		mat.translate(pivot.copy().negate());

		float jawCycle = (Mth.sin(time * 0.05F - 0.5F) + 1.0F) * 0.5F;
		float jawPitch = jawCycle * 7.0F;

		Matrix4 matHHJ = mat.copy();
		Matrix4 matEyes = mat.copy();
		Matrix4 matJaw = mat.copy();

		Vector3 jawPivot = new Vector3(0.5, 0.5, 1.0);
		matJaw.translate(jawPivot);
		matJaw.rotate(Math.toRadians(jawPitch), new Vector3(1, 0, 0));
		matJaw.translate(jawPivot.copy().negate());

		ccrs.bind(dryheadtype, source);
		head.render(ccrs, matHHJ);

		TextureAtlasSprite soulFireSprite = TextureUtils.getTexture("minecraft:block/soul_fire_1");
		ccrs.bind(dryhairtype, source);
		hair.render(ccrs, matHHJ, new IconTransformation(soulFireSprite));

		ccrs.bind(dryheadtype, source);
		jaw.render(ccrs, matJaw);

		float cycleDuration = 70.0f;
		float blinkDuration = 4.0f;

		float cycleTime = time % cycleDuration;
		float scaleY = 1.0f;

		if (cycleTime < blinkDuration) {
			float blinkProgress = cycleTime / blinkDuration;
			scaleY = 1.0f - (Mth.sin(blinkProgress * (float) Math.PI) * 0.95f);
		}

		matEyes.translate(0.5, 0.45, 0.5);
		matEyes.scale(1.0f, scaleY, 1.0f);
		matEyes.translate(-0.5, -0.45, -0.5);

		float eyesCycle = -1.0f + (Mth.sin(time * 0.05F - 0.5F) + 2.0F) * 0.5F;
		float eyesPitch = eyesCycle * 4.0F;

		matEyes.translate(0.0, 0.0, 0.02);
		matEyes.rotate(Math.toRadians(eyesPitch), new Vector3(0, 1, 0));

		ccrs.bind(dryeyestype, source);
		eyes.render(ccrs, matEyes);
	}
}
