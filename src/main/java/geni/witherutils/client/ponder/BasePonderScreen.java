package geni.witherutils.client.ponder;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public abstract class BasePonderScreen extends Screen {

    protected final ItemStack targetStack;
    protected final Screen parentScreen;

    protected int sceneTicks = 0;
    protected boolean paused = false;

    protected float cameraYaw = -38.0f;
    protected float cameraPitch = 24.0f;
    protected float targetCameraPitch = 24.0f;
    protected float cameraZoom = 1.0f;

    protected boolean isDragging = false;
    protected Button playPauseBtn;

    public BasePonderScreen(Component title, ItemStack targetStack, Screen parentScreen) {
        super(title);
        this.targetStack = targetStack;
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int bottomY = this.height - 32;

        // Previous Step Button
        this.addRenderableWidget(Button.builder(Component.literal("⏮"), btn -> jumpToPreviousKeyframe())
                .bounds(centerX - 110, bottomY, 36, 20)
                .build());

        // Play / Pause Button
        this.playPauseBtn = this.addRenderableWidget(Button.builder(Component.literal(paused ? "▶" : "⏸"), btn -> {
            paused = !paused;
            btn.setMessage(Component.literal(paused ? "▶" : "⏸"));
        }).bounds(centerX - 68, bottomY, 36, 20).build());

        // Next Step Button
        this.addRenderableWidget(Button.builder(Component.literal("⏭"), btn -> jumpToNextKeyframe())
                .bounds(centerX - 26, bottomY, 36, 20)
                .build());

        // Replay Button
        this.addRenderableWidget(Button.builder(Component.translatable("ponder.witherutils.replay"), btn -> {
            sceneTicks = 0;
            paused = false;
            resetCamera();
            if (playPauseBtn != null) {
                playPauseBtn.setMessage(Component.literal("⏸"));
            }
            rebuildSceneState();
        }).bounds(centerX + 16, bottomY, 56, 20).build());

        // Close Button
        this.addRenderableWidget(Button.builder(Component.translatable("ponder.witherutils.close"), btn -> onClose())
                .bounds(centerX + 78, bottomY, 48, 20)
                .build());

        rebuildSceneState();
    }

    protected void resetCamera() {
        cameraYaw = -38.0f;
        cameraPitch = 24.0f;
        targetCameraPitch = 24.0f;
        cameraZoom = 1.0f;
    }

    protected void jumpToPreviousKeyframe() {
        int[] keyframes = getKeyframes();
        for (int i = keyframes.length - 1; i >= 0; i--) {
            if (sceneTicks > keyframes[i] + 10) {
                sceneTicks = keyframes[i];
                rebuildSceneState();
                return;
            }
        }
        sceneTicks = 0;
        rebuildSceneState();
    }

    protected void jumpToNextKeyframe() {
        int[] keyframes = getKeyframes();
        for (int kf : keyframes) {
            if (kf > sceneTicks) {
                sceneTicks = kf;
                rebuildSceneState();
                return;
            }
        }
        if (keyframes.length > 0) {
            sceneTicks = keyframes[keyframes.length - 1];
        }
        rebuildSceneState();
    }

    @Override
    public void tick() {
        super.tick();
        if (!paused && sceneTicks < getTotalTicks()) {
            sceneTicks++;
        }

        onSceneTick();
        rebuildSceneState();
    }

    protected void onSceneTick() {
        if (!isDragging) {
            cameraPitch = Mth.lerp(0.08f, cameraPitch, targetCameraPitch);
        }
    }

    protected void rebuildSceneState() {
    }

    protected abstract int getTotalTicks();
    protected abstract int[] getKeyframes();

    protected abstract String getHeaderTitle();
    protected abstract String getHeaderSubtitle();
    protected int getAccentColor() {
        return 0xFF00ABE2;
    }

    // --- Blur deactivation (Guaranteed disabled for all screens) ---
    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Blur shader intentionally disabled to keep scene sharp and clean
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
        // Minecraft 1.21.1 post-processing blur disabled
    }

    // --- Screen-Space Layout & Positioning ---
    // Kept to the left half so that callout boxes on the right never overlap!
    protected float getSceneCenterX() {
        return this.width * 0.35f;
    }

    protected float getSceneCenterY() {
        return this.height * 0.56f;
    }

    protected float getSceneScale() {
        return 44.0f * cameraZoom;
    }

    protected float getSceneZOffset() {
        return 50.0f; // Farther back in depth space
    }

    protected void applySceneCenterOffset(PoseStack poseStack) {
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Dark sleek backdrop
        graphics.fillGradient(0, 0, this.width, this.height, 0xF00A0D16, 0xFA05070A);

        // 2. Render 3D Isometric Scene in the background
        float smoothTick = sceneTicks + (paused ? 0.0f : partialTick);
        Matrix4f sceneMatrix = render3DSceneInternal(graphics, smoothTick);

        // 3. Clear depth buffer & disable depth test so UI / Text / Boxes are NEVER occluded!
        RenderSystem.disableDepthTest();
        RenderSystem.clear(org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);

        // 4. Overlays in screen space (particles, indicators, rays)
        renderSceneOverlays(graphics, sceneMatrix, smoothTick);

        // 5. Render 2D UI elements in the FOREGROUND on higher Z plane
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 150.0f);

        renderCallouts(graphics, sceneMatrix, smoothTick);
        renderHeader(graphics);
        renderTimelineBar(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);

        pose.popPose();
    }

    @SuppressWarnings("deprecation")
    private Matrix4f render3DSceneInternal(GuiGraphics graphics, float smoothTick) {
        PoseStack poseStack = graphics.pose();
        poseStack.pushPose();

        float centerX = getSceneCenterX();
        float centerY = getSceneCenterY();
        float scale = getSceneScale();

        poseStack.translate(centerX, centerY, getSceneZOffset());
        poseStack.scale(scale, -scale, scale);

        poseStack.mulPose(Axis.XP.rotationDegrees(cameraPitch));
        poseStack.mulPose(Axis.YP.rotationDegrees(cameraYaw));

        applySceneCenterOffset(poseStack);

        Matrix4f capturedMatrix = new Matrix4f(poseStack.last().pose());

        Lighting.setupFor3DItems();
        RenderSystem.enableDepthTest();

        BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        render3DSceneContent(poseStack, bufferSource, blockRenderer, smoothTick);

        bufferSource.endBatch();
        Lighting.setupForFlatItems();
        poseStack.popPose();

        return capturedMatrix;
    }

    protected abstract void render3DSceneContent(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, BlockRenderDispatcher blockRenderer, float smoothTick);

    protected void renderSceneOverlays(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick) {
    }

    protected abstract void renderCallouts(GuiGraphics graphics, Matrix4f sceneMatrix, float smoothTick);

    protected void renderHeader(GuiGraphics graphics) {
        graphics.fill(0, 0, this.width, 36, 0xD00F172A);
        graphics.fill(0, 36, this.width, 37, getAccentColor());

        graphics.renderItem(targetStack, 14, 10);

        String itemTitle = getHeaderTitle();
        graphics.drawString(this.font, Component.literal("□■□ ").withStyle(ChatFormatting.DARK_AQUA)
                .append(Component.literal(itemTitle).withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD)), 38, 9, 0xFFFFFF, true);

        String subtitle = getHeaderSubtitle();
        if (subtitle != null && !subtitle.isEmpty()) {
            graphics.drawString(this.font, subtitle, 38, 21, 0x94A3B8, false);
        }

        String dragHint = I18n.get("ponder.witherutils.drag_hint");
        int hintWidth = this.font.width(dragHint);
        graphics.drawString(this.font, dragHint, this.width - hintWidth - 14, 14, 0x64748B, false);
    }

    protected void renderTimelineBar(GuiGraphics graphics) {
        int barWidth = Math.min(360, this.width - 60);
        int barX = (this.width - barWidth) / 2;
        int barY = this.height - 44;

        // Background groove
        graphics.fill(barX, barY, barX + barWidth, barY + 4, 0xFF1E293B);

        float progress = Mth.clamp(sceneTicks / (float) getTotalTicks(), 0.0f, 1.0f);
        int filled = Math.round(barWidth * progress);
        graphics.fill(barX, barY, barX + filled, barY + 4, getAccentColor());

        // Keyframe ticks
        int[] keyframes = getKeyframes();
        for (int kf : keyframes) {
            int kx = barX + Math.round((kf / (float) getTotalTicks()) * barWidth);
            graphics.fill(kx - 1, barY - 2, kx + 1, barY + 6, 0xFF94A3B8);
        }
    }

    protected void renderCalloutBox(GuiGraphics graphics, Vector4f anchor3D, String titleKey, String bodyText, int accentColor, float smoothTick, int stepStartTick) {
        renderCalloutBox(graphics, anchor3D, titleKey, bodyText, null, accentColor, smoothTick, stepStartTick);
    }

    /**
     * Renders a callout box with animated slide-in, pulsating beacon, connection elbow and optional flavor quote.
     */
    protected void renderCalloutBox(GuiGraphics graphics, Vector4f anchor3D, String titleKey, String bodyText, String comicQuote, int accentColor, float smoothTick, int stepStartTick) {
        if (anchor3D == null || titleKey == null || bodyText == null) {
            return;
        }

        int targetX = Math.round(anchor3D.x);
        int targetY = Math.round(anchor3D.y);

        int boxWidth = Math.min(235, Math.max(175, this.width / 3));
        int boxX = this.width - boxWidth - 22;
        int boxY = Math.max(50, Math.min(this.height - 150, targetY - 32));

        List<FormattedCharSequence> wrappedLines = this.font.split(Component.literal(bodyText), boxWidth - 18);
        List<FormattedCharSequence> quoteLines = comicQuote != null ? this.font.split(Component.literal(comicQuote), boxWidth - 18) : null;
        int boxHeight = 26 + (wrappedLines.size() * 11) + (quoteLines != null ? (quoteLines.size() * 10 + 6) : 0);

        float enterProgress = stepStartTick >= 0 ? Mth.clamp((smoothTick - stepStartTick) / 10.0f, 0.0f, 1.0f) : 1.0f;
        float slideOffset = (1.0f - easeOutCubic(enterProgress)) * 25.0f;
        int animatedBoxX = Math.round(boxX + slideOffset);

        int boxAnchorX = animatedBoxX;
        int boxAnchorY = boxY + 16;
        int elbowX = (targetX + boxAnchorX) / 2;

        // Connecting lines with subtle glow
        float pulse = (Mth.sin(smoothTick * 0.15f) + 1.0f) * 0.5f;
        int pulseAlpha = 0xAA + Math.round(pulse * 0x55);
        int lineColor = (pulseAlpha << 24) | (accentColor & 0x00FFFFFF);

        drawLine(graphics, targetX, targetY, elbowX, boxAnchorY, lineColor);
        drawLine(graphics, elbowX, boxAnchorY, boxAnchorX, boxAnchorY, lineColor);

        // Pulsating target beacon on the 3D block
        float ringRadius = 3.0f + pulse * 2.5f;
        int ringInt = Math.round(ringRadius);
        graphics.fill(targetX - ringInt - 1, targetY - ringInt - 1, targetX + ringInt + 1, targetY + ringInt + 1, 0x55000000);
        graphics.fill(targetX - 3, targetY - 3, targetX + 3, targetY + 3, accentColor);
        graphics.fill(targetX - 1, targetY - 1, targetX + 1, targetY + 1, 0xFFFFFFFF);

        // Box card styling (Glassmorphism dark card)
        graphics.fill(animatedBoxX - 2, boxY - 2, animatedBoxX + boxWidth + 2, boxY + boxHeight + 2, 0xEE090D16);
        graphics.fill(animatedBoxX, boxY, animatedBoxX + boxWidth, boxY + boxHeight, 0xF50F172A);
        // Left accent stripe
        graphics.fill(animatedBoxX, boxY, animatedBoxX + 3, boxY + boxHeight, accentColor);
        // Subtle outline
        graphics.fill(animatedBoxX, boxY, animatedBoxX + boxWidth, boxY + 1, 0x4494A3B8);
        graphics.fill(animatedBoxX, boxY + boxHeight - 1, animatedBoxX + boxWidth, boxY + boxHeight, 0x4494A3B8);

        // Title & Wrapped Text
        String title = I18n.get(titleKey);
        graphics.drawString(this.font, Component.literal(title).withStyle(ChatFormatting.BOLD), animatedBoxX + 10, boxY + 7, accentColor, true);

        int lineY = boxY + 21;
        for (FormattedCharSequence line : wrappedLines) {
            graphics.drawString(this.font, line, animatedBoxX + 10, lineY, 0xE2E8F0, false);
            lineY += 11;
        }

        if (quoteLines != null) {
            lineY += 4;
            for (FormattedCharSequence qLine : quoteLines) {
                graphics.drawString(this.font, qLine, animatedBoxX + 10, lineY, 0x94A3B8, false);
                lineY += 10;
            }
        }
    }

    protected Vector4f projectToScreen(Matrix4f matrix, float x, float y, float z) {
        Vector4f vec = new Vector4f(x, y, z, 1.0f);
        vec.mul(matrix);
        return vec;
    }

    protected void drawLine(GuiGraphics graphics, int x0, int y0, int x1, int y1, int color) {
        int dx = x1 - x0;
        int dy = y1 - y0;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        if (steps == 0) {
            graphics.fill(x0, y0, x0 + 1, y0 + 1, color);
            return;
        }
        for (int i = 0; i <= steps; i += 2) {
            int px = x0 + (dx * i) / steps;
            int py = y0 + (dy * i) / steps;
            graphics.fill(px - 1, py - 1, px + 1, py + 1, color);
        }
    }

    // --- Smooth Animation Helpers ---
    public static float easeOutCubic(float x) {
        return 1.0f - (float) Math.pow(1.0f - x, 3);
    }

    public static float easeOutBack(float x) {
        float c1 = 1.70158f;
        float c3 = c1 + 1.0f;
        return 1.0f + c3 * (float) Math.pow(x - 1.0f, 3) + c1 * (float) Math.pow(x - 1.0f, 2);
    }

    public static float easeInOutQuad(float x) {
        return x < 0.5f ? 2.0f * x * x : 1.0f - (float) Math.pow(-2.0f * x + 2.0f, 2) / 2.0f;
    }

    public static float easeOutBounce(float x) {
        float n1 = 7.5625f;
        float d1 = 2.75f;

        if (x < 1.0f / d1) {
            return n1 * x * x;
        } else if (x < 2.0f / d1) {
            return n1 * (x -= 1.5f / d1) * x + 0.75f;
        } else if (x < 2.5f / d1) {
            return n1 * (x -= 2.25f / d1) * x + 0.9375f;
        } else {
            return n1 * (x -= 2.625f / d1) * x + 0.984375f;
        }
    }

    // --- Interactive Mouse & Window Controls ---
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (button == 0 && mouseY > 38 && mouseY < this.height - 48) {
            isDragging = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isDragging && button == 0) {
            cameraYaw += (float) dragX * 0.65f;
            cameraPitch = Mth.clamp(cameraPitch + (float) dragY * 0.5f, -15.0f, 75.0f);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        cameraZoom = Mth.clamp(cameraZoom + (float) scrollY * 0.1f, 0.6f, 1.8f);
        return true;
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parentScreen);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
