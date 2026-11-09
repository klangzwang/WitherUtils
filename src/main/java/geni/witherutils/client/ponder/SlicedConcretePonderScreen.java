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
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class SlicedConcretePonderScreen extends BasePonderScreen {

    private static final int TOTAL_TICKS = 480;

    private static final int STEP_1_EXTRACTION = 0;
    private static final int STEP_2_THREE_SHADES = 120;
    private static final int STEP_3_MULTIFACE = 240;
    private static final int STEP_4_CLEANROOM = 360;

    private final Map<BlockPos, BlockState> pedestalTiles = new HashMap<>();

    public SlicedConcretePonderScreen(ItemStack targetStack, Screen parentScreen) {
        super(Component.translatable("ponder.witherutils.slicedconcrete.title"), targetStack, parentScreen);
        this.cameraYaw = -38.0f;
        this.cameraPitch = 22.0f;
        this.cameraZoom = 1.05f;

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                pedestalTiles.put(new BlockPos(x, 0, z), Blocks.POLISHED_ANDESITE.defaultBlockState());
            }
        }
    }

    @Override
    protected int getTotalTicks() {
        return TOTAL_TICKS;
    }

    @Override
    protected int[] getKeyframes() {
        return new int[]{STEP_1_EXTRACTION, STEP_2_THREE_SHADES, STEP_3_MULTIFACE, STEP_4_CLEANROOM};
    }

    @Override
    protected String getHeaderTitle() {
        return targetStack.isEmpty() ? "Sliced Concrete" : targetStack.getHoverName().getString();
    }

    @Override
    protected String getHeaderSubtitle() {
        return I18n.get("ponder.witherutils.slicedconcrete.header_subtitle");
    }

    @Override
    protected int getAccentColor() {
        return 0xFF94A3B8;
    }

    @Override
    protected void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        for (Map.Entry<BlockPos, BlockState> entry : pedestalTiles.entrySet()) {
            BlockPos pos = entry.getKey();
            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderSingleBlock(entry.getValue(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();
        }

        if (sceneTicks < STEP_2_THREE_SHADES) {
            renderSmashExtractionScene(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_3_MULTIFACE) {
            renderThreeShadesScene(poseStack, bufferSource, blockRenderer, smoothTick);
        } else if (sceneTicks < STEP_4_CLEANROOM) {
            renderMultifaceVeneerScene(poseStack, bufferSource, blockRenderer);
        } else {
            renderCleanroomCorridorScene(poseStack, bufferSource, blockRenderer);
        }
    }

    private void renderSmashExtractionScene(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        float cycle = (smoothTick % 60.0f);
        boolean isShattered = cycle >= 32.0f;

        if (!isShattered) {
            poseStack.pushPose();
            poseStack.translate(0.0f, 1.0f, 0.0f);
            blockRenderer.renderSingleBlock(Blocks.BLACK_CONCRETE.defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();

            poseStack.pushPose();
            float hammerProgress = cycle / 32.0f;
            float hammerRot = (float) Math.sin(hammerProgress * Math.PI * 0.5f) * 65.0f;
            poseStack.translate(0.5f, 2.2f - (hammerProgress * 0.7f), 0.0f);
            poseStack.mulPose(Axis.ZP.rotationDegrees(-hammerRot));
            poseStack.scale(0.85f, 0.85f, 0.85f);
            ItemStack hammer = new ItemStack(WUTItems.HAMMER.get());
            Minecraft.getInstance().getItemRenderer().renderStatic(hammer, ItemDisplayContext.NONE, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        } else {
            float burstProgress = (cycle - 32.0f) / 28.0f;

            if (cycle < 38.0f) {
                poseStack.pushPose();
                poseStack.translate(0.0f, 1.3f, 0.0f);
                poseStack.scale(0.6f, 0.6f, 0.6f);
                ItemStack smoke = new ItemStack(Items.FIREWORK_STAR);
                Minecraft.getInstance().getItemRenderer().renderStatic(smoke, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
                poseStack.popPose();
            }

            for (int i = 0; i < 8; i++) {
                float angle = (i * 45.0f) * ((float) Math.PI / 180.0f);
                float radius = 0.2f + (burstProgress * 0.75f);
                float ix = Mth.cos(angle) * radius;
                float iz = Mth.sin(angle) * radius;
                float iy = 1.0f + (float) Math.sin(burstProgress * Math.PI) * 0.6f;

                poseStack.pushPose();
                poseStack.translate(ix, iy, iz);
                poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 6.0f + (i * 45.0f)));
                poseStack.scale(0.35f, 0.35f, 0.35f);
                ItemStack slice = new ItemStack(WUTBlocks.SLICEDCONCRETEBLACK.get());
                Minecraft.getInstance().getItemRenderer().renderStatic(slice, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
                poseStack.popPose();
            }
        }
    }

    private void renderThreeShadesScene(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick) {
        int[] posX = {-1, 0, 1};
        Block[] concreteBlocks = {Blocks.BLACK_CONCRETE, Blocks.GRAY_CONCRETE, Blocks.WHITE_CONCRETE};
        Block[] slicedBlocks = {WUTBlocks.SLICEDCONCRETEBLACK.get(), WUTBlocks.SLICEDCONCRETEGRAY.get(), WUTBlocks.SLICEDCONCRETEWHITE.get()};

        for (int i = 0; i < 3; i++) {
            int x = posX[i];
            poseStack.pushPose();
            poseStack.translate(x, 1.0f, 0.0f);
            blockRenderer.renderSingleBlock(concreteBlocks[i].defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
            poseStack.popPose();

            poseStack.pushPose();
            float bobY = 2.15f + (float) Math.sin(smoothTick * 0.15f + (i * 1.2f)) * 0.1f;
            poseStack.translate(x, bobY, 0.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(smoothTick * 3.0f + (i * 60.0f)));
            poseStack.scale(0.42f, 0.42f, 0.42f);
            ItemStack sliceItem = new ItemStack(slicedBlocks[i]);
            Minecraft.getInstance().getItemRenderer().renderStatic(sliceItem, ItemDisplayContext.GROUND, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, poseStack, bufferSource, this.minecraft != null ? this.minecraft.level : null, 0);
            poseStack.popPose();
        }
    }

    private void renderMultifaceVeneerScene(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer) {
        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(Blocks.POLISHED_BLACKSTONE.defaultBlockState(), poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.solid());
        poseStack.popPose();

        BlockState sliceState = WUTBlocks.SLICEDCONCRETEWHITE.get().defaultBlockState()
                .setValue(MultifaceBlock.getFaceProperty(Direction.UP), true)
                .setValue(MultifaceBlock.getFaceProperty(Direction.NORTH), true)
                .setValue(MultifaceBlock.getFaceProperty(Direction.EAST), true);

        poseStack.pushPose();
        poseStack.translate(0.0f, 1.0f, 0.0f);
        blockRenderer.renderSingleBlock(sliceState, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
        poseStack.popPose();
    }

    private void renderCleanroomCorridorScene(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                BlockState tile = ((x + z) % 2 == 0) ? WUTBlocks.SLICEDCONCRETEGRAY.get().defaultBlockState() : WUTBlocks.SLICEDCONCRETEBLACK.get().defaultBlockState();
                BlockState floorTile = tile.setValue(MultifaceBlock.getFaceProperty(Direction.UP), true);
                poseStack.pushPose();
                poseStack.translate(x, 0.0f, z);
                blockRenderer.renderSingleBlock(floorTile, poseStack, bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
                poseStack.popPose();
            }
        }
    }

    @Override
    protected void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
        renderCraftingHUD(graphics);

        if (sceneTicks < STEP_2_THREE_SHADES) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.4f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.slicedconcrete.step1.title",
                    I18n.get("ponder.witherutils.slicedconcrete.step1.text"),
                    "§8»BlockHead Industry: Sledgehammer physics: 1 Solid Concrete Block yields 8 ultra-thin veneers!«",
                    0xFF64748B, smoothTick, STEP_1_EXTRACTION);
        } else if (sceneTicks < STEP_3_MULTIFACE) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.8f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.slicedconcrete.step2.title",
                    I18n.get("ponder.witherutils.slicedconcrete.step2.text"),
                    "§7§l»Black, Gray, and White: The holy trinity of industrial minimalism.«",
                    0xFF94A3B8, smoothTick, STEP_2_THREE_SHADES);
        } else if (sceneTicks < STEP_4_CLEANROOM) {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.5f, 1.6f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.slicedconcrete.step3.title",
                    I18n.get("ponder.witherutils.slicedconcrete.step3.text"),
                    "§b§l»Stick it to floors, walls, and ceilings without stealing precious walking room!«",
                    0xFF38BDF8, smoothTick, STEP_3_MULTIFACE);
        } else {
            Vector4f anchor = projectToScreen(sceneMatrix, 0.0f, 1.8f, 0.0f);
            renderCalloutBox(graphics, anchor, "ponder.witherutils.slicedconcrete.step4.title",
                    I18n.get("ponder.witherutils.slicedconcrete.step4.text"),
                    "§a§l»Sterile lab vibes, zero dust, 100% waterloggable. Welcome to the future of bunker architecture.«",
                    0xFF10B981, smoothTick, STEP_4_CLEANROOM);
        }
    }

    private void renderCraftingHUD(GuiGraphics graphics) {
        int hudX = 20;
        int hudY = 48;
        int hudW = 114;
        int hudH = 92;

        graphics.fill(hudX - 2, hudY - 2, hudX + hudW + 2, hudY + hudH + 2, 0xEE020617);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + hudH, 0xF50F172A);
        graphics.fill(hudX, hudY, hudX + 3, hudY + hudH, 0xFF94A3B8);
        graphics.fill(hudX, hudY, hudX + hudW, hudY + 1, 0x4494A3B8);
        graphics.fill(hudX, hudY + hudH - 1, hudX + hudW, hudY + hudH, 0x4494A3B8);

        graphics.drawString(this.font, Component.literal("Smash Recipe").withStyle(ChatFormatting.BOLD), hudX + 8, hudY + 6, 0xFF94A3B8, false);

        int slotStartX = hudX + 8;
        int slotStartY = hudY + 22;

        graphics.fill(slotStartX, slotStartY, slotStartX + 18, slotStartY + 18, 0xAA1E293B);
        graphics.renderFakeItem(new ItemStack(WUTItems.HAMMER.get()), slotStartX + 1, slotStartY + 1);

        graphics.drawString(this.font, "+", slotStartX + 24, slotStartY + 5, 0x94A3B8, false);

        graphics.fill(slotStartX + 36, slotStartY, slotStartX + 54, slotStartY + 18, 0xAA1E293B);
        graphics.renderFakeItem(new ItemStack(Blocks.BLACK_CONCRETE), slotStartX + 37, slotStartY + 1);

        graphics.drawString(this.font, "➔", slotStartX + 58, slotStartY + 5, 0x94A3B8, false);

        graphics.fill(slotStartX + 72, slotStartY, slotStartX + 90, slotStartY + 18, 0xFF475569);
        ItemStack sliceItem = new ItemStack(WUTBlocks.SLICEDCONCRETEBLACK.get(), 8);
        graphics.renderFakeItem(sliceItem, slotStartX + 73, slotStartY + 1);
        graphics.renderItemDecorations(this.font, sliceItem, slotStartX + 73, slotStartY + 1);

        graphics.drawString(this.font, "Yields 8 Veneers", hudX + 8, hudY + 54, 0xCBD5E1, false);
    }
}
