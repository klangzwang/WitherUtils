/*
package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.level.block.SteelBeamBlock;
import geni.witherutils.common.world.level.block.SteelPoleBlock;
import net.minecraft.ChatFormatting;
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
public class MetalDoorPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 520;

    private static final int STEP_1_SPLIT_INTRO = 0;
    private static final int STEP_2_CHAIN_LINK = 125;
    private static final int STEP_3_SYNC_OPERATION = 250;
    private static final int STEP_4_SENSOR_AUTOMATION = 385;

    private final Map<BlockPos, BlockState> baseFloorTiles = new HashMap<>();

    public MetalDoorPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.metal_door.title"), targetStack, parentScreen);
        this.cameraYaw = -35.0f;
        this.cameraPitch = 20.0f;
        this.cameraZoom = 1.0f;

        for (int x = -2; x <= 2; x++) {
            for (int z = -1; z <= 1; z++) {
                baseFloorTiles.put(new BlockPos(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
            }
        }
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_SPLIT_INTRO, STEP_2_CHAIN_LINK, STEP_3_SYNC_OPERATION, STEP_4_SENSOR_AUTOMATION};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "Motorized Metal Door" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.metal_door.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFFF97316;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : baseFloorTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        renderSupportFrame(poseStack, bufferSource, blockRenderer);
        renderDoorPanels(poseStack, bufferSource, blockRenderer, smoothTick);

        if (sceneTicks >= STEP_4_SENSOR_AUTOMATION) {
            renderSensorAutomation(poseStack, bufferSource, blockRenderer);
        }
    }

    private void renderSupportFrame(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer) {
        int[] pillarX = {-2, 2};
        for (int px : pillarX) {
            for (int py = 1; py <= 3; py++) {
                BlockState pole = WUTBlocks.STEEL_POLE.get().defaultBlockState()
                        .setValue(SteelPoleBlock.FOOT, py == 1)
                        .setValue(SteelPoleBlock.FACINGDOWN, false);

                poseStack.pushPose();
                poseStack.translate(px, py, 0.0f);
                blockRenderer.renderSingleBlock(pole, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();
            }
        }

        for (int bx = -1; bx <= 1; bx++) {
            BlockState beam = WUTBlocks.STEEL_BEAM.get().defaultBlockState()
                    .setValue(SteelBeamBlock.FACING, Direction.EAST)
                    .setValue(SteelBeamBlock.WALL, false)
                    .setValue(SteelBeamBlock.EASTCONN, true)
                    .setValue(SteelBeamBlock.WESTCONN, true);

            poseStack.pushPose();
            poseStack.translate(bx, 3.0f, 0.0f);
            blockRenderer.renderSingleBlock(beam, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    private void renderDoorPanels(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        boolean isMultiChain = sceneTicks >= STEP_2_CHAIN_LINK;
        int minX = isMultiChain ? -1 : 0;
        int maxX = isMultiChain ? 1 : 0;

        float slideOpen = 0.0f;
        if (sceneTicks >= STEP_3_SYNC_OPERATION && sceneTicks < STEP_4_SENSOR_AUTOMATION) {
            float phaseProgress = (sceneTicks - STEP_3_SYNC_OPERATION) / (float) (STEP_4_SENSOR_AUTOMATION - STEP_3_SYNC_OPERATION);
            if (phaseProgress < 0.7f) {
                slideOpen = Mth.clamp(phaseProgress * 2.2f, 0.0f, 1.0f);
            } else {
                slideOpen = Mth.clamp((1.0f - phaseProgress) * 3.3f, 0.0f, 1.0f);
            }
        } else if (sceneTicks >= STEP_4_SENSOR_AUTOMATION) {
            float phaseProgress = (sceneTicks - STEP_4_SENSOR_AUTOMATION) / (float) (TOTAL_TICKS - STEP_4_SENSOR_AUTOMATION);
            if (phaseProgress > 0.25f && phaseProgress < 0.85f) {
                slideOpen = 1.0f;
            } else if (phaseProgress <= 0.25f) {
                slideOpen = phaseProgress / 0.25f;
            } else {
                slideOpen = (1.0f - phaseProgress) / 0.15f;
            }
        }

        for (int x = minX; x <= maxX; x++) {
            MetalDoorBlock.DoorPart part = MetalDoorBlock.DoorPart.SINGLE;
            if (isMultiChain) {
                if (x == -1) part = MetalDoorBlock.DoorPart.LEFT;
                else if (x == 0) part = MetalDoorBlock.DoorPart.MIDDLE;
                else part = MetalDoorBlock.DoorPart.RIGHT;
            }

            BlockState doorState = WUTBlocks.METAL_DOOR.get().defaultBlockState()
                    .setValue(MetalDoorBlock.FACING, Direction.NORTH)
                    .setValue(MetalDoorBlock.DOOR_PART, part)
                    .setValue(MetalDoorBlock.LIT, slideOpen > 0.1f);

            poseStack.pushPose();
            poseStack.translate(x, 1.0f - (slideOpen * 0.95f), 0.0f);
            blockRenderer.renderSingleBlock(doorState.setValue(MetalDoorBlock.TOP_FRAME, false), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();

            poseStack.pushPose();
            poseStack.translate(x, 2.0f + (slideOpen * 0.95f), 0.0f);
            blockRenderer.renderSingleBlock(doorState.setValue(MetalDoorBlock.TOP_FRAME, true), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    private void renderSensorAutomation(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer) {
        BlockState sensor = WUTBlocks.WALL_SENSOR.get().defaultBlockState();
        poseStack.pushPose();
        poseStack.translate(0.0f, 3.0f, 0.4f);
        blockRenderer.renderSingleBlock(sensor, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingRecipeHUD(graphics);

        if (sceneTicks < STEP_2_CHAIN_LINK) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.metal_door.step1.title",
                    I18n.get("ponder.witherutils.metal_door.step1.text"),
                    "§6»BlockHead Industry: Splitting blast gates: because ordinary doors are for people who like getting breached!«",
                    0xFFF97316, smoothTick, STEP_1_SPLIT_INTRO);
        } else if (sceneTicks < STEP_3_SYNC_OPERATION) {
            Vector4f anchor = projectToScreen(sceneMatrix, 1.0f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.metal_door.step2.title",
                    I18n.get("ponder.witherutils.metal_door.step2.text"),
                    "§e§l»Chain up to 8 blocks wide! From private airlocks to hangar bays for colossal airships.«",
                    0xFFFBBF24, smoothTick, STEP_2_CHAIN_LINK);
        } else if (sceneTicks < STEP_4_SENSOR_AUTOMATION) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 2.1f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.metal_door.step3.title",
                    I18n.get("ponder.witherutils.metal_door.step3.text"),
                    "§a§l»One click opens the entire front! Heavy hydraulic hiss and solid zero-collision passageway.«",
                    0xFF10B981, smoothTick, STEP_3_SYNC_OPERATION);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 3.2f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.metal_door.step4.title",
                    I18n.get("ponder.witherutils.metal_door.step4.text"),
                    "§b§l»Drive heavy machinery straight through without stopping to flip a lever!«",
                    0xFF06B6D4, smoothTick, STEP_4_SENSOR_AUTOMATION);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 114;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFFF97316);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Metal Door").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFFFB923C, false);

        int slotStartX = hudX + 8;
        int slotStartY = hudY + 22;
        int slotSize = 20;

        ItemStack[][] grid = new ItemStack[3][3];
        grid[0][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[0][1] = new ItemStack(Items.IRON_INGOT);
        grid[0][2] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[1][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[1][1] = new ItemStack(Items.REDSTONE);
        grid[1][2] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[2][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[2][1] = new ItemStack(Items.PISTON);
        grid[2][2] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int sx = slotStartX + (col * slotSize);
                int sy = slotStartY + (row * slotSize);
                graphics.fill(sx, sy, sx + 18, sy + 18, 0xAA1E293B);
                graphics.fill(sx, sy, sx + 18, sy + 1, 0x66475569);
                graphics.fill(sx, sy, sx + 1, sy + 18, 0x66475569);

                ItemStack ingredient = grid[row][col];
                if (ingredient != null && !ingredient.isEmpty()) {
                    graphics.renderFakeItem(ingredient, sx + 1, sy + 1);
                }
            }
        }

        int outX = hudX + 74;
        int outY = hudY + 42;
        graphics.drawString(this.font, "➔", outX - 3, outY + 4, 0x94A3B8, false);
        graphics.fill(outX + 11, outY - 2, outX + 31, outY + 18, 0xFFF97316);
        graphics.fill(outX + 12, outY - 1, outX + 30, outY + 17, 0xFF0F172A);
        ItemStack doorItem = new ItemStack(WUTBlocks.METAL_DOOR.get());
        graphics.renderFakeItem(doorItem, outX + 13, outY);
        graphics.renderItemDecorations(this.font, doorItem, outX + 13, outY);
    }
}
*/
