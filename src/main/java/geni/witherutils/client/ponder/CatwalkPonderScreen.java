package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.level.block.CatwalkBlock;
import geni.witherutils.common.world.level.block.RailingBlock;
import geni.witherutils.common.world.level.block.SteelBeamBlock;
import geni.witherutils.common.world.level.block.SteelPoleBlock;
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
public class CatwalkPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 600;

    private static final int STEP_1_POLES = 0;
    private static final int STEP_2_BEAMS = 120;
    private static final int STEP_3_CATWALK_DECK = 240;
    private static final int STEP_4_BRIDGING = 360;
    private static final int STEP_5_SAFETY_FALL = 480;

    private final Map<BlockPos, BlockState> factoryWallTiles = new HashMap<>();

    public CatwalkPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.catwalk.title"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 0.98f;

        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 4; y++) {
                factoryWallTiles.put(new BlockPos(x, y, -2), Blocks.DEEPSLATE_TILES.defaultBlockState());
            }
        }
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_POLES, STEP_2_BEAMS, STEP_3_CATWALK_DECK, STEP_4_BRIDGING, STEP_5_SAFETY_FALL};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "Industrial Steel Infrastructure" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.catwalk.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF0284C7;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : factoryWallTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        BlockState floorConcrete = Blocks.SMOOTH_STONE.defaultBlockState();
        int[][] pillarXZ = {{-1, 1}, {1, 1}};
        for (int[] xz : pillarXZ) {
            poseStack.pushPose();
            poseStack.translate(xz[0], -0.2f, xz[1]);
            blockRenderer.renderSingleBlock(floorConcrete, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        renderSteelPillars(poseStack, bufferSource, blockRenderer, smoothTick);

        if (sceneTicks >= STEP_2_BEAMS) {
            renderSteelBeams(poseStack, bufferSource, blockRenderer, smoothTick);
        }

        if (sceneTicks >= STEP_3_CATWALK_DECK) {
            renderCatwalkWalkway(poseStack, bufferSource, blockRenderer, smoothTick);
        }

        if (sceneTicks >= STEP_5_SAFETY_FALL) {
            renderSafetyRailingsAndFallSparks(poseStack, bufferSource, blockRenderer, smoothTick);
        }
    }

    private void renderSteelPillars(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        int[][] pillarXZ = {{-1, 1}, {1, 1}};

        for (int[] xz : pillarXZ) {
            int px = xz[0];
            int pz = xz[1];

            BlockState bottomPole = WUTBlocks.STEEL_POLE.get().defaultBlockState()
                    .setValue(SteelPoleBlock.FOOT, true)
                    .setValue(SteelPoleBlock.FACINGDOWN, false);

            poseStack.pushPose();
            poseStack.translate(px, 0.0f, pz);
            blockRenderer.renderSingleBlock(bottomPole, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();

            boolean hasBeamNorth = sceneTicks >= STEP_2_BEAMS;
            boolean hasBeamEast = sceneTicks >= STEP_2_BEAMS && px == -1;
            boolean hasBeamWest = sceneTicks >= STEP_2_BEAMS && px == 1;

            BlockState topPole = WUTBlocks.STEEL_POLE.get().defaultBlockState()
                    .setValue(SteelPoleBlock.FOOT, false)
                    .setValue(SteelPoleBlock.FACINGDOWN, false)
                    .setValue(SteelPoleBlock.NORTHCONN, hasBeamNorth)
                    .setValue(SteelPoleBlock.EASTCONN, hasBeamEast)
                    .setValue(SteelPoleBlock.WESTCONN, hasBeamWest);

            poseStack.pushPose();
            poseStack.translate(px, 1.0f, pz);
            blockRenderer.renderSingleBlock(topPole, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    private void renderSteelBeams(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        BlockState crossBeam = WUTBlocks.STEEL_BEAM.get().defaultBlockState()
                .setValue(SteelBeamBlock.FACING, Direction.EAST)
                .setValue(SteelBeamBlock.WALL, false)
                .setValue(SteelBeamBlock.EASTCONN, true)
                .setValue(SteelBeamBlock.WESTCONN, true);

        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 1.0f);
        blockRenderer.renderSingleBlock(crossBeam, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        int[] beamX = {-1, 1};
        for (int bx : beamX) {
            BlockState wallBeam = WUTBlocks.STEEL_BEAM.get().defaultBlockState()
                    .setValue(SteelBeamBlock.FACING, Direction.NORTH)
                    .setValue(SteelBeamBlock.WALL, true)
                    .setValue(SteelBeamBlock.SOUTHCONN, true);

            poseStack.pushPose();
            poseStack.translate(bx, 1.0f, -1.0f);
            blockRenderer.renderSingleBlock(wallBeam, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();

            BlockState midBeam = WUTBlocks.STEEL_BEAM.get().defaultBlockState()
                    .setValue(SteelBeamBlock.FACING, Direction.NORTH)
                    .setValue(SteelBeamBlock.WALL, false)
                    .setValue(SteelBeamBlock.NORTHCONN, true)
                    .setValue(SteelBeamBlock.SOUTHCONN, true);

            poseStack.pushPose();
            poseStack.translate(bx, 1.0f, 0.0f);
            blockRenderer.renderSingleBlock(midBeam, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    private void renderCatwalkWalkway(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        int maxZ = 1;
        if (sceneTicks >= STEP_4_BRIDGING) {
            float bridgeProgress = (sceneTicks - STEP_4_BRIDGING) / (float) (STEP_5_SAFETY_FALL - STEP_4_BRIDGING);
            if (bridgeProgress > 0.65f) {
                maxZ = 3;
            } else if (bridgeProgress > 0.25f) {
                maxZ = 2;
            }
        }

        for (int z = -1; z <= maxZ; z++) {
            boolean hasNorth = (z > -1);
            boolean hasSouth = (z < maxZ);

            BlockState catwalkState = WUTBlocks.STEEL_CATWALK.get().defaultBlockState()
                    .setValue(CatwalkBlock.NORTH, hasNorth)
                    .setValue(CatwalkBlock.SOUTH, hasSouth)
                    .setValue(CatwalkBlock.EAST, false)
                    .setValue(CatwalkBlock.WEST, false)
                    .setValue(CatwalkBlock.BEAMCONN, true);

            poseStack.pushPose();
            poseStack.translate(0.0f, 1.5f, z);
            blockRenderer.renderSingleBlock(catwalkState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }
    }

    private void renderSafetyRailingsAndFallSparks(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        int[][] leftRailPos = {{-1, -1}, {-1, 0}, {-1, 1}};
        for (int[] pos : leftRailPos) {
            BlockState railState = WUTBlocks.STEEL_RAILING.get().defaultBlockState()
                    .setValue(RailingBlock.FACING, Direction.EAST)
                    .setValue(RailingBlock.EAST, true);
            poseStack.pushPose();
            poseStack.translate(pos[0], 2.0f, pos[1]);
            blockRenderer.renderSingleBlock(railState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }

        float fallCycle = (smoothTick - STEP_5_SAFETY_FALL) % 50.0f;
        if (fallCycle < 14.0f) {
            for (int i = 0; i < 6; i++) {
                poseStack.pushPose();
                float sparkAngle = (i * 60.0f) * ((float) Math.PI / 180.0f);
                float sparkDist = (fallCycle / 14.0f) * 0.8f;
                float sx = 0.0f + Mth.cos(sparkAngle) * sparkDist;
                float sz = 0.0f + Mth.sin(sparkAngle) * sparkDist;
                float sy = 2.0f + (float) Math.sin(fallCycle * 0.4f) * 0.2f;

                poseStack.translate(sx, sy, sz);
                poseStack.scale(0.18f, 0.18f, 0.18f);
                ItemStack sparkItem = new ItemStack(Items.FIREWORK_STAR);
                Minecraft.getInstance().getItemRenderer().renderStatic(sparkItem, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
                poseStack.popPose();
            }
        }
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingRecipeHUD(graphics);

        if (sceneTicks < STEP_2_BEAMS) {
            Vector4f anchor = projectToScreen(sceneMatrix, -1.0f, 0.4f, 1.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.catwalk.step1.title",
                    I18n.get("ponder.witherutils.catwalk.step1.text"),
                    "§b»BlockHead Industry: If you don't anchor your pillars, your entire floor becomes a downward elevator!«",
                    0xFF0284C7, smoothTick, STEP_1_POLES);
        } else if (sceneTicks < STEP_3_CATWALK_DECK) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.5f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.catwalk.step2.title",
                    I18n.get("ponder.witherutils.catwalk.step2.text"),
                    "§6§l»Snaps onto concrete walls like an angry badger! No loose screws in BlockHead factories.«",
                    0xFFF59E0B, smoothTick, STEP_2_BEAMS);
        } else if (sceneTicks < STEP_4_BRIDGING) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.8f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.catwalk.step3.title",
                    I18n.get("ponder.witherutils.catwalk.step3.text"),
                    "§3§l»See through the grating: observe your machinery below while remaining 100% on solid ground!«",
                    0xFF06B6D4, smoothTick, STEP_3_CATWALK_DECK);
        } else if (sceneTicks < STEP_5_SAFETY_FALL) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.8f, 2.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.catwalk.step4.title",
                    I18n.get("ponder.witherutils.catwalk.step4.text"),
                    "§a§l»Bridge chasms like an acrobat! Click forward, run forward, repeat until across.«",
                    0xFF10B981, smoothTick, STEP_4_BRIDGING);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.9f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.catwalk.step5.title",
                    I18n.get("ponder.witherutils.catwalk.step5.text"),
                    "§e§l»50% fall damage reduction! Your knees will write a thank-you letter to BlockHead engineering.«",
                    0xFFEAB308, smoothTick, STEP_5_SAFETY_FALL);
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

        String recipeTitle;
        ItemStack outputStack;
        ItemStack[][] grid = new ItemStack[3][3];

        if (sceneTicks < STEP_2_BEAMS) {
            recipeTitle = "Steel Pole";
            outputStack = new ItemStack(WUTBlocks.STEEL_POLE.get(), 4);
            grid[0][1] = new ItemStack(WUTItems.WITHERSTEEL_ROD.get());
            grid[1][1] = new ItemStack(WUTItems.WITHERSTEEL_ROD.get());
            grid[2][1] = new ItemStack(WUTItems.WITHERSTEEL_ROD.get());
        } else if (sceneTicks < STEP_3_CATWALK_DECK) {
            recipeTitle = "Steel Beam";
            outputStack = new ItemStack(WUTBlocks.STEEL_BEAM.get(), 4);
            grid[1][0] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
            grid[1][1] = new ItemStack(WUTItems.WITHERSTEEL_INGOT.get());
            grid[1][2] = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        } else {
            recipeTitle = "Steel Catwalk";
            outputStack = new ItemStack(WUTBlocks.STEEL_CATWALK.get(), 6);
            grid[0][0] = new ItemStack(Items.IRON_BARS);
            grid[0][1] = new ItemStack(Items.IRON_BARS);
            grid[0][2] = new ItemStack(Items.IRON_BARS);
            grid[1][0] = new ItemStack(WUTItems.WITHERSTEEL_ROD.get());
            grid[1][1] = new ItemStack(WUTItems.WITHERSTEEL_ROD.get());
            grid[1][2] = new ItemStack(WUTItems.WITHERSTEEL_ROD.get());
        }

        graphics.drawString(this.font, Component.literal(recipeTitle).withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF38BDF8, false);

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
        graphics.fill(outX + 11, outY - 2, outX + 31, outY + 18, 0xFF0284C7);
        graphics.fill(outX + 12, outY - 1, outX + 30, outY + 17, 0xFF0F172A);
        graphics.renderFakeItem(outputStack, outX + 13, outY);
        graphics.renderItemDecorations(this.font, outputStack, outX + 13, outY);
    }
}
