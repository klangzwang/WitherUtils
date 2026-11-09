package geni.witherutils.client.block.render.fluid;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import geni.withercore.common.world.fluid.AbstractLiquidBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/**
 * LiquidRendererPlus
 * Registers via ClientSetup / FMLClientSetupEvent and hooks into RenderLevelStageEvent.
 * Exposes a singleton INSTANCE with PoseStack and MultiBufferSource to render custom 3D models,
 * sub-surface geometry, and GeckoLib-compatible animated features directly inside active LiquidBlocks.
 */
@EventBusSubscriber()
public class LiquidRendererPlus {

    public static final LiquidRendererPlus INSTANCE = new LiquidRendererPlus();

    private boolean initialized = false;

    private LiquidRendererPlus() {}

    /**
     * Called during FMLClientSetupEvent to initialize client-side liquid rendering extensions.
     */
    public static void onClientSetup(FMLClientSetupEvent event) {
        INSTANCE.initialized = true;
    }

    /**
     * Listens to NeoForge RenderLevelStageEvent at AFTER_TRANSLUCENT_BLOCKS.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Level level = mc.level;
        Camera camera = event.getCamera();
        if (level == null || camera == null) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        double camX = camera.getPosition().x;
        double camY = camera.getPosition().y;
        double camZ = camera.getPosition().z;
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);

        BlockPos playerPos = camera.getBlockPosition();
        int renderRadius = 12;

        // Scan nearby loaded blocks for AbstractLiquidBlock instances to render extra models/animations
        for (BlockPos pos : BlockPos.betweenClosed(
            playerPos.offset(-renderRadius, -6, -renderRadius),
            playerPos.offset(renderRadius, 6, renderRadius)
        )) {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof AbstractLiquidBlock liquidBlock) {
                INSTANCE.render(poseStack, bufferSource, camX, camY, camZ, partialTicks, pos, liquidBlock);
            }
        }
    }

    /**
     * Public render function to render custom models, runic matrices, or animated GeckoLib overlays
     * directly inside the LiquidBlock.
     */
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, double camX, double camY, double camZ, float partialTicks, BlockPos pos, AbstractLiquidBlock block) {

        /*System.out.println("LiquidRendererPlus: render");*/

        poseStack.pushPose();
        poseStack.translate(pos.getX() - camX + 0.5, pos.getY() - camY + 0.5, pos.getZ() - camZ + 0.5);

        // Animated swirling vortex inside the fluid volume
        long time = System.currentTimeMillis();
        float angle = (time % 3600L) / 10.0F;
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(angle));

        VertexConsumer builder = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        // Render sub-surface holographic orbital ring inside fluid
        float radius = 0.35F;
        int segments = 16;
        for (int i = 0; i < segments; i++) {
            double a1 = (2 * Math.PI / segments) * i;
            double a2 = (2 * Math.PI / segments) * (i + 1);

            float x1 = (float) (Math.cos(a1) * radius);
            float z1 = (float) (Math.sin(a1) * radius);
            float x2 = (float) (Math.cos(a2) * radius);
            float z2 = (float) (Math.sin(a2) * radius);

            builder.addVertex(matrix, x1, 0.0F, z1).setColor(0.0F, 0.9F, 1.0F, 0.75F).setNormal(0, 1, 0);
            builder.addVertex(matrix, x2, 0.0F, z2).setColor(0.0F, 0.9F, 1.0F, 0.75F).setNormal(0, 1, 0);
        }

        poseStack.popPose();
    }

    public boolean isInitialized() {
        return initialized;
    }
}
