/*
package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DoorsPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 520;

    private static final int STEP_1_CATALOG = 0;
    private static final int STEP_2_FRAMING = 125;
    private static final int STEP_3_DUAL_ACCESS = 250;
    private static final int STEP_4_DOUBLE_DOORS = 385;

    private final Map<BlockPos, BlockState> doorwayWallTiles = new HashMap<>();

    public DoorsPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.doors.title"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 1.0f;

        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 3; y++) {
                if (y == 0) {
                    doorwayWallTiles.put(new BlockPos(x, 0, 0), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    doorwayWallTiles.put(new BlockPos(x, 0, 1), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                    doorwayWallTiles.put(new BlockPos(x, 0, -1), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                } else if (Math.abs(x) >= 2 || y >= 3) {
                    doorwayWallTiles.put(new BlockPos(x, y, 0), Blocks.POLISHED_DEEPSLATE.defaultBlockState());
                }
            }
        }
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_CATALOG, STEP_2_FRAMING, STEP_3_DUAL_ACCESS, STEP_4_DOUBLE_DOORS};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "Industrial Security Doors" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.doors.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF3B82F6;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : doorwayWallTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            if (sceneTicks >= STEP_4_DOUBLE_DOORS && pos.getY() >= 1 && pos.getY() <= 2 && (pos.getX() == -1 || pos.getX() == 0 || pos.getX() == 1)) {
                continue;
            } else if (sceneTicks < STEP_4_DOUBLE_DOORS && pos.getY() >= 1 && pos.getY() <= 2 && pos.getX() != 0) {
                poseStack.pushPose();
                poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
                poseStack.popPose();
                continue;
            }

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        renderSecurityDoorModels(poseStack, bufferSource, blockRenderer, smoothTick);
    }

    private Block getActiveDoorBlock() {
        if (!targetStack.isEmpty()) {
            Block b = Block.byItem(targetStack.getItem());
            if (b instanceof DoorsBlock) {
                return b;
            }
        }

        if (sceneTicks < STEP_2_FRAMING) {
            int cycle = (sceneTicks / 25) % 5;
            return switch (cycle) {
                case 0 -> WUTBlocks.CASED_DOOR.get();
                case 1 -> WUTBlocks.STRIPED_DOOR.get();
                case 2 -> WUTBlocks.CREEP_DOOR.get();
                case 3 -> WUTBlocks.STEEL_DOOR.get();
                default -> WUTBlocks.LIRON_DOOR.get();
            };
        }
        return WUTBlocks.STEEL_DOOR.get();
    }

    private void renderSecurityDoorModels(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        Block doorBlock = getActiveDoorBlock();

        boolean hasFrame = sceneTicks >= STEP_2_FRAMING;
        boolean isOpen = false;

        if (sceneTicks >= STEP_3_DUAL_ACCESS && sceneTicks < STEP_4_DOUBLE_DOORS) {
            float phaseProgress = (sceneTicks - STEP_3_DUAL_ACCESS) / (float) (STEP_4_DOUBLE_DOORS - STEP_3_DUAL_ACCESS);
            isOpen = (phaseProgress > 0.25f && phaseProgress < 0.75f);
        } else if (sceneTicks >= STEP_4_DOUBLE_DOORS) {
            float phaseProgress = (sceneTicks - STEP_4_DOUBLE_DOORS) / (float) (TOTAL_TICKS - STEP_4_DOUBLE_DOORS);
            isOpen = (phaseProgress > 0.2f && phaseProgress < 0.8f);
        }

        if (sceneTicks < STEP_4_DOUBLE_DOORS) {
            renderSingleDoorUnit(poseStack, bufferSource, blockRenderer, doorBlock, 0, Direction.NORTH, DoorHingeSide.LEFT, isOpen, hasFrame);
        } else {
            renderSingleDoorUnit(poseStack, bufferSource, blockRenderer, doorBlock, 0, Direction.NORTH, DoorHingeSide.RIGHT, isOpen, hasFrame);
            renderSingleDoorUnit(poseStack, bufferSource, blockRenderer, doorBlock, 1, Direction.NORTH, DoorHingeSide.LEFT, isOpen, hasFrame);
        }
    }

    private void renderSingleDoorUnit(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer,
                                      Block doorBlock, int x, Direction facing, DoorHingeSide hinge, boolean isOpen, boolean hasFrame) {
        BlockState lowerState = doorBlock.defaultBlockState()
                .setValue(DoorsBlock.FACING, facing)
                .setValue(DoorsBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorsBlock.HINGE, hinge)
                .setValue(DoorsBlock.OPEN, isOpen)
                .setValue(DoorsBlock.HAS_FRAME, hasFrame);

        BlockState upperState = doorBlock.defaultBlockState()
                .setValue(DoorsBlock.FACING, facing)
                .setValue(DoorsBlock.HALF, DoubleBlockHalf.UPPER)
                .setValue(DoorsBlock.HINGE, hinge)
                .setValue(DoorsBlock.OPEN, isOpen)
                .setValue(DoorsBlock.HAS_FRAME, hasFrame);

        poseStack.pushPose();
        poseStack.translate(x, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(lowerState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(x, 2.0f, 0.0f);
        blockRenderer.renderSingleBlock(upperState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingRecipeHUD(graphics);

        if (sceneTicks < STEP_2_FRAMING) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.doors.step1.title",
                    I18n.get("ponder.witherutils.doors.step1.text"),
                    "§9»BlockHead Industry: 5 specialized reinforced alloys. Industrial aesthetics meet uncompromising security!«",
                    0xFF3B82F6, smoothTick, STEP_1_CATALOG);
        } else if (sceneTicks < STEP_3_DUAL_ACCESS) {
            Vector4f anchor = projectToScreen(sceneMatrix, -0.5f, 2.0f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.doors.step2.title",
                    I18n.get("ponder.witherutils.doors.step2.text"),
                    "§6§l»Snug fit! Wall casing frames attach automatically whenever masonry or metal borders the hinge.«",
                    0xFFF59E0B, smoothTick, STEP_2_FRAMING);
        } else if (sceneTicks < STEP_4_DOUBLE_DOORS) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.3f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.doors.step3.title",
                    I18n.get("ponder.witherutils.doors.step3.text"),
                    "§a§l»No annoying wooden buttons needed! Click with your bare hand or wire into automation.«",
                    0xFF10B981, smoothTick, STEP_3_DUAL_ACCESS);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.8f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.doors.step4.title",
                    I18n.get("ponder.witherutils.doors.step4.text"),
                    "§c§l»Double the door, double the prestige! Symmetrical portals for grand factory gates.«",
                    0xFFEF4444, smoothTick, STEP_4_DOUBLE_DOORS);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 114;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF3B82F6);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        Block activeDoor = getActiveDoorBlock();
        String doorTitle = activeDoor.getName().getString();

        graphics.drawString(this.font, Component.literal(doorTitle).withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF60A5FA, false);

        int slotStartX = hudX + 8;
        int slotStartY = hudY + 22;
        int slotSize = 20;

        ItemStack[][] grid = new ItemStack[3][3];
        grid[0][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[0][1] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[1][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[1][1] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[2][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        grid[2][1] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());

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
        graphics.fill(outX + 11, outY - 2, outX + 31, outY + 18, 0xFF3B82F6);
        graphics.fill(outX + 12, outY - 1, outX + 30, outY + 17, 0xFF0F172A);
        graphics.renderFakeItem(new ItemStack(activeDoor), outX + 13, outY);
        graphics.renderItemDecorations(this.font, new ItemStack(activeDoor), outX + 13, outY);
    }
}
*/
