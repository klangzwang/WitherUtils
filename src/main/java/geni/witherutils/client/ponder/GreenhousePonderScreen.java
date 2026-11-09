package geni.witherutils.client.ponder;

import com.mojang.blaze3d.vertex.PoseStack;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.level.block.GreenhouseBlock;
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
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class GreenhousePonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 520;

    private static final int STEP_1_CANOPY_INTRO = 0;
    private static final int STEP_2_SKY_ACCESS = 125;
    private static final int STEP_3_CONNECTED_ARRAY = 250;
    private static final int STEP_4_GROWTH_BURSTS = 385;

    private final Map<BlockPos, BlockState> farmBaseTiles = new HashMap<>();

    public GreenhousePonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.greenhouse.title"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 25.0f;
        this.cameraZoom = 1.05f;

        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (Math.abs(x) == 2 || Math.abs(z) == 2) {
                    farmBaseTiles.put(new BlockPos(x, 0, z), Blocks.POLISHED_ANDESITE.defaultBlockState());
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
        return new int[]{STEP_1_CANOPY_INTRO, STEP_2_SKY_ACCESS, STEP_3_CONNECTED_ARRAY, STEP_4_GROWTH_BURSTS};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.greenhouse.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF22C55E;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : farmBaseTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(state, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
            poseStack.popPose();
        }

        boolean isFullArray = sceneTicks >= STEP_3_CONNECTED_ARRAY;
        renderAgriculturalLayers(poseStack, bufferSource, blockRenderer, smoothTick, isFullArray);
    }

    private void renderAgriculturalLayers(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick, boolean fullArray) {
        BlockState moistFarmland = Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, 7);

        int wheatAge = 0;
        if (sceneTicks >= STEP_4_GROWTH_BURSTS) {
            float stepProgress = smoothTick - STEP_4_GROWTH_BURSTS;
            float growthT = Mth.clamp(stepProgress / 80.0f, 0.0f, 1.0f);
            wheatAge = Math.min(7, (int) (growthT * 8.0f));
        } else if (sceneTicks >= STEP_3_CONNECTED_ARRAY) {
            wheatAge = 1;
        }

        BlockState wheatState = Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, wheatAge);

        int minX = fullArray ? -1 : 0;
        int maxX = fullArray ? 1 : 0;
        int minZ = fullArray ? -1 : 0;
        int maxZ = fullArray ? 1 : 0;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                float spawnDelay = (x == 0 && z == 0) ? 0.0f : (STEP_3_CONNECTED_ARRAY + (Math.abs(x) * 4.0f + Math.abs(z) * 4.0f));
                if (sceneTicks < spawnDelay) {
                    continue;
                }

                float age = Math.max(0.0f, smoothTick - spawnDelay);
                float dropProgress = spawnDelay == 0.0f ? 1.0f : Mth.clamp(age / 12.0f, 0.0f, 1.0f);
                float yOffset = (1.0f - easeOutBack(dropProgress)) * 2.0f;

                poseStack.pushPose();
                poseStack.translate(x, 0.0f, z);
                blockRenderer.renderSingleBlock(moistFarmland, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();

                poseStack.pushPose();
                poseStack.translate(x, 1.0f, z);
                blockRenderer.renderSingleBlock(wheatState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();

                BlockState panelState = computeConnectedPanelState(x, z, fullArray);
                poseStack.pushPose();
                poseStack.translate(x, 2.0f + yOffset, z);
                blockRenderer.renderSingleBlock(panelState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.translucent());
                poseStack.popPose();

                if (sceneTicks >= STEP_4_GROWTH_BURSTS && wheatAge < 7) {
                    if ((sceneTicks + x * 7 + z * 11) % 18 < 6) {
                        poseStack.pushPose();
                        float py = 1.2f + (float) Math.sin(smoothTick * 0.2f + x) * 0.2f;
                        poseStack.translate(x + 0.5f, py, z + 0.5f);
                        poseStack.scale(0.25F, 0.25F, 0.25F);
                        ItemStack sparkle = new ItemStack(Items.BONE_MEAL);
                        Minecraft.getInstance().getItemRenderer().renderStatic(sparkle, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
                        poseStack.popPose();
                    }
                }
            }
        }
    }

    private BlockState computeConnectedPanelState(int x, int z, boolean fullArray) {
        BlockState base = WUTBlocks.GREENHOUSE.get().defaultBlockState();
        if (!fullArray) {
            return base;
        }

        boolean north = z > -1;
        boolean south = z < 1;
        boolean west = x > -1;
        boolean east = x < 1;

        return base
                .setValue(GreenhouseBlock.NORTH, north)
                .setValue(GreenhouseBlock.SOUTH, south)
                .setValue(GreenhouseBlock.WEST, west)
                .setValue(GreenhouseBlock.EAST, east)
                .setValue(GreenhouseBlock.CORNER_NORTH_WEST, false)
                .setValue(GreenhouseBlock.CORNER_NORTH_EAST, false)
                .setValue(GreenhouseBlock.CORNER_SOUTH_WEST, false)
                .setValue(GreenhouseBlock.CORNER_SOUTH_EAST, false)
                .setValue(GreenhouseBlock.CORNER_ROD, true);
    }

    @Override
    protected void renderSceneOverlays(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks >= STEP_2_SKY_ACCESS && sceneTicks < STEP_3_CONNECTED_ARRAY) {
            renderSunRayOverlay(graphics, sceneMatrix, smoothTick);
        }
    }

    private void renderSunRayOverlay(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        float stepProgress = smoothTick - STEP_2_SKY_ACCESS;
        float progress = Mth.clamp(stepProgress / (float) (STEP_3_CONNECTED_ARRAY - STEP_2_SKY_ACCESS), 0.0f, 1.0f);
        float angle = progress * (float) Math.PI;

        float sunX = 0.5f - Mth.cos(angle) * 1.8f;
        float sunY = 2.4f + Mth.sin(angle) * 1.5f;
        float sunZ = 0.5f;

        Vector4f sunScreen = projectToScreen(sceneMatrix, sunX, sunY, sunZ);
        Vector4f panelScreen = projectToScreen(sceneMatrix, 0.5f, 2.1f, 0.5f);

        int sx = Math.round(sunScreen.x);
        int sy = Math.round(sunScreen.y);
        int px = Math.round(panelScreen.x);
        int py = Math.round(panelScreen.y);

        drawLine(graphics, sx, sy, px, py, 0x88FACC15);

        graphics.fill(sx - 7, sy - 7, sx + 7, sy + 7, 0x66F59E0B);
        graphics.fill(sx - 4, sy - 4, sx + 4, sy + 4, 0xFFFDE047);

        graphics.drawString(this.font, "Direct Sunlight (100%)", sx + 10, sy - 4, 0xFDE047, true);
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        if (sceneTicks < STEP_2_SKY_ACCESS) {
            renderCraftingRecipeHUD(graphics);
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 2.2f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.greenhouse.step1.title",
                    I18n.get("ponder.witherutils.greenhouse.step1.text"),
                    "§a»BlockHead Industry: Harness sunlight, accelerate photosynthetics, eliminate hunger!«",
                    0xFF22C55E, smoothTick, STEP_1_CANOPY_INTRO);
        } else if (sceneTicks < STEP_3_CONNECTED_ARRAY) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 2.1f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.greenhouse.step2.title",
                    I18n.get("ponder.witherutils.greenhouse.step2.text"),
                    "§6§l»Unobstructed sunlight channels solar heat down to soil, tripling natural growth ticks!«",
                    0xFFFACC15, smoothTick, STEP_2_SKY_ACCESS);
        } else if (sceneTicks < STEP_4_GROWTH_BURSTS) {
            Vector4f anchor = projectToScreen(sceneMatrix, 1.5f, 2.1f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.greenhouse.step3.title",
                    I18n.get("ponder.witherutils.greenhouse.step3.text"),
                    "§b§l»Place them in a grid — frames merge seamlessly for maximum aesthetic greenhouse farming!«",
                    0xFF00ABE2, smoothTick, STEP_3_CONNECTED_ARRAY);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.2f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.greenhouse.step4.title",
                    I18n.get("ponder.witherutils.greenhouse.step4.text"),
                    "§d§l»No more dirty hands grinding bones: sit back and watch your crops burst with vitality!«",
                    0xFFEC4899, smoothTick, STEP_4_GROWTH_BURSTS);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 108;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF22C55E);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Recipe Grid").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF22C55E, false);

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

        for (int col = 0; col < 3; col++) {
            graphics.renderItem(new ItemStack(Items.GLASS_PANE), slotStartX + (col * 20), slotStartY);
        }
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_ROD.get()), slotStartX, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.BONE_BLOCK), slotStartX + 20, slotStartY + 20);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_ROD.get()), slotStartX + 40, slotStartY + 20);
        for (int col = 0; col < 3; col++) {
            graphics.renderItem(new ItemStack(Items.GLASS_PANE), slotStartX + (col * 20), slotStartY + 40);
        }

        graphics.drawString(this.font, "➔", hudX + 70, hudY + 42, 0x94A3B8, false);
        graphics.renderItem(new ItemStack(WUTBlocks.GREENHOUSE.get()), hudX + 84, hudY + 38);
    }
}
