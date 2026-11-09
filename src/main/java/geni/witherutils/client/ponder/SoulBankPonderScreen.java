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
public class SoulBankPonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 480;

    private static final int STEP_1_CASE_INTRO = 0;
    private static final int STEP_2_THREE_TIERS = 120;
    private static final int STEP_3_ENERGY_STORAGE = 240;
    private static final int STEP_4_MACHINE_SLOT = 360;

    private final Map<BlockPos, BlockState> platformTiles = new HashMap<>();

    public SoulBankPonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.literal("SoulBank Power & Battery Manual"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 1.05f;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                platformTiles.put(new BlockPos(x, 0, z), Blocks.POLISHED_BLACKSTONE.defaultBlockState());
            }
        }
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_CASE_INTRO, STEP_2_THREE_TIERS, STEP_3_ENERGY_STORAGE, STEP_4_MACHINE_SLOT};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "SoulBank Battery Unit" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return "╘ BlockHead Industry — Quantum Soul & Energy Storage Manual";
    }

    @Override
    protected int getAccentColor() {
        return 0xFFA855F7;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : platformTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        if (sceneTicks < STEP_2_THREE_TIERS) {
            renderFloatingSoulCase(poseStack, bufferSource, smoothTick);
        } else if (sceneTicks < STEP_3_ENERGY_STORAGE) {
            renderTierShowcase(poseStack, bufferSource, smoothTick);
        } else if (sceneTicks < STEP_4_MACHINE_SLOT) {
            renderEnergySwirlScene(poseStack, bufferSource, smoothTick);
        } else {
            renderMachineSlotScene(poseStack, bufferSource, blockRenderer, smoothTick);
        }
    }

    private void renderFloatingSoulCase(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float smoothTick) {
        poseStack.pushPose();
        float hover = 1.35f + (float) Math.sin(smoothTick * 0.08f) * 0.12f;
        poseStack.translate(0.0f, hover, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 2.5f));
        poseStack.scale(0.85f, 0.85f, 0.85f);
        ItemStack caseStack = new ItemStack(WUTItems.SOULBANK_CASE.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(caseStack, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
        poseStack.popPose();
    }

    private void renderTierShowcase(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float smoothTick) {
        ItemStack[] tiers = {
                new ItemStack(WUTItems.SOULBANK_BASIC.get()),
                new ItemStack(WUTItems.SOULBANK_ADVANCED.get()),
                new ItemStack(WUTItems.SOULBANK_ULTRA.get())
        };
        float[] offsetsX = {-0.9f, 0.0f, 0.9f};

        for (int i = 0; i < 3; i++) {
            poseStack.pushPose();
            float hover = 1.3f + (float) Math.sin(smoothTick * 0.12f + (i * 1.5f)) * 0.1f;
            poseStack.translate(offsetsX[i], hover, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 3.0f + (i * 45.0f)));
            poseStack.scale(0.8f, 0.8f, 0.8f);
            Minecraft.getInstance().getItemRenderer().renderStatic(tiers[i], ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderEnergySwirlScene(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float smoothTick) {
        // Ultra SoulBank in center
        poseStack.pushPose();
        float hover = 1.35f + (float) Math.sin(smoothTick * 0.08f) * 0.1f;
        poseStack.translate(0.0f, hover, 0.0f);
        poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 2.0f));
        poseStack.scale(0.95f, 0.95f, 0.95f);
        ItemStack ultra = new ItemStack(WUTItems.SOULBANK_ULTRA.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(ultra, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
        poseStack.popPose();

        // Orbiting Soul Energy Particles
        for (int i = 0; i < 4; i++) {
            float angle = (smoothTick * 0.12f) + (i * (float) Math.PI * 0.5f);
            float rad = 0.75f;
            float px = (float) Math.cos(angle) * rad;
            float pz = (float) Math.sin(angle) * rad;
            float py = 1.35f + (float) Math.sin(smoothTick * 0.2f + i) * 0.2f;

            poseStack.pushPose();
            poseStack.translate(px, py, pz);
            poseStack.scale(0.22f, 0.22f, 0.22f);
            ItemStack tear = new ItemStack(Items.GHAST_TEAR);
            Minecraft.getInstance().getItemRenderer().renderStatic(tear, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderMachineSlotScene(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        // Machine representation
        BlockState machine = WUTBlocks.RACKITEM_CONTROLLER.get().defaultBlockState();
        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(machine, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();

        // Soulbank hovering above slot and clicking in
        float cycle = (smoothTick % 50.0f) / 50.0f;
        float itemY = cycle < 0.5f ? 2.3f : Mth.lerp((cycle - 0.5f) / 0.5f, 2.3f, 1.6f);

        poseStack.pushPose();
        poseStack.translate(0.5f, itemY, 0.5f);
        poseStack.scale(0.55f, 0.55f, 0.55f);
        ItemStack bank = new ItemStack(WUTItems.SOULBANK_ADVANCED.get());
        Minecraft.getInstance().getItemRenderer().renderStatic(bank, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
        poseStack.popPose();
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingRecipeHUD(graphics);

        if (sceneTicks < STEP_2_THREE_TIERS) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.4f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.soulbank.step1.title",
                    I18n.get("ponder.witherutils.soulbank.step1.text"),
                    "§d»BlockHead Industry: Before you can harness the power of captured souls, you need an airtight casing!«",
                    0xFFA855F7, smoothTick, STEP_1_CASE_INTRO);
        } else if (sceneTicks < STEP_3_ENERGY_STORAGE) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.6f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.soulbank.step2.title",
                    I18n.get("ponder.witherutils.soulbank.step2.text"),
                    "§5§l»Basic -> Advanced -> Ultra: Supercharge your machinery's internal energy buffers!«",
                    0xFFC084FC, smoothTick, STEP_2_THREE_TIERS);
        } else if (sceneTicks < STEP_4_MACHINE_SLOT) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.5f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.soulbank.step3.title",
                    I18n.get("ponder.witherutils.soulbank.step3.text"),
                    "§a§l»Stable, zero radiation, 100% clean spiritual energy storage.«",
                    0xFF10B981, smoothTick, STEP_3_ENERGY_STORAGE);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.8f, 0.5f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.soulbank.step4.title",
                    I18n.get("ponder.witherutils.soulbank.step4.text"),
                    "§6§l»Pop it right in! The heart of every automated factory complex.«",
                    0xFFF59E0B, smoothTick, STEP_4_MACHINE_SLOT);
        }
    }

    private void renderCraftingRecipeHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 114;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFFA855F7);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("SoulBank Case").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFFC084FC, false);

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

        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_PLATE.get()), slotStartX, slotStartY);
        graphics.renderItem(new ItemStack(Items.IRON_INGOT), slotStartX + 20, slotStartY);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_PLATE.get()), slotStartX + 40, slotStartY);
        graphics.renderItem(new ItemStack(Items.IRON_INGOT), slotStartX, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.REDSTONE), slotStartX + 20, slotStartY + 20);
        graphics.renderItem(new ItemStack(Items.IRON_INGOT), slotStartX + 40, slotStartY + 20);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_PLATE.get()), slotStartX, slotStartY + 40);
        graphics.renderItem(new ItemStack(Items.IRON_INGOT), slotStartX + 20, slotStartY + 40);
        graphics.renderItem(new ItemStack(WUTItems.WITHERSTEEL_PLATE.get()), slotStartX + 40, slotStartY + 40);

        int outX = hudX + 74;
        int outY = hudY + 42;
        graphics.drawString(this.font, "➔", outX - 3, outY + 4, 0x94A3B8, false);
        graphics.fill(outX + 11, outY - 2, outX + 31, outY + 18, 0xFFA855F7);
        graphics.fill(outX + 12, outY - 1, outX + 30, outY + 17, 0xFF0F172A);
        ItemStack caseItem = new ItemStack(WUTItems.SOULBANK_CASE.get());
        graphics.renderFakeItem(caseItem, outX + 13, outY);
    }
}
