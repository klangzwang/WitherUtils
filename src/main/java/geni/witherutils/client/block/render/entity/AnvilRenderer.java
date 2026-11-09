package geni.witherutils.client.block.render.entity;

import javax.annotation.Nullable;

import geni.withercore.client.block.render.entity.AbstractBlockEntityRenderer;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import codechicken.lib.vec.Vector3;
import geni.witherutils.WitherUtils;
import geni.witherutils.client.BasicModels;
import geni.witherutils.common.world.level.block.entity.AnvilBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TransparencyStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class AnvilRenderer extends AbstractBlockEntityRenderer<AnvilBlockEntity> {

    private static TransparencyStateShard ANVIL_TRANSPARENCY;
    private static RenderType ANVIL;

    public AnvilRenderer(Context context) {
        super(context);
    }

    @Override
    public void render(AnvilBlockEntity tile, float partialTick, PoseStack matrix, MultiBufferSource buffer,
            Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlayLight) {
        if (tile.getLevel() == null)
            return;

        matrix.pushPose();
        renderSkullEmissive(tile, partialTick, matrix, buffer, mc, level, player, light, overlayLight);
        matrix.popPose();

        matrix.pushPose();
        renderSkullSolid(tile, partialTick, matrix, buffer, mc, level, player, light, overlayLight);
        matrix.popPose();

        matrix.pushPose();
        renderRecipeItem(tile, partialTick, matrix, buffer, mc, level, player, light, overlayLight);
        matrix.popPose();

        matrix.pushPose();
        renderHotGlow(tile, partialTick, matrix, buffer, mc, level, player, light, overlayLight);
        matrix.popPose();
    }

    public void renderSkullEmissive(AnvilBlockEntity tile, float partialTicks, PoseStack matrix,
            MultiBufferSource buffer, Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlay) {
        matrix.pushPose();
        if (!AnvilBlockEntity.INPUT.getItemStack(tile.getInventory()).isEmpty()) {
            matrix.translate(0.5, 0.5, 0.5);
            Direction facing = tile.getCurrentFacing();
            if (facing == Direction.NORTH || facing == Direction.SOUTH)
                matrix.mulPose(Axis.YP.rotationDegrees(90));
            matrix.translate(-0.5, -0.5, -0.5);

            renderSpecialModel(tile, BasicModels.SKULLEM.getModel(), matrix, buffer.getBuffer(
                    RenderType.entityTranslucentEmissive(WitherUtils.loc("textures/block/emissive/red.png"))));
        }
        matrix.popPose();
    }

    public void renderSkullSolid(AnvilBlockEntity tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
            Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlay) {
        float time = Minecraft.getInstance().level.getLevelData().getGameTime() + partialTicks;
        float offset = (float) Math.sin(time * 0.1f) / 30.f;

        offset = !AnvilBlockEntity.INPUT.getItemStack(tile.getInventory()).isEmpty() ? offset * 360 : 0;

        double x = Vector3.CENTER.x;
        double y = Vector3.CENTER.y;
        double z = Vector3.CENTER.z;

        matrix.translate(x, y, z);

        Direction facing = tile.getCurrentFacing();
        if (facing == Direction.NORTH || facing == Direction.SOUTH)
            matrix.mulPose(Axis.YP.rotationDegrees(90));

        matrix.pushPose();

        matrix.mulPose(Axis.YP.rotationDegrees(0));

        matrix.scale(0.5f, 0.5f, 0.5f);
        matrix.translate(-0.5, -0.5, -0.5);

        matrix.translate(0.0, 0.4, -0.75);
        renderSpecialModel(tile, BasicModels.SKULLUP.getModel(), matrix, buffer.getBuffer(RenderType.cutout()));

        matrix.translate(0.0, -0.1, 1.5);
        matrix.mulPose(Axis.XP.rotationDegrees(5));
        matrix.mulPose(Axis.XP.rotationDegrees(offset));
        renderSpecialModel(tile, BasicModels.SKULLLW.getModel(), matrix, buffer.getBuffer(RenderType.cutout()));

        matrix.popPose();

        matrix.pushPose();

        matrix.mulPose(Axis.YP.rotationDegrees(180));

        matrix.scale(0.5f, 0.5f, 0.5f);
        matrix.translate(-0.5, -0.5, -0.5);

        matrix.translate(0.0, 0.4, -0.75);
        renderSpecialModel(tile, BasicModels.SKULLUP.getModel(), matrix, buffer.getBuffer(RenderType.cutout()));

        matrix.translate(0.0, -0.1, 1.5);
        matrix.mulPose(Axis.XP.rotationDegrees(5));
        matrix.mulPose(Axis.XP.rotationDegrees(offset));
        renderSpecialModel(tile, BasicModels.SKULLLW.getModel(), matrix, buffer.getBuffer(RenderType.cutout()));

        matrix.popPose();
    }

    public void renderRecipeItem(AnvilBlockEntity tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
            Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlay) {
        matrix.pushPose();

        ItemStack stack = AnvilBlockEntity.INPUT.getItemStack(tile.getInventory());

        if (!stack.isEmpty()) {
            double x = Vector3.CENTER.x - 0.0F;
            double y = Vector3.CENTER.y - 0.0F;
            double z = Vector3.CENTER.z - 0.0F;

            matrix.translate(x, y, z);
            matrix.translate(0, 0.5, 0);

            // rotateMatrixForDirection(matrix, tile.getCurrentFacing());
            if (stack.getItem() instanceof BlockItem) {
                matrix.translate(0, 0.5f / 4d, 0);
            } else {
                matrix.translate(0, 0.025, 0);
                matrix.mulPose(Axis.XP.rotationDegrees(90));
            }
            matrix.scale(0.5f, 0.5f, 0.5f);

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedModel = itemRenderer.getModel(stack, mc.level, null, 0);
            itemRenderer.render(stack, ItemDisplayContext.FIXED, true, matrix, buffer, light, overlay, bakedModel);
        }

        matrix.popPose();
    }

    public void renderHotGlow(AnvilBlockEntity tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer,
            Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlay) {
        matrix.pushPose();
        matrix.translate(0.5, 0.5, 0.5);
        Direction facing = tile.getCurrentFacing();
        if (facing == Direction.NORTH || facing == Direction.SOUTH)
            matrix.mulPose(Axis.YP.rotationDegrees(90));
        matrix.translate(-0.5, -0.5, -0.5);

        matrix.translate(0.0, 0.005, 0.0);

        ANVIL_TRANSPARENCY = new TransparencyStateShard("ghost_transparency",
                () -> {
                    RenderSystem.enableBlend();
                    RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA,
                            GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
                    GL14.glBlendColor(1.0F, 1.0F, 1.0F, tile.getHotCounter());
                },
                () -> {
                    GL14.glBlendColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                    RenderSystem.defaultBlendFunc();
                });
        ANVIL = RenderType.create("anvil_hot", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false,
                true, RenderType.CompositeState.builder()
                        .setLightmapState(RenderStateShard.LIGHTMAP)
                        .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                        .setTransparencyState(ANVIL_TRANSPARENCY)
                        .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                        .createCompositeState(false));

        renderSpecialModel(tile, BasicModels.ANVIL_HOT.getModel(), matrix, buffer.getBuffer(ANVIL));
        matrix.popPose();
    }

    public void renderSpecialModel(AnvilBlockEntity tile, BakedModel BakedModel, PoseStack matrix,
            VertexConsumer vertexConsumer) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.renderModelLists(BakedModel, ItemStack.EMPTY,
                LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos().above()), OverlayTexture.NO_OVERLAY,
                matrix, vertexConsumer);
    }

    public Quaternionf getFacing(@Nullable Direction facing) {
        if (facing != null) {
            if (facing == Direction.NORTH) {
            } else if (facing == Direction.EAST)
                return Axis.YN.rotationDegrees(90);
            else if (facing == Direction.WEST)
                return Axis.YP.rotationDegrees(90);
            else if (facing == Direction.UP)
                return Axis.XP.rotationDegrees(90);
            else if (facing == Direction.DOWN)
                return Axis.XN.rotationDegrees(90);
        }
        return Axis.YN.rotationDegrees(180);
    }
}
