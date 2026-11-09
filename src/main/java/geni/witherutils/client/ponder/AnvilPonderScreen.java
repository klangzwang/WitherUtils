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
public class AnvilPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 580;

    private static final int STEP_1_HAMMER_CRAFT = 0;
    private static final int STEP_2_CURSED_SMASH = 105;
    private static final int STEP_3_LOAD_MATERIAL = 225;
    private static final int STEP_4_FORGING_STRIKES = 345;
    private static final int STEP_5_OUTPUT_RECIPES = 465;

    private final Map<BlockPos, BlockState> basePlatformBlocks = new HashMap<>();

    public AnvilPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.anvil.title"), targetStack, parentScreen);
        this.cameraYaw = -32.0f;
        this.cameraPitch = 25.0f;
        this.cameraZoom = 1.05f;
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_HAMMER_CRAFT, STEP_2_CURSED_SMASH, STEP_3_LOAD_MATERIAL, STEP_4_FORGING_STRIKES, STEP_5_OUTPUT_RECIPES};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.anvil.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFFEAB308;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : basePlatformBlocks.entrySet()) {
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

        if (sceneTicks < STEP_2_CURSED_SMASH) {
            renderStage1HammerHero(poseStack, bufferSource, smoothTick);
        } else if (sceneTicks < STEP_3_LOAD_MATERIAL) {
            renderStage2CursedSmash(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_4_FORGING_STRIKES) {
            renderStage3LoadingMaterial(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_5_OUTPUT_RECIPES) {
            renderStage4HammerForging(poseStack, bufferSource, blockRenderer, smoothTick);
        } else {
            renderStage5OutputShowcase(poseStack, bufferSource, blockRenderer, smoothTick);
        }
    }

    private void renderStage1HammerHero(PoseStack poseStack, MultiBufferSource bufferSource, float smoothTick) {
        poseStack.pushPose();
        float hover = Mth.sin(smoothTick * 0.08f) * 0.12f;
        poseStack.translate(0.0D, 1.3D + hover, 0.0D);
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 2.5f));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-25.0f));
        poseStack.scale(1.5F, 1.5F, 1.5F);

        ItemStack hammerStack = new ItemStack(WUTItems.HAMMER.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                hammerStack,
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

    private void renderStage2CursedSmash(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float stepProgress = smoothTick - STEP_2_CURSED_SMASH;
        boolean postHit = stepProgress >= 30.0f;

        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);

        BlockState anvilState = postHit
                ? WUTBlocks.ANVIL.get().defaultBlockState()
                : Blocks.ANVIL.defaultBlockState();

        blockRenderer.renderSingleBlock(
                anvilState,
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();

        if (postHit) {
            BlockPos[] firePositions = {
                    new BlockPos(-1, 1, 0), new BlockPos(1, 1, 0),
                    new BlockPos(0, 1, -1), new BlockPos(0, 1, 1)
            };
            for (BlockPos fpos : firePositions) {
                poseStack.pushPose();
                poseStack.translate(fpos.getX(), fpos.getY(), fpos.getZ());
                blockRenderer.renderSingleBlock(
                        WUTBlocks.SOULFIRE.get().defaultBlockState(),
                        poseStack,
                        bufferSource,
                        LightTexture.FULL_BRIGHT,
                        OverlayTexture.NO_OVERLAY,
                        ModelData.EMPTY,
                        RenderType.cutout()
                );
                poseStack.popPose();
            }
        }

        poseStack.pushPose();
        float hammerY;
        float hammerAngle;

        if (stepProgress < 24.0f) {
            float t = stepProgress / 24.0f;
            hammerY = Mth.lerp(t, 1.5f, 2.5f);
            hammerAngle = Mth.lerp(t, 0.0f, -60.0f);
        } else if (stepProgress < 30.0f) {
            float t = (stepProgress - 24.0f) / 6.0f;
            hammerY = Mth.lerp(t, 2.5f, 1.6f);
            hammerAngle = Mth.lerp(t, -60.0f, 25.0f);
        } else {
            float t = Math.min(1.0f, (stepProgress - 30.0f) / 20.0f);
            hammerY = Mth.lerp(t, 1.6f, 2.1f);
            hammerAngle = Mth.lerp(t, 25.0f, -15.0f);
        }

        poseStack.translate(0.3D, hammerY, 0.0D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(hammerAngle));
        poseStack.scale(1.2F, 1.2F, 1.2F);

        ItemStack hammer = new ItemStack(WUTItems.HAMMER.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                hammer,
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

    private void renderStage3LoadingMaterial(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        blockRenderer.renderSingleBlock(
                WUTBlocks.ANVIL.get().defaultBlockState(),
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();

        float stepProgress = smoothTick - STEP_3_LOAD_MATERIAL;
        float placeProgress = Mth.clamp(stepProgress / 30.0f, 0.0f, 1.0f);
        float itemY = Mth.lerp(placeProgress, 2.2f, 1.62f);
        float itemAngle = Mth.lerp(placeProgress, 45.0f, 0.0f);

        poseStack.pushPose();
        poseStack.translate(0.5D, itemY, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(itemAngle));
        poseStack.scale(0.85F, 0.85F, 0.85F);

        ItemStack ingot = new ItemStack(Items.IRON_INGOT);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ingot,
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

    private void renderStage4HammerForging(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        blockRenderer.renderSingleBlock(
                WUTBlocks.ANVIL.get().defaultBlockState(),
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();

        poseStack.pushPose();
        poseStack.translate(0.5D, 1.62D, 0.5D);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.85F, 0.85F, 0.85F);

        ItemStack ingot = new ItemStack(Items.IRON_INGOT);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                ingot,
                ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                this.minecraft != null ? this.minecraft.level : null,
                0
        );
        poseStack.popPose();

        float stepProgress = smoothTick - STEP_4_FORGING_STRIKES;
        float cycle = stepProgress % 30.0f;

        float hammerAngle;
        float hammerY;
        if (cycle < 20.0f) {
            float t = cycle / 20.0f;
            hammerY = Mth.lerp(t, 1.8f, 2.4f);
            hammerAngle = Mth.lerp(t, 0.0f, -45.0f);
        } else {
            float t = (cycle - 20.0f) / 10.0f;
            hammerY = Mth.lerp(t, 2.4f, 1.75f);
            hammerAngle = Mth.lerp(t, -45.0f, 15.0f);
        }

        poseStack.pushPose();
        poseStack.translate(0.5D, hammerY, 0.5D);
        poseStack.mulPose(Axis.ZP.rotationDegrees(hammerAngle));
        poseStack.scale(1.1F, 1.1F, 1.1F);

        ItemStack hammer = new ItemStack(WUTItems.HAMMER.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(
                hammer,
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

    private void renderStage5OutputShowcase(PoseStack poseStack, MultiBufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        poseStack.pushPose();
        poseStack.translate(0.0D, 1.0D, 0.0D);
        blockRenderer.renderSingleBlock(
                WUTBlocks.ANVIL.get().defaultBlockState(),
                poseStack,
                bufferSource,
                LightTexture.FULL_BRIGHT,
                OverlayTexture.NO_OVERLAY,
                ModelData.EMPTY,
                RenderType.cutout()
        );
        poseStack.popPose();

        float stepProgress = smoothTick - STEP_5_OUTPUT_RECIPES;
        int cycleIndex = ((int) (stepProgress / 40.0f)) % 3;

        ItemStack outputStack;
        if (cycleIndex == 0) {
            outputStack = new ItemStack(WUTItems.WITHERSTEEL_NUGGET.get());
        } else if (cycleIndex == 1) {
            outputStack = new ItemStack(WUTItems.WITHERSTEEL_PLATE.get());
        } else {
            outputStack = new ItemStack(WUTItems.WITHERSTEEL_GEAR.get());
        }

        poseStack.pushPose();
        float hover = Mth.sin(smoothTick * 0.1f) * 0.1f;
        poseStack.translate(0.5D, 1.85D + hover, 0.5D);
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 3.0f));
        poseStack.scale(1.2F, 1.2F, 1.2F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                outputStack,
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
        if (sceneTicks < STEP_2_CURSED_SMASH) {
            renderCraftingRecipeHUD(graphics);
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.4f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.anvil.step1.title",
                    I18n.get("ponder.witherutils.anvil.step1.text"),
                    "§6»BlockHead Industry: Before you can break physics, you must build the hammer that breaks physics!«",
                    0xFFFACC15, smoothTick, STEP_1_HAMMER_CRAFT);
        } else if (sceneTicks < STEP_3_LOAD_MATERIAL) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.3f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.anvil.step2.title",
                    I18n.get("ponder.witherutils.anvil.step2.text"),
                    "§c§l»KRAKATOA! Lightning strikes, ears ring, and suddenly your peaceful anvil is glowing red-hot!«",
                    0xFFEF4444, smoothTick, STEP_2_CURSED_SMASH);
        } else if (sceneTicks < STEP_4_FORGING_STRIKES) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.6f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.anvil.step3.title",
                    I18n.get("ponder.witherutils.anvil.step3.text"),
                    "§b§l»Right-click any compatible raw material directly onto the anvil head. Clean, hands-on manufacturing!«",
                    0xFF00ABE2, smoothTick, STEP_3_LOAD_MATERIAL);
        } else if (sceneTicks < STEP_5_OUTPUT_RECIPES) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.7f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.anvil.step4.title",
                    I18n.get("ponder.witherutils.anvil.step4.text"),
                    "§e§l»CLANG! CLANG! CLANG! Rhythmic blows generate heat and compress molecules into industrial alloys!«",
                    0xFFF59E0B, smoothTick, STEP_4_FORGING_STRIKES);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.9f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.anvil.step5.title",
                    I18n.get("ponder.witherutils.anvil.step5.text"),
                    "§a§l»From basic iron to Withersteel Plates, Gears, and Rods — the entire industrial empire starts right here!«",
                    0xFF10B981, smoothTick, STEP_5_OUTPUT_RECIPES);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 108;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFFEAB308);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Recipe Grid").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFFEAB308, false);

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

        graphics.renderItem(new ItemStack(Items.IRON_INGOT), slotStartX + 40, slotStartY);
        graphics.renderItem(new ItemStack(Items.STICK), slotStartX, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.STICK), slotStartX + 20, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.IRON_BLOCK), slotStartX + 40, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.IRON_INGOT), slotStartX + 40, slotStartY + 40);

        graphics.drawString(this.font, "➔", hudX + 70, hudY + 42, 0x94A3B8, false);
        graphics.renderItem(new ItemStack(WUTItems.HAMMER.get()), hudX + 84, hudY + 38);
    }
}
