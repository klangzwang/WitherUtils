package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import geni.withercore.init.WUTBlocks;
import geni.witherutils.common.world.level.block.rack.terminal.TerminalBlock;
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
public class RackPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 620;

    private static final int STEP_1_CASING_FRAME = 0;
    private static final int STEP_2_CORE_INSERT = 120;
    private static final int STEP_3_TERMINAL_SEAL = 250;
    private static final int STEP_4_FORMATION = 380;
    private static final int STEP_5_PIPING = 500;

    private final Map<BlockPos, BlockState> baseFloorBlocks = new HashMap<>();

    public RackPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.rack.title"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 24.0f;
        this.cameraZoom = 1.0f;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    baseFloorBlocks.put(new BlockPos(x, -1, z), Blocks.POLISHED_DEEPSLATE_SLAB.defaultBlockState());
                } else {
                    baseFloorBlocks.put(new BlockPos(x, -1, z), Blocks.SMOOTH_STONE_SLAB.defaultBlockState());
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
        return new int[]{STEP_1_CASING_FRAME, STEP_2_CORE_INSERT, STEP_3_TERMINAL_SEAL, STEP_4_FORMATION, STEP_5_PIPING};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.rack.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF818CF8;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : baseFloorBlocks.entrySet()) {
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

        renderMultiblockGrid(poseStack, bufferSource, blockRenderer, smoothTick);

        if (sceneTicks >= STEP_4_FORMATION) {
            renderFormationAura(poseStack, bufferSource, smoothTick);
        }

        if (sceneTicks >= STEP_5_PIPING) {
            renderPipingFlow(poseStack, bufferSource, smoothTick);
        }
    }

    private void renderMultiblockGrid(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        BlockState caseState = WUTBlocks.RACK_CASE.get().defaultBlockState();

        // Bottom Layer (Y = 0)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                float spawnTick = Math.abs(x) * 4.0f + Math.abs(z) * 4.0f;
                float age = Math.max(0.0f, smoothTick - spawnTick);
                float drop = Mth.clamp(age / 12.0f, 0.0f, 1.0f);
                float yOffset = (1.0f - easeOutBack(drop)) * 2.0f;

                poseStack.pushPose();
                poseStack.translate(x, 0.0f + yOffset, z);
                blockRenderer.renderSingleBlock(caseState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();
            }
        }

        // Middle Layer (Y = 1)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) {
                    if (sceneTicks >= STEP_2_CORE_INSERT) {
                        renderCenterController(poseStack, bufferSource, blockRenderer, smoothTick);
                    }
                    continue;
                }

                boolean isTerminalPos = (x == 0 && z == 1);
                BlockState blockToRender = caseState;

                if (isTerminalPos && sceneTicks >= STEP_3_TERMINAL_SEAL) {
                    boolean formed = sceneTicks >= STEP_4_FORMATION;
                    blockToRender = WUTBlocks.RACK_TERMINAL.get().defaultBlockState()
                            .setValue(TerminalBlock.FACING, Direction.SOUTH)
                            .setValue(TerminalBlock.FORMED, formed);
                }

                float spawnTick = STEP_1_CASING_FRAME + 25.0f + (Math.abs(x) * 3.0f + Math.abs(z) * 3.0f);
                float age = Math.max(0.0f, smoothTick - spawnTick);
                float drop = Mth.clamp(age / 12.0f, 0.0f, 1.0f);
                float yOffset = (1.0f - easeOutBack(drop)) * 2.0f;

                poseStack.pushPose();
                poseStack.translate(x, 1.0f + yOffset, z);
                blockRenderer.renderSingleBlock(blockToRender, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();
            }
        }

        // Top Layer (Y = 2)
        if (sceneTicks >= STEP_3_TERMINAL_SEAL) {
            float stepProgress = smoothTick - STEP_3_TERMINAL_SEAL;
            float dropProgress = Mth.clamp(stepProgress / 18.0f, 0.0f, 1.0f);
            float yOffset = (1.0f - easeOutBack(dropProgress)) * 3.0f;

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    poseStack.pushPose();
                    poseStack.translate(x, 2.0f + yOffset, z);
                    blockRenderer.renderSingleBlock(caseState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                    poseStack.popPose();
                }
            }
        }
    }

    private void renderCenterController(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_2_CORE_INSERT;
        float insertProgress = Mth.clamp(stepProgress / 20.0f, 0.0f, 1.0f);
        float yOffset = (1.0f - easeOutBack(insertProgress)) * 3.5f;

        int typeIndex = ((int) ((smoothTick - STEP_2_CORE_INSERT) / 40.0f)) % 3;
        BlockState controllerState;
        if (typeIndex == 0) {
            controllerState = WUTBlocks.RACKITEM_CONTROLLER.get().defaultBlockState();
        } else if (typeIndex == 1) {
            controllerState = WUTBlocks.RACKFLUID_CONTROLLER.get().defaultBlockState();
        } else {
            controllerState = WUTBlocks.RACKENERGY_CONTROLLER.get().defaultBlockState();
        }

        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f + yOffset, 0.0f);

        if (insertProgress < 1.0f) {
            float s = 1.0f + Mth.sin(smoothTick * 0.2f) * 0.05f;
            poseStack.translate(0.5f, 0.5f, 0.5f);
            poseStack.scale(s, s, s);
            poseStack.translate(-0.5f, -0.5f, -0.5f);
        }

        blockRenderer.renderSingleBlock(
                controllerState,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();
    }

    private void renderFormationAura(PoseStack poseStack, MultiBufferSource bufferSource, float smoothTick) {
        float auraT = smoothTick * 0.05f;
        for (int i = 0; i < 4; i++) {
            float angle = auraT + (i * (float) Math.PI * 0.5f);
            float rad = 1.8f;
            float px = (float) Math.cos(angle) * rad;
            float pz = (float) Math.sin(angle) * rad;
            float py = 1.0f + (float) Math.sin(auraT * 2.0f + i) * 0.4f;

            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.scale(0.3F, 0.3F, 0.3F);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 8.0f));

            ItemStack soulStar = new ItemStack(Items.NETHER_STAR);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    soulStar,
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
    }

    private void renderPipingFlow(PoseStack poseStack, MultiBufferSource bufferSource, float smoothTick) {
        for (int i = 0; i < 3; i++) {
            float cycle = ((smoothTick * 0.08f) + (i * 0.33f)) % 1.0f;
            float pz = 1.0f + (cycle * 1.5f);

            poseStack.pushPose();
            poseStack.translate(0.0f, 1.0f, pz);
            poseStack.scale(0.2F, 0.2F, 0.2F);
            ItemStack redstone = new ItemStack(Items.REDSTONE);
            Minecraft.getInstance().getItemRenderer().renderStatic(
                    redstone,
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
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks >= STEP_2_CORE_INSERT && sceneTicks < STEP_3_TERMINAL_SEAL) {
            renderControllerSelectorHUD(graphics, smoothTick);
        }

        if (sceneTicks < STEP_2_CORE_INSERT) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 0.5f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.rack.step1.title",
                    I18n.get("ponder.witherutils.rack.step1.text"),
                    "§6»BlockHead Industry: When regular chests overflow, it's time to build a warehouse in a cube!«",
                    0xFFF59E0B, smoothTick, STEP_1_CASING_FRAME);
        } else if (sceneTicks < STEP_3_TERMINAL_SEAL) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.5f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.rack.step2.title",
                    I18n.get("ponder.witherutils.rack.step2.text"),
                    "§d§l»One cube to rule them all: choose Item, Fluid, or Energy core to define your multiblock's purpose!«",
                    0xFFEC4899, smoothTick, STEP_2_CORE_INSERT);
        } else if (sceneTicks < STEP_4_FORMATION) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.5f, 1.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.rack.step3.title",
                    I18n.get("ponder.witherutils.rack.step3.text"),
                    "§b§l»Don't forget the Terminal! A warehouse without doors is just an expensive concrete paperweight!«",
                    0xFF00ABE2, smoothTick, STEP_3_TERMINAL_SEAL);
        } else if (sceneTicks < STEP_5_PIPING) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 2.5f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.rack.step4.title",
                    I18n.get("ponder.witherutils.rack.step4.text"),
                    "§a§l»POOF! 27 blocks snap into a unified quantum collective!«",
                    0xFF10B981, smoothTick, STEP_4_FORMATION);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.5f, 1.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.rack.step5.title",
                    I18n.get("ponder.witherutils.rack.step5.text"),
                    "§e§l»Pipe in from any side. High throughput, zero clutter, 100% industrial efficiency!«",
                    0xFF818CF8, smoothTick, STEP_5_PIPING);
        }
    }

    private void renderControllerSelectorHUD(GuiGraphics graphics, float smoothTick) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 120;
        int hudH = 84;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF818CF8);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Controller Cores").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF818CF8, false);

        int currentType = ((int) ((smoothTick - STEP_2_CORE_INSERT) / 40.0f)) % 3;

        renderOptionRow(graphics, hudX + 8, hudY + 22, new ItemStack(WUTBlocks.RACKITEM_CONTROLLER.get()), "Item Bank", currentType == 0);
        renderOptionRow(graphics, hudX + 8, hudY + 42, new ItemStack(WUTBlocks.RACKFLUID_CONTROLLER.get()), "Fluid Tank", currentType == 1);
        renderOptionRow(graphics, hudX + 8, hudY + 62, new ItemStack(WUTBlocks.RACKENERGY_CONTROLLER.get()), "Energy Cell", currentType == 2);
    }

    private void renderOptionRow(GuiGraphics graphics, int x, int y, ItemStack icon, String label, boolean active) {
        if (active) {
            graphics.fill(x - 2, y - 2, x + 104, y + 16, 0x44818CF8);
            graphics.fill(x - 2, y - 2, x, y + 16, 0xFF818CF8);
        }
        graphics.renderItem(icon, x, y - 2);
        graphics.drawString(this.font, label, x + 20, y + 2, active ? 0xFFFFFFFF : 0xFF94A3B8, false);
    }
}
