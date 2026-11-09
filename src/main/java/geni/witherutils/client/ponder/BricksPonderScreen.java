package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import geni.withercore.init.WUTBlocks;
import geni.witherutils.common.world.level.block.BricksLavaBlock;
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
public class BricksPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 480;

    private static final int STEP_1_DARK_BRICKS = 0;
    private static final int STEP_2_LAVA_INFUSION = 120;
    private static final int STEP_3_HOT_FLOOR = 240;
    private static final int STEP_4_RAIN_QUENCH = 360;

    private final Map<BlockPos, BlockState> darkFoundationTiles = new HashMap<>();

    public BricksPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.bricks.title"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 1.05f;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x != 0 || z != 0) {
                    darkFoundationTiles.put(new BlockPos(x, 0, z), WUTBlocks.BRICKSDARK.get().defaultBlockState());
                }
            }
        }
        darkFoundationTiles.put(new BlockPos(-1, 1, -1), WUTBlocks.BRICKSDARK.get().defaultBlockState());
        darkFoundationTiles.put(new BlockPos(1, 1, -1), WUTBlocks.BRICKSDARK.get().defaultBlockState());
        darkFoundationTiles.put(new BlockPos(-1, 1, 1), WUTBlocks.BRICKSDARK.get().defaultBlockState());
        darkFoundationTiles.put(new BlockPos(1, 1, 1), WUTBlocks.BRICKSDARK.get().defaultBlockState());
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_DARK_BRICKS, STEP_2_LAVA_INFUSION, STEP_3_HOT_FLOOR, STEP_4_RAIN_QUENCH};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "Dark & Lava Bricks" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.bricks.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFFEA580C;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : darkFoundationTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        renderCenterHearthBlock(poseStack, bufferSource, blockRenderer, smoothTick);
        renderAtmosphericParticles(poseStack, bufferSource, blockRenderer, smoothTick);
    }

    private void renderCenterHearthBlock(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        if (sceneTicks < STEP_2_LAVA_INFUSION) {
            BlockState centerState = WUTBlocks.BRICKSDARK.get().defaultBlockState();
            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.0f);
            blockRenderer.renderSingleBlock(centerState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        } else {
            boolean isLit = true;
            if (sceneTicks >= STEP_4_RAIN_QUENCH) {
                float rainProgress = (sceneTicks - STEP_4_RAIN_QUENCH) / (float) (TOTAL_TICKS - STEP_4_RAIN_QUENCH);
                isLit = (rainProgress < 0.15f || rainProgress > 0.80f);
            }

            BlockState lavaState = WUTBlocks.BRICKSLAVA.get().defaultBlockState()
                    .setValue(BricksLavaBlock.LIT, isLit);

            poseStack.pushPose();
            poseStack.translate(0.0f, 0.0f, 0.0f);
            blockRenderer.renderSingleBlock(lavaState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    private void renderAtmosphericParticles(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        if (sceneTicks >= STEP_2_LAVA_INFUSION && sceneTicks < STEP_4_RAIN_QUENCH) {
            float popCycle = (smoothTick * 0.2f) % 6.28f;
            float px = (float) Math.sin(popCycle) * 0.25f;
            float pz = (float) Math.cos(popCycle * 1.5f) * 0.25f;
            float py = 1.05f + (float) Math.abs(Math.sin(popCycle * 3.0f)) * 0.3f;

            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.scale(0.12f, 0.12f, 0.12f);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 15.0f));
            blockRenderer.renderSingleBlock(Blocks.MAGMA_BLOCK.defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingHUD(graphics);

        if (sceneTicks < STEP_2_LAVA_INFUSION) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 0.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.bricks.step1.title",
                    I18n.get("ponder.witherutils.bricks.step1.text"),
                    "§8»BlockHead Industry: Blast-resistant, grim, and guaranteed to make your factory look 200% more intimidating!«",
                    0xFF64748B, smoothTick, STEP_1_DARK_BRICKS);
        } else if (sceneTicks < STEP_3_HOT_FLOOR) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.2f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.bricks.step2.title",
                    I18n.get("ponder.witherutils.bricks.step2.text"),
                    "§6§l»Infused with molten magma for that cozy industrial glow. Zero torches needed!«",
                    0xFFF97316, smoothTick, STEP_2_LAVA_INFUSION);
        } else if (sceneTicks < STEP_4_RAIN_QUENCH) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.4f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.bricks.step3.title",
                    I18n.get("ponder.witherutils.bricks.step3.text"),
                    "§c§l»Watch your step! Running on these without sneaking will singe your BlockHead toes.«",
                    0xFFEF4444, smoothTick, STEP_3_HOT_FLOOR);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.bricks.step4.title",
                    I18n.get("ponder.witherutils.bricks.step4.text"),
                    "§b§l»Sizzle! Rain temporarily quenches the veins into dark stone, but the magma core never dies.«",
                    0xFF38BDF8, smoothTick, STEP_4_RAIN_QUENCH);
        }
    }

    private void renderCraftingHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 112;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFFEA580C);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        String recipeTitle;
        ItemStack outputStack;
        ItemStack[][] grid = new ItemStack[3][3];

        if (sceneTicks < STEP_2_LAVA_INFUSION) {
            recipeTitle = "Dark Bricks";
            outputStack = new ItemStack(WUTBlocks.BRICKSDARK.get(), 4);
            grid[0][0] = new ItemStack(Items.POLISHED_DEEPSLATE);
            grid[0][1] = new ItemStack(Items.POLISHED_DEEPSLATE);
            grid[1][0] = new ItemStack(Items.POLISHED_DEEPSLATE);
            grid[1][1] = new ItemStack(Items.POLISHED_DEEPSLATE);
        } else {
            recipeTitle = "Lava Bricks";
            outputStack = new ItemStack(WUTBlocks.BRICKSLAVA.get(), 8);
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    if (r == 1 && c == 1) {
                        grid[r][c] = new ItemStack(Items.LAVA_BUCKET);
                    } else {
                        grid[r][c] = new ItemStack(WUTBlocks.BRICKSDARK.get());
                    }
                }
            }
        }

        graphics.drawString(this.font, Component.literal(recipeTitle).withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFFF97316, false);

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

                ItemStack ingredient = grid[row][col];
                if (ingredient != null && !ingredient.isEmpty()) {
                    graphics.renderFakeItem(ingredient, sx + 1, sy + 1);
                }
            }
        }

        int outX = hudX + 74;
        int outY = hudY + 42;
        graphics.drawString(this.font, "➔", outX - 3, outY + 4, 0x94A3B8, false);
        graphics.fill(outX + 11, outY - 2, outX + 31, outY + 18, 0xFFEA580C);
        graphics.fill(outX + 12, outY - 1, outX + 30, outY + 17, 0xFF0F172A);
        graphics.renderFakeItem(outputStack, outX + 13, outY);
        graphics.renderItemDecorations(this.font, outputStack, outX + 13, outY);
    }
}
