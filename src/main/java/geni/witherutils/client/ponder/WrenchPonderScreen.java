package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.level.block.FloorSensorBlock;
import geni.witherutils.common.world.level.block.SteelBeamBlock;
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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class WrenchPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 480;

    private static final int STEP_1_WRENCH_INTRO = 0;
    private static final int STEP_2_ROTATE_BLOCKS = 120;
    private static final int STEP_3_UNMASK_CAMOUFLAGE = 240;
    private static final int STEP_4_MAINTENANCE = 360;

    private final Map<BlockPos, BlockState> platformTiles = new HashMap<>();

    public WrenchPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.literal("Industrial Wrench Engineering"), targetStack, parentScreen);
        this.cameraYaw = -36.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 1.05f;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                platformTiles.put(new BlockPos(x, 0, z), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
            }
        }
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_WRENCH_INTRO, STEP_2_ROTATE_BLOCKS, STEP_3_UNMASK_CAMOUFLAGE, STEP_4_MAINTENANCE};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "Industrial Wrench" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return "╘ BlockHead Industry — Block Rotation & Maintenance Manual";
    }

    @Override
    protected int getAccentColor() {
        return 0xFF0284C7;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : platformTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        if (sceneTicks < STEP_2_ROTATE_BLOCKS) {
            renderFloatingWrenchTool(poseStack, bufferSource, smoothTick);
        } else if (sceneTicks < STEP_3_UNMASK_CAMOUFLAGE) {
            renderRotationDemonstration(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_4_MAINTENANCE) {
            renderCamouflageRemovalDemonstration(poseStack, bufferSource, blockRenderer, smoothTick);
        } else {
            renderMaintenanceInspection(poseStack, bufferSource, blockRenderer, smoothTick);
        }
    }

    private void renderFloatingWrenchTool(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float smoothTick) {
        poseStack.pushPose();
        float bobY = 1.35f + (float) Math.sin(smoothTick * 0.08f) * 0.12f;
        poseStack.translate(0.0f, bobY, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 2.5f));
        poseStack.scale(0.9f, 0.9f, 0.9f);
        ItemStack wrench = new ItemStack(WUTItems.WRENCH.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(wrench, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
        poseStack.popPose();
    }

    private void renderRotationDemonstration(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_2_ROTATE_BLOCKS;
        int quarterTurn = ((int) (stepProgress / 30.0f)) % 4;
        Direction facing = Direction.from2DDataValue(quarterTurn);

        BlockState beamState = WUTBlocks.STEEL_BEAM.get().defaultBlockState()
                .setValue(SteelBeamBlock.FACING, facing);

        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(beamState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        // Wrench tapping the beam to rotate it
        float clickPhase = (stepProgress % 30.0f);
        if (clickPhase < 12.0f) {
            poseStack.pushPose();
            float rotWrench = (float) Math.sin((clickPhase / 12.0f) * Math.PI) * 25.0f;
            poseStack.translate(0.6f, 1.8f, 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(rotWrench));
            poseStack.scale(0.65f, 0.65f, 0.65f);
            ItemStack wrench = new ItemStack(WUTItems.WRENCH.get());
            Minecraft.getInstance().getItemRenderer().renderStatic(wrench, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderCamouflageRemovalDemonstration(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_3_UNMASK_CAMOUFLAGE;
        float cycle = stepProgress % 60.0f;
        boolean isUnmasked = cycle >= 30.0f;

        BlockState centerState = isUnmasked
                ? WUTBlocks.FLOORSENSOR.get().defaultBlockState().setValue(FloorSensorBlock.COVERED, false)
                : Blocks.SMOOTH_STONE.defaultBlockState();

        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(centerState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        if (cycle >= 25.0f && cycle < 35.0f) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 1.9f, 0.5f);
            poseStack.scale(0.7f, 0.7f, 0.7f);
            ItemStack wrench = new ItemStack(WUTItems.WRENCH.get());
            Minecraft.getInstance().getItemRenderer().renderStatic(wrench, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderMaintenanceInspection(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        BlockState machineState = WUTBlocks.RACK_CASE.get().defaultBlockState();
        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(machineState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        // Pulsing cog particles
        for (int i = 0; i < 3; i++) {
            float angle = (smoothTick * 0.1f) + (i * 2.09f);
            float px = (float) Math.cos(angle) * 0.7f;
            float pz = (float) Math.sin(angle) * 0.7f;
            float py = 1.5f + (float) Math.sin(smoothTick * 0.15f + i) * 0.2f;

            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 10.0f));
            ItemStack gear = new ItemStack(WUTItems.WITHERSTEEL_GEAR.get());
            Minecraft.getInstance().getItemRenderer().renderStatic(gear, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingRecipeHUD(graphics);

        if (sceneTicks < STEP_2_ROTATE_BLOCKS) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.4f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.wrench.step1.title",
                    I18n.get("ponder.witherutils.wrench.step1.text"),
                    "§b»BlockHead Industry: Right-click to rotate, shift-click to dismantle. Every engineer needs one!«",
                    0xFF0284C7, smoothTick, STEP_1_WRENCH_INTRO);
        } else if (sceneTicks < STEP_3_UNMASK_CAMOUFLAGE) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.6f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.wrench.step2.title",
                    I18n.get("ponder.witherutils.wrench.step2.text"),
                    "§6§l»Turn beams and directional sensors on the fly with a single rhythmic twist!«",
                    0xFFF59E0B, smoothTick, STEP_2_ROTATE_BLOCKS);
        } else if (sceneTicks < STEP_4_MAINTENANCE) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.wrench.step3.title",
                    I18n.get("ponder.witherutils.wrench.step3.text"),
                    "§a§l»Discovered a hidden sensor? One sneak-twist unmasks the illusion immediately.«",
                    0xFF10B981, smoothTick, STEP_3_UNMASK_CAMOUFLAGE);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.6f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.wrench.step4.title",
                    I18n.get("ponder.witherutils.wrench.step4.text"),
                    "§d§l»Zero pickaxe damage loss! Dismantle high-tech infrastructure with professional care.«",
                    0xFFEC4899, smoothTick, STEP_4_MAINTENANCE);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 114;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF0284C7);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Wrench Recipe").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF0284C7, false);

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

        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_INGOT.get()), slotStartX, slotStartY);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_INGOT.get()), slotStartX + 40, slotStartY);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_ROD.get()), slotStartX + 20, slotStartY + 20);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_ROD.get()), slotStartX + 20, slotStartY + 40);

        int outX = hudX + 74;
        int outY = hudY + 42;
        graphics.drawString(this.font, "➔", outX - 3, outY + 4, 0x94A3B8, false);
        graphics.fill(outX + 11, outY - 2, outX + 31, outY + 18, 0xFF0284C7);
        graphics.fill(outX + 12, outY - 1, outX + 30, outY + 17, 0xFF0F172A);
        ItemStack wrench = new ItemStack(WUTItems.WRENCH.get());
        graphics.renderFakeItem(wrench, outX + 13, outY);
    }
}
