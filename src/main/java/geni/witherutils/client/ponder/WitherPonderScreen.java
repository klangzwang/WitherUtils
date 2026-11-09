package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import geni.withercore.init.WUTBlocks;
import geni.witherutils.client.block.render.entity.SolarPanelRenderer;
import geni.witherutils.common.config.common.SolarConfig;
import geni.witherutils.common.world.level.block.SolarPanelBlock;
import geni.witherutils.common.world.level.block.SolarPanelBlockItem;
import geni.witherutils.common.world.level.block.solar.SolarType;
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
import net.minecraft.world.level.block.Block;
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
public class WitherPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 560;

    private static final int STEP_1_PLACE_SINGLE = 0;
    private static final int STEP_2_ALEXA_INTRO = 45;
    private static final int STEP_3_RF_OUTPUT = 135;
    private static final int STEP_4_SUN_ANGLE = 225;
    private static final int STEP_5_PAUSE_START = 315;
    private static final int STEP_6_CONNECT_NETWORK = 345;
    private static final int STEP_7_BOTTOM_TAP = 445;

    private final Block solarBlock;
    private final SolarType solarType;
    private final Map<BlockPos, PlacedBlockEntry> sceneBlocks = new HashMap<>();

    private static class PlacedBlockEntry {
        final BlockState state;
        final int spawnTick;
        final boolean dropFromBelow;

        PlacedBlockEntry(BlockState state, int spawnTick, boolean dropFromBelow) {
            this.state = state;
            this.spawnTick = spawnTick;
            this.dropFromBelow = dropFromBelow;
        }
    }

    public WitherPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.title"), targetStack, parentScreen);

        if (targetStack.getItem() instanceof SolarPanelBlockItem solarItem) {
            this.solarBlock = solarItem.getBlock();
            this.solarType = solarItem.getType();
        } else {
            this.solarBlock = WUTBlocks.SOLARBASIC.get();
            this.solarType = SolarType.BASIC;
        }

        this.cameraYaw = -38.0f;
        this.cameraPitch = 28.0f;
        this.targetCameraPitch = 28.0f;
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_PLACE_SINGLE, STEP_2_ALEXA_INTRO, STEP_3_RF_OUTPUT, STEP_4_SUN_ANGLE, STEP_6_CONNECT_NETWORK, STEP_7_BOTTOM_TAP};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF00ABE2;
    }

    @Override
    protected void applySceneCenterOffset(PoseStack poseStack) {
        poseStack.translate(-0.5f, -1.0f, -0.5f);
    }

    @Override
    protected void onSceneTick() {
        if (!isDragging) {
            if (sceneTicks >= STEP_7_BOTTOM_TAP) {
                targetCameraPitch = 14.0f;
            } else {
                targetCameraPitch = 28.0f;
            }
        }
        super.onSceneTick();
    }

    @Override
    protected void rebuildSceneState() {
        sceneBlocks.clear();

        boolean networkPhase = sceneTicks >= STEP_6_CONNECT_NETWORK;
        boolean bottomTapPhase = sceneTicks >= STEP_7_BOTTOM_TAP;

        // 1. Center Solar Panel at (0, 1, 0)
        if (sceneTicks >= 5) {
            BlockState centerSolar = computeConnectedSolarState(0, 0, networkPhase);
            sceneBlocks.put(new BlockPos(0, 1, 0), new PlacedBlockEntry(centerSolar, 5, false));
        }

        // 2. Surrounding 8 Solar Panels in 3x3 grid
        if (networkPhase) {
            int[][] offsets = {
                    {-1, 0}, {1, 0}, {0, -1}, {0, 1},
                    {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
            };
            for (int i = 0; i < offsets.length; i++) {
                int dx = offsets[i][0];
                int dz = offsets[i][1];
                int spawnAt = STEP_6_CONNECT_NETWORK + (i * 3);
                if (sceneTicks >= spawnAt) {
                    BlockState panelState = computeConnectedSolarState(dx, dz, true);
                    sceneBlocks.put(new BlockPos(dx, 1, dz), new PlacedBlockEntry(panelState, spawnAt, false));
                }
            }
        }

        // 3. Bottom Energy Extraction Tap & Conduit
        if (bottomTapPhase) {
            sceneBlocks.put(new BlockPos(0, 0, 0), new PlacedBlockEntry(WUTBlocks.STEEL_POLE.get().defaultBlockState(), STEP_7_BOTTOM_TAP, true));
            if (sceneTicks >= STEP_7_BOTTOM_TAP + 8) {
                sceneBlocks.put(new BlockPos(0, -1, 0), new PlacedBlockEntry(WUTBlocks.CREATIVE_GENERATOR.get().defaultBlockState(), STEP_7_BOTTOM_TAP + 8, true));
            }
        } else {
            // Subtle support pillars
            sceneBlocks.put(new BlockPos(-1, 0, -1), new PlacedBlockEntry(Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 0, false));
            sceneBlocks.put(new BlockPos(1, 0, -1), new PlacedBlockEntry(Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 0, false));
            sceneBlocks.put(new BlockPos(-1, 0, 1), new PlacedBlockEntry(Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 0, false));
            sceneBlocks.put(new BlockPos(1, 0, 1), new PlacedBlockEntry(Blocks.SMOOTH_STONE_SLAB.defaultBlockState(), 0, false));
        }
    }

    private BlockState computeConnectedSolarState(int x, int z, boolean connected3x3) {
        BlockState base = solarBlock.defaultBlockState();
        if (!connected3x3) {
            return base;
        }
        boolean north = z > -1;
        boolean south = z < 1;
        boolean west = x > -1;
        boolean east = x < 1;

        return base
                .setValue(SolarPanelBlock.NORTH, north)
                .setValue(SolarPanelBlock.SOUTH, south)
                .setValue(SolarPanelBlock.WEST, west)
                .setValue(SolarPanelBlock.EAST, east)
                .setValue(SolarPanelBlock.CORNER_NORTH_WEST, false)
                .setValue(SolarPanelBlock.CORNER_NORTH_EAST, false)
                .setValue(SolarPanelBlock.CORNER_SOUTH_WEST, false)
                .setValue(SolarPanelBlock.CORNER_SOUTH_EAST, false);
    }

    private int getRfPerTickForType() {
        return switch (solarType) {
            case BASIC -> SolarConfig.SOLARBASICINPUTRF != null ? SolarConfig.SOLARBASICINPUTRF.get() : 40;
            case ADVANCED -> SolarConfig.SOLARADVINPUTRF != null ? SolarConfig.SOLARADVINPUTRF.get() : 160;
            case ULTRA -> SolarConfig.SOLARULTRAINPUTRF != null ? SolarConfig.SOLARULTRAINPUTRF.get() : 640;
            default -> 40;
        };
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float sunAlpha = 1.0f;
        if (sceneTicks >= STEP_4_SUN_ANGLE && sceneTicks < STEP_5_PAUSE_START) {
            float progress = (smoothTick - STEP_4_SUN_ANGLE) / (float) (STEP_5_PAUSE_START - STEP_4_SUN_ANGLE);
            sunAlpha = Mth.clamp(Mth.sin(progress * (float) Math.PI), 0.15f, 1.0f);
        }

        for (Map.Entry<BlockPos, PlacedBlockEntry> entry : sceneBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            PlacedBlockEntry blockEntry = entry.getValue();

            float age = Math.max(0.0f, smoothTick - blockEntry.spawnTick);
            float dropProgress = blockEntry.spawnTick == 0 ? 1.0f : Mth.clamp(age / 12.0f, 0.0f, 1.0f);
            float eased = easeOutBack(dropProgress);

            float yOffset = 0.0f;
            if (blockEntry.spawnTick > 0) {
                yOffset = (1.0f - eased) * (blockEntry.dropFromBelow ? -1.2f : 1.5f);
            }

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY() + yOffset, pos.getZ());

            if (dropProgress < 1.0f) {
                float s = Mth.clamp(eased, 0.01f, 1.05f);
                poseStack.translate(0.5f, 0.5f, 0.5f);
                poseStack.scale(s, s, s);
                poseStack.translate(-0.5f, -0.5f, -0.5f);
            }

            blockRenderer.renderSingleBlock(
                    blockEntry.state,
                    poseStack,
                    bufferSource,
                    LightTexture.FULL_BRIGHT,
                    OverlayTexture.NO_OVERLAY,
                    ModelData.EMPTY,
                    RenderType.cutout()
            );

            if (blockEntry.state.getBlock() instanceof SolarPanelBlock) {
                SolarPanelRenderer.renderStandalone(
                        blockEntry.state,
                        sunAlpha,
                        smoothTick,
                        poseStack,
                        bufferSource,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY
                );
            }

            poseStack.popPose();
        }
    }

    @Override
    protected void renderSceneOverlays(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks >= STEP_4_SUN_ANGLE && sceneTicks < STEP_5_PAUSE_START) {
            float progress = Mth.clamp((smoothTick - STEP_4_SUN_ANGLE) / (float) (STEP_5_PAUSE_START - STEP_4_SUN_ANGLE), 0.0f, 1.0f);
            float angle = progress * (float) Math.PI;

            float sunX = 0.5f - Mth.cos(angle) * 1.6f;
            float sunY = 1.3f + Mth.sin(angle) * 1.4f;
            float sunZ = 0.5f;

            Vector4f sunScreen = projectToScreen(sceneMatrix, sunX, sunY, sunZ);
            Vector4f panelScreen = projectToScreen(sceneMatrix, 0.5f, 1.08f, 0.5f);

            int sx = Math.round(sunScreen.x);
            int sy = Math.round(sunScreen.y);
            int px = Math.round(panelScreen.x);
            int py = Math.round(panelScreen.y);

            drawLine(graphics, sx, sy, px, py, 0x88FACC15);

            graphics.fill(sx - 7, sy - 7, sx + 7, sy + 7, 0x66F59E0B);
            graphics.fill(sx - 4, sy - 4, sx + 4, sy + 4, 0xFFFDE047);

            int degrees = Math.round(progress * 180.0f);
            int efficiency = Math.round(Mth.sin(angle) * 100.0f);
            String sunLabel = degrees + "° (" + efficiency + "% RF)";
            graphics.drawString(this.font, sunLabel, sx + 10, sy - 4, 0xFDE047, true);
        }

        if (sceneTicks >= STEP_7_BOTTOM_TAP + 8) {
            for (int i = 0; i < 3; i++) {
                float cycle = ((smoothTick * 0.06f) + (i * 0.33f)) % 1.0f;
                float y = 1.0f - (cycle * 1.5f);
                Vector4f pt = projectToScreen(sceneMatrix, 0.5f, y, 0.5f);
                int px = Math.round(pt.x);
                int py = Math.round(pt.y);
                graphics.fill(px - 3, py - 3, px + 3, py + 3, 0x8838BDF8);
                graphics.fill(px - 2, py - 2, px + 2, py + 2, 0xFFFACC15);
            }
        }
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks >= STEP_2_ALEXA_INTRO && sceneTicks < STEP_3_RF_OUTPUT) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.06f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.solar.step1.title",
                    I18n.get("ponder.witherutils.solar.step1.text"), 0xFF38BDF8, smoothTick, STEP_2_ALEXA_INTRO);
        } else if (sceneTicks >= STEP_3_RF_OUTPUT && sceneTicks < STEP_4_SUN_ANGLE) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.25f, 1.06f, 0.25f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.solar.step2.title",
                    I18n.get("ponder.witherutils.solar.step2.text", getRfPerTickForType()), 0xFFFACC15, smoothTick, STEP_3_RF_OUTPUT);
        } else if (sceneTicks >= STEP_4_SUN_ANGLE && sceneTicks < STEP_5_PAUSE_START) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.75f, 1.06f, 0.75f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.solar.step3.title",
                    I18n.get("ponder.witherutils.solar.step3.text"), 0xFFF59E0B, smoothTick, STEP_4_SUN_ANGLE);
        } else if (sceneTicks >= STEP_6_CONNECT_NETWORK && sceneTicks < STEP_7_BOTTOM_TAP) {
            Vector4f anchor = projectToScreen(sceneMatrix, 1.5f, 1.06f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.solar.step4.title",
                    I18n.get("ponder.witherutils.solar.step4.text", getRfPerTickForType() * 9), 0xFF10B981, smoothTick, STEP_6_CONNECT_NETWORK);
        } else if (sceneTicks >= STEP_7_BOTTOM_TAP) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 0.95f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.solar.step5.title",
                    I18n.get("ponder.witherutils.solar.step5.text"), 0xFFEC4899, smoothTick, STEP_7_BOTTOM_TAP);
        }
    }
}
