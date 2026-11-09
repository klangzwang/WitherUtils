package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AngelPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 500;

    private static final int STEP_1_MIDAIR = 0;
    private static final int STEP_2_UPDRAFT = 120;
    private static final int STEP_3_FALLING = 240;
    private static final int STEP_4_FEATHER = 370;

    private final Map<BlockPos, BlockState> platformBlocks = new HashMap<>();

    public AngelPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.angel.title"), targetStack, parentScreen);
        this.cameraYaw = -35.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 1.05f;
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_MIDAIR, STEP_2_UPDRAFT, STEP_3_FALLING, STEP_4_FEATHER};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.angel.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF38BDF8;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        // Platform
        for (Map.Entry<BlockPos, BlockState> entry : platformBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(
                    state,
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    RenderType.cutout()
            );
            poseStack.popPose();
        }

        // Stages
        if (sceneTicks < STEP_2_UPDRAFT) {
            renderStage1MidAirPlacement(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_3_FALLING) {
            renderStage2UpdraftElevator(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_4_FEATHER) {
            renderStage3StructuralInstability(poseStack, bufferSource, blockRenderer, smoothTick);
        } else {
            renderStage4FeatherCushion(poseStack, bufferSource, blockRenderer, smoothTick);
        }
    }

    private void renderStage1MidAirPlacement(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_1_MIDAIR;
        float placeProgress = Mth.clamp(stepProgress / 20.0f, 0.0f, 1.0f);
        float scale = easeOutBack(placeProgress);

        poseStack.pushPose();
        float hover = Mth.sin(smoothTick * 0.08f) * 0.08f;
        poseStack.translate(0.0D, 1.8D + hover, 0.0D);

        if (placeProgress < 1.0f) {
            poseStack.translate(0.5D, 0.5D, 0.5D);
            poseStack.scale(scale, scale, scale);
            poseStack.translate(-0.5D, -0.5D, -0.5D);
        }

        blockRenderer.renderSingleBlock(
                WUTBlocks.ANGEL.get().defaultBlockState(),
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();
    }

    private void renderStage2UpdraftElevator(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_2_UPDRAFT;
        float cycle = stepProgress % 50.0f;

        poseStack.pushPose();
        poseStack.translate(0.0D, 2.0D, 0.0D);
        blockRenderer.renderSingleBlock(
                WUTBlocks.ANGEL.get().defaultBlockState(),
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();

        for (int i = 0; i < 4; i++) {
            float pcycle = ((smoothTick * 0.07f) + (i * 0.25f)) % 1.0f;
            float py = 0.0f + (pcycle * 2.0f);
            float px = 0.5f + Mth.sin(smoothTick * 0.2f + i) * 0.15f;
            float pz = 0.5f + Mth.cos(smoothTick * 0.2f + i) * 0.15f;

            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.scale(0.25F, 0.25F, 0.25F);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 12.0f));

            ItemStack soulDust = new ItemStack(Items.GHAST_TEAR);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    soulDust,
                    ItemDisplayContext.GROUND,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    this.minecraft != null ? this.minecraft.level : null,
                    0
            );
            poseStack.popPose();
        }

        float tokenY = 0.0f;
        if (cycle < 20.0f) {
            tokenY = 0.0f;
        } else if (cycle >= 20.0f && cycle < 28.0f) {
            float t = (cycle - 20.0f) / 8.0f;
            tokenY = Mth.lerp(t, 0.0f, 3.0f);
        } else {
            tokenY = 3.0f;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, tokenY + 0.2D, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 3.0f));
        poseStack.scale(0.65F, 0.65F, 0.65F);

        ItemStack boots = new ItemStack(Items.GOLDEN_BOOTS);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                boots,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                this.minecraft != null ? this.minecraft.level : null,
                0
        );
        poseStack.popPose();
    }

    private void renderStage3StructuralInstability(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_3_FALLING;
        float cycle = stepProgress % 60.0f;

        float blockY;
        float wobbleZ = 0.0f;

        if (cycle < 25.0f) {
            float wobblePhase = cycle / 25.0f;
            wobbleZ = Mth.sin(wobblePhase * (float) Math.PI * 8.0f) * 4.0f * wobblePhase;
            blockY = 2.0f;
        } else if (cycle >= 25.0f && cycle < 40.0f) {
            float fallT = (cycle - 25.0f) / 15.0f;
            blockY = Mth.lerp(fallT * fallT, 2.0f, 0.0f);
        } else {
            blockY = 0.0f;
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, blockY + 0.5D, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(wobbleZ));
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        blockRenderer.renderSingleBlock(
                WUTBlocks.ANGEL.get().defaultBlockState(),
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();
    }

    private void renderStage4FeatherCushion(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_4_FEATHER;
        float cycle = stepProgress % 60.0f;

        if (cycle < 20.0f) {
            float fallT = cycle / 20.0f;
            float blockY = Mth.lerp(fallT * fallT, 3.2f, 1.0f);

            poseStack.pushPose();
            poseStack.translate(0.0D, blockY, 0.0D);
            blockRenderer.renderSingleBlock(
                    WUTBlocks.ANGEL.get().defaultBlockState(),
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    RenderType.cutout()
            );
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            float hover = Mth.sin(smoothTick * 0.1f) * 0.15f;
            poseStack.translate(0.5D, 1.4D + hover, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 3.5f));
            poseStack.scale(1.3F, 1.3F, 1.3F);

            ItemStack featherStack = new ItemStack(Items.FEATHER);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    featherStack,
                    ItemDisplayContext.FIXED,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    bufferSource,
                    this.minecraft != null ? this.minecraft.level : null,
                    0
            );
            poseStack.popPose();
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.2D, 0.5D);
        poseStack.scale(0.8F, 0.8F, 0.8F);
        ItemStack helmet = new ItemStack(Items.IRON_HELMET);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                helmet,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                this.minecraft != null ? this.minecraft.level : null,
                0
        );
        poseStack.popPose();
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks < STEP_2_UPDRAFT) {
            renderCraftingRecipeHUD(graphics);
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 2.3f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.angel.step1.title",
                    I18n.get("ponder.witherutils.angel.step1.text"), 0xFF38BDF8, smoothTick, STEP_1_MIDAIR);
        } else if (sceneTicks < STEP_3_FALLING) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 2.0f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.angel.step2.title",
                    I18n.get("ponder.witherutils.angel.step2.text"), 0xFFFACC15, smoothTick, STEP_2_UPDRAFT);
        } else if (sceneTicks < STEP_4_FEATHER) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.2f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.angel.step3.title",
                    I18n.get("ponder.witherutils.angel.step3.text"), 0xFFEF4444, smoothTick, STEP_3_FALLING);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.4f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.angel.step4.title",
                    I18n.get("ponder.witherutils.angel.step4.text"), 0xFFA855F7, smoothTick, STEP_4_FEATHER);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 108;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF38BDF8);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Recipe Grid").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF38BDF8, false);

        int slotStartX = hudX + 8;
        int slotStartY = hudY + 22;
        int slotSize = 20;

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = slotStartX + (col * slotSize);
                int sy = slotStartY + (row * slotSize);
                graphics.fill(sx, sy, sx + 18, sy + 18, 0xAA1E293B);
                graphics.fill(sx, sy, sx + 18, sy + 1, 0x66475569);
                graphics.fill(sx, sy, sx + 1, sy + 18, 0x66475569);
            }
        }

        graphics.renderItem(new ItemStack(Items.FEATHER), slotStartX, slotStartY);
        graphics.renderItem(new ItemStack(Items.GOLD_NUGGET), slotStartX + 20, slotStartY);
        graphics.renderItem(new ItemStack(Items.FEATHER), slotStartX + 40, slotStartY);
        graphics.renderItem(new ItemStack(Items.GOLD_NUGGET), slotStartX, slotStartY + 20);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_CASE.get()), slotStartX + 20, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.GOLD_NUGGET), slotStartX + 40, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.FEATHER), slotStartX, slotStartY + 40);
        graphics.renderItem(new ItemStack(Items.GOLD_NUGGET), slotStartX + 20, slotStartY + 40);
        graphics.renderItem(new ItemStack(Items.FEATHER), slotStartX + 40, slotStartY + 40);

        graphics.drawString(this.font, "➔", hudX + 70, hudY + 42, 0x94A3B8, false);
        graphics.renderItem(new ItemStack(WUTBlocks.ANGEL.get()), hudX + 84, hudY + 38);
    }
}
