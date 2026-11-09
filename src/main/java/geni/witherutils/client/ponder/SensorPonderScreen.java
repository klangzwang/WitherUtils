package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.level.block.FloorSensorBlock;
import geni.witherutils.common.world.level.block.WallSensorBlock;
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
public class SensorPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 520;

    private static final int STEP_1_FLOOR_SCAN = 0;
    private static final int STEP_2_CAMOUFLAGE = 130;
    private static final int STEP_3_WALL_MOTION = 260;
    private static final int STEP_4_AUTOMATION = 390;

    private final Map<BlockPos, BlockState> hallwayFloor = new HashMap<>();

    public SensorPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.sensor.title"), targetStack, parentScreen);
        this.cameraYaw = -34.0f;
        this.cameraPitch = 24.0f;
        this.cameraZoom = 1.05f;

        for (int x = -2; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }
                if (Math.abs(x) == 2) {
                    hallwayFloor.put(new BlockPos(x, 0, z), Blocks.POLISHED_ANDESITE.defaultBlockState());
                } else {
                    hallwayFloor.put(new BlockPos(x, 0, z), Blocks.SMOOTH_STONE.defaultBlockState());
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
        return new int[]{STEP_1_FLOOR_SCAN, STEP_2_CAMOUFLAGE, STEP_3_WALL_MOTION, STEP_4_AUTOMATION};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.sensor.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF10B981;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : hallwayFloor.entrySet()) {
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

        if (sceneTicks < STEP_2_CAMOUFLAGE) {
            renderStage1FloorScanning(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_3_WALL_MOTION) {
            renderStage2Camouflage(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_4_AUTOMATION) {
            renderStage3WallMotionCone(poseStack, bufferSource, blockRenderer, smoothTick);
        } else {
            renderStage4HandsFreeAutomation(poseStack, bufferSource, blockRenderer, smoothTick);
        }
    }

    private void renderStage1FloorScanning(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_1_FLOOR_SCAN;
        float cycle = stepProgress % 50.0f;
        boolean triggered = (cycle >= 20.0f && cycle < 40.0f);

        BlockState floorSensorState = WUTBlocks.FLOORSENSOR.get().defaultBlockState()
                .setValue(FloorSensorBlock.LIT, triggered)
                .setValue(FloorSensorBlock.COVERED, false);

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.0D);
        blockRenderer.renderSingleBlock(floorSensorState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        float entityZ;
        if (cycle < 20.0f) {
            entityZ = Mth.lerp(cycle / 20.0f, 1.8f, 0.0f);
        } else if (cycle < 40.0f) {
            entityZ = 0.0f;
        } else {
            entityZ = Mth.lerp((cycle - 40.0f) / 10.0f, 0.0f, -1.2f);
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.25D, entityZ + 0.5D);
        poseStack.scale(0.6F, 0.6F, 0.6F);
        ItemStack boot = new ItemStack(Items.IRON_BOOTS);
        Minecraft.getInstance().getItemRenderer().renderStatic(boot, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
        poseStack.popPose();

        if (triggered) {
            poseStack.pushPose();
            poseStack.translate(0.5D, -0.6D, 0.5D);
            poseStack.scale(0.4F, 0.4F, 0.4F);
            ItemStack redstone = new ItemStack(Items.REDSTONE);
            Minecraft.getInstance().getItemRenderer().renderStatic(redstone, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderStage2Camouflage(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_2_CAMOUFLAGE;
        boolean covered = stepProgress >= 25.0f;

        BlockState sensorState;
        if (!covered) {
            sensorState = WUTBlocks.FLOORSENSOR.get().defaultBlockState()
                    .setValue(FloorSensorBlock.COVERED, false);
        } else {
            sensorState = Blocks.SMOOTH_STONE.defaultBlockState();
        }

        poseStack.pushPose();
        poseStack.translate(0.0D, 0.0D, 0.0D);
        blockRenderer.renderSingleBlock(sensorState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        if (!covered) {
            float t = stepProgress / 25.0f;
            float blockY = Mth.lerp(t, 2.0f, 0.1f);

            poseStack.pushPose();
            poseStack.translate(0.0D, blockY, 0.0D);
            poseStack.scale(0.8F, 0.8F, 0.8F);
            poseStack.translate(0.125D, 0.0D, 0.125D);
            blockRenderer.renderSingleBlock(Blocks.SMOOTH_STONE.defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        } else {
            poseStack.pushPose();
            float hover = Mth.sin(smoothTick * 0.1f) * 0.08f;
            poseStack.translate(0.5D, 1.2D + hover, 0.5D);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 3.0f));
            poseStack.scale(0.7F, 0.7F, 0.7F);
            ItemStack wrench = new ItemStack(WUTItems.WRENCH.get());
            Minecraft.getInstance().getItemRenderer().renderStatic(wrench, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderStage3WallMotionCone(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        BlockState wallState = Blocks.STONE_BRICKS.defaultBlockState();
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 2; y++) {
                if (x == 0 && y == 1) {
                    continue;
                }
                poseStack.pushPose();
                poseStack.translate(x, y, -1.0D);
                blockRenderer.renderSingleBlock(wallState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();
            }
        }

        BlockState wallSensor = WUTBlocks.WALL_SENSOR.get().defaultBlockState()
                .setValue(WallSensorBlock.FACING, Direction.SOUTH)
                .setValue(WallSensorBlock.LIT, false);

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, -1.0D);
        blockRenderer.renderSingleBlock(wallSensor, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        float scanAngle = Mth.sin(smoothTick * 0.08f) * 35.0f;
        for (int i = 1; i <= 3; i++) {
            float dist = i * 0.9f;
            float px = (float) Math.sin(Math.toRadians(scanAngle)) * dist;
            float pz = -1.0f + dist;

            poseStack.pushPose();
            poseStack.translate(0.5D + px, 1.5D - (i * 0.25f), pz);
            poseStack.scale(0.18F, 0.18F, 0.18F);
            ItemStack scanSpark = new ItemStack(Items.GLOWSTONE_DUST);
            Minecraft.getInstance().getItemRenderer().renderStatic(scanSpark, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderStage4HandsFreeAutomation(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_4_AUTOMATION;
        float cycle = stepProgress % 60.0f;
        boolean inZone = (cycle >= 15.0f && cycle < 45.0f);

        BlockState doorState = Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.OPEN, inZone);

        poseStack.pushPose();
        poseStack.translate(1.0D, 1.0D, -1.0D);
        blockRenderer.renderSingleBlock(doorState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        BlockState wallSensor = WUTBlocks.WALL_SENSOR.get().defaultBlockState()
                .setValue(WallSensorBlock.FACING, Direction.SOUTH)
                .setValue(WallSensorBlock.LIT, inZone);

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, -1.0D);
        blockRenderer.renderSingleBlock(wallSensor, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        float playerZ;
        if (cycle < 15.0f) {
            playerZ = Mth.lerp(cycle / 15.0f, 2.2f, 0.8f);
        } else if (cycle < 45.0f) {
            playerZ = 0.8f;
        } else {
            playerZ = Mth.lerp((cycle - 45.0f) / 15.0f, 0.8f, 2.2f);
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.4D, playerZ);
        poseStack.scale(0.7F, 0.7F, 0.7F);
        ItemStack playerToken = new ItemStack(Items.IRON_HELMET);
        Minecraft.getInstance().getItemRenderer().renderStatic(playerToken, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
        poseStack.popPose();

        if (inZone) {
            poseStack.pushPose();
            poseStack.translate(0.5D, 1.0D, -0.9D);
            poseStack.scale(0.35F, 0.35F, 0.35F);
            ItemStack redstonePulse = new ItemStack(Items.REDSTONE);
            Minecraft.getInstance().getItemRenderer().renderStatic(redstonePulse, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks >= STEP_1_FLOOR_SCAN && sceneTicks < STEP_2_CAMOUFLAGE) {
            renderFilterModeHUD(graphics, smoothTick);
        }

        if (sceneTicks < STEP_2_CAMOUFLAGE) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 0.2f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.sensor.step1.title",
                    I18n.get("ponder.witherutils.sensor.step1.text"),
                    "§a»BlockHead Industry: Detect your friends, trap your enemies, ignore the wandering livestock!«",
                    0xFF10B981, smoothTick, STEP_1_FLOOR_SCAN);
        } else if (sceneTicks < STEP_3_WALL_MOTION) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 0.5f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.sensor.step2.title",
                    I18n.get("ponder.witherutils.sensor.step2.text"),
                    "§b§l»Slap a stone block onto it — now you see it, now you don't! The ultimate hidden trigger.«",
                    0xFF00ABE2, smoothTick, STEP_2_CAMOUFLAGE);
        } else if (sceneTicks < STEP_4_AUTOMATION) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.3f, -0.8f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.sensor.step3.title",
                    I18n.get("ponder.witherutils.sensor.step3.text"),
                    "§e§l»Wall-mounted motion detection with precision tilt. Your base knows you're coming before you do!«",
                    0xFFF59E0B, smoothTick, STEP_3_WALL_MOTION);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 1.2f, 1.2f, -0.8f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.sensor.step4.title",
                    I18n.get("ponder.witherutils.sensor.step4.text"),
                    "§d§l»Hands full of heavy ingots? Walk right through as the doors open like magic!«",
                    0xFFEC4899, smoothTick, STEP_4_AUTOMATION);
        }
    }

    private void renderFilterModeHUD(GuiGraphics graphics, float smoothTick) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 118;
        int hudH = 76;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF10B981);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Target Filter").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF10B981, false);

        int modeCycle = ((int) (smoothTick / 40.0f)) % 3;

        renderFilterRow(graphics, hudX + 8, hudY + 20, "👤 Players", modeCycle == 0);
        renderFilterRow(graphics, hudX + 8, hudY + 36, "🧟 Hostile Mobs", modeCycle == 1);
        renderFilterRow(graphics, hudX + 8, hudY + 52, "🐷 Passive Animals", modeCycle == 2);
    }

    private void renderFilterRow(GuiGraphics graphics, int x, int y, String label, boolean active) {
        if (active) {
            graphics.fill(x - 2, y - 1, x + 102, y + 13, 0x3310B981);
            graphics.fill(x - 2, y - 1, x, y + 13, 0xFF10B981);
        }
        graphics.drawString(this.font, label, x + 4, y + 1, active ? 0xFFFFFFFF : 0xFF94A3B8, false);
    }
}
