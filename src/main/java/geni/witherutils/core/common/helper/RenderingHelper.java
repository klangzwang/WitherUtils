package geni.witherutils.core.common.helper;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class RenderingHelper {

    public static float rot = 0.0f;

    @SuppressWarnings("resource")
	public static void renderEntity(Entity entity, PoseStack matrixStack, int xPos, int yPos, float scale)
    {
        matrixStack.pushPose();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        matrixStack.translate(xPos + 8, yPos + 24, 50F);
        matrixStack.scale(-scale, scale, scale);
        matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
        matrixStack.mulPose(Axis.YP.rotationDegrees(135));
        Lighting.setupForEntityInInventory();
        matrixStack.mulPose(Axis.YP.rotationDegrees(-135));
        matrixStack.mulPose(Axis.YP.rotationDegrees(rot));
        matrixStack.mulPose(Axis.XP.rotationDegrees(0));

        if (!(entity instanceof Player))
        {
            entity.setXRot(0.0F);
            entity.xRotO = 0.0F;
            entity.setYRot(0.0f);
            entity.yRotO = 0.0f;

            if (entity instanceof LivingEntity livingEntity)
            {
                livingEntity.yBodyRotO = 0.0f;
                livingEntity.yBodyRot = 0.0f;

                livingEntity.yHeadRot = 0.0f;
                livingEntity.yHeadRotO = 0.0f;
            }
        }

        float ridingOffset;
        if (entity.getVehicle() == null)
        {
            ridingOffset = 0.0f;
        }
        else
        {
            ridingOffset = (float) entity.getMyRidingOffset();
        }
        matrixStack.translate(0.0F, ridingOffset + (entity instanceof HangingEntity ? 0.5F : 0.0F), 0.0F);
        RenderSystem.applyModelViewMatrix();

        EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
            dispatcher.setRenderShadow(false);
            dispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, matrixStack, buffer, 15728880);
            buffer.endBatch();
        } catch (Exception e) {
        }
        dispatcher.setRenderShadow(true);
        Lighting.setupFor3DItems();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        matrixStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void drawHorizontalLine(GuiGraphics graphics, int x1, int y1, int x2, int color)
    {
        graphics.fill(x1, y1, x2, y1 + 1, color);
    }

    public static void drawVerticalLine(GuiGraphics graphics, int x1, int y1, int y2, int color)
    {
        graphics.fill(x1, y1, x1 + 1, y2, color);
    }

    public static void drawThickBeveledBox(GuiGraphics graphics, int x1, int y1, int x2, int y2, int thickness, int topleftcolor, int botrightcolor, int fillcolor)
    {
        if (fillcolor != -1)
        {
            graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillcolor);
        }
        graphics.fill(x1, y1, x2 - 1, y1 + thickness, topleftcolor);
        graphics.fill(x1, y1, x1 + thickness, y2 - 1, topleftcolor);
        graphics.fill(x2 - thickness, y1, x2, y2 - 1, botrightcolor);
        graphics.fill(x1, y2 - thickness, x2, y2, botrightcolor);
    }

    public static void drawTexturedModalRect(Matrix4f matrix, int x, int y, int u, int v, int width, int height, int twidth, int theight)
    {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        float zLevel = 0.01f;
        float f = (1.0f / twidth);
        float f1 = (1.0f / theight);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.vertex(matrix, (x), (y + height), zLevel).uv(((u) * f), ((v + height) * f1)).endVertex();
        buffer.vertex(matrix, (x + width), (y + height), zLevel).uv(((u + width) * f), ((v + height) * f1)).endVertex();
        buffer.vertex(matrix, (x + width), (y), zLevel).uv(((u + width) * f), ((v) * f1)).endVertex();
        buffer.vertex(matrix, (x), (y), zLevel).uv(((u) * f), ((v) * f1)).endVertex();
        tessellator.end();
    }

    public static void drawTexturedModalRect(Matrix4f matrix, int x, int y, int u, int v, int width, int height)
    {
        float zLevel = 0.01f;
        float f = (1 / 256.0f);
        float f1 = (1 / 256.0f);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.vertex(matrix, (x), (y + height), zLevel).uv(((u) * f), ((v + height) * f1)).endVertex();
        buffer.vertex(matrix, (x + width), (y + height), zLevel).uv(((u + width) * f), ((v + height) * f1)).endVertex();
        buffer.vertex(matrix, (x + width), (y), zLevel).uv(((u + width) * f), ((v) * f1)).endVertex();
        buffer.vertex(matrix, (x), (y), zLevel).uv(((u) * f), ((v) * f1)).endVertex();
        tessellator.end();
    }

	public static void drawTexturedModalRect(Matrix4f matrix, int x, int y, TextureAtlasSprite sprite, int width, int height)
    {
        float zLevel = 0.01f;
        float f = (1 / 256.0f);
        float f1 = (1 / 256.0f);

        float u1 = sprite.getU0();
        float v1 = sprite.getV0();
        float u2 = sprite.getU1();
        float v2 = sprite.getV1();

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.vertex(matrix, (x), (y + height), zLevel).uv(u1, v1).endVertex();
        buffer.vertex(matrix, (x + width), (y + height), zLevel).uv(u1, v2).endVertex();
        buffer.vertex(matrix, (x + width), (y), zLevel).uv(u2, v2).endVertex();
        buffer.vertex(matrix, (x), (y), zLevel).uv(u2, v1).endVertex();
        tessellator.end();
    }

    public static boolean renderItemStack(Minecraft mc, ItemRenderer itemRender, ItemStack itm, GuiGraphics graphics, int x, int y, String txt)
    {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);
        PoseStack matrixStack = graphics.pose();

        boolean rc = true;
        if (!itm.isEmpty() && itm.getItem() != null) {
            matrixStack.pushPose();
            matrixStack.translate(0.0F, 0.0F, 32.0F);
            RenderSystem.applyModelViewMatrix();
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Lighting.setupFor3DItems();
            try {
                graphics.renderItem(itm, x, y, x * y * 31);
                renderItemStackOverlay(graphics, mc.font, itm, x, y, txt, txt.length() - 2);
            } catch (Exception e) {
                rc = false;
            }
            matrixStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        return rc;
    }

    @SuppressWarnings("resource")
	public static void renderItemStackOverlay(GuiGraphics graphics, Font fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text, int scaled)
    {
        PoseStack matrixStack = graphics.pose();
        if (!stack.isEmpty()) {
            if (stack.getCount() != 1 || text != null) {
                String s = text == null ? String.valueOf(stack.getCount()) : text;
                if (text == null && stack.getCount() < 1) {
                    s = ChatFormatting.RED + String.valueOf(stack.getCount());
                }

                matrixStack.translate(0.0D, 0.0D, 200.0F);

                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                if (scaled >= 2) {
                    matrixStack.pushPose();
                    matrixStack.scale(.5f, .5f, .5f);
                    graphics.drawString(fr, s, ((xPosition + 19 - 2) * 2 - fr.width(s)), yPosition * 2 + 24, 16777215);
                    matrixStack.popPose();
                } else if (scaled == 1) {
                    matrixStack.pushPose();
                    matrixStack.scale(.75f, .75f, .75f);
                    graphics.drawString(fr, s, (int) ((xPosition - 2) * 1.34f + 24 - fr.width(s)), (int) (yPosition * 1.34f + 14), 16777215);
                    matrixStack.popPose();
                } else {
                    graphics.drawString(fr, s, (xPosition + 19 - 2 - fr.width(s)), (yPosition + 6 + 3), 16777215);
                }
                RenderSystem.enableDepthTest();
                RenderSystem.enableBlend();
            }

            if (stack.getItem().isBarVisible(stack)) {
                double health = stack.getItem().getBarWidth(stack);
                int i = Math.round(13.0F - (float)health * 13.0F);
                int j = stack.getItem().getBarColor(stack);
                RenderSystem.disableDepthTest();
                RenderSystem.disableBlend();
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder vertexbuffer = tessellator.getBuilder();
                Matrix4f matrix = matrixStack.last().pose();
                draw(vertexbuffer, matrix, xPosition + 2, yPosition + 13, 13, 2, 0, 0, 0, 255);
                draw(vertexbuffer, matrix, xPosition + 2, yPosition + 13, 12, 1, (255 - i) / 4, 64, 0, 255);
                draw(vertexbuffer, matrix, xPosition + 2, yPosition + 13, i, 1, j >> 16 & 255, j >> 8 & 255, j & 255, 255);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
            }

            Player playerEntitysp = Minecraft.getInstance().player;
            float f = playerEntitysp == null ? 0.0F : playerEntitysp.getCooldowns().getCooldownPercent(stack.getItem(), Minecraft.getInstance().getFrameTime());

            if (f > 0.0F) {
                RenderSystem.disableDepthTest();
                Tesselator tessellator1 = Tesselator.getInstance();
                BufferBuilder vertexbuffer1 = tessellator1.getBuilder();
                draw(vertexbuffer1, matrixStack.last().pose(), xPosition, yPosition + (int) Math.floor(16.0F * (1.0F - f)), 16, (int) Math.ceil(16.0F * f), 255, 255, 255, 127);
                RenderSystem.enableDepthTest();
            }
        }
    }

    private static void draw(BufferBuilder renderer, Matrix4f matrix, int x, int y, int width, int height, int red, int green, int blue, int alpha)
    {
        renderer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        renderer.vertex(matrix, (x), (y), 0).color(red, green, blue, alpha).endVertex();
        renderer.vertex(matrix, (x), (y + height), 0).color(red, green, blue, alpha).endVertex();
        renderer.vertex(matrix, (x + width), (y + height), 0).color(red, green, blue, alpha).endVertex();
        renderer.vertex(matrix, (x + width), (y), 0).color(red, green, blue, alpha).endVertex();
        Tesselator.getInstance().end();
    }


    public static int renderText(Minecraft mc, GuiGraphics graphics, int x, int y, String txt)
    {
        PoseStack matrixStack = graphics.pose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);

        matrixStack.pushPose();
        matrixStack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Lighting.setupFor3DItems();

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        int width = mc.font.width(txt);
        graphics.drawString(mc.font, txt, x, y, 16777215);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        matrixStack.popPose();
        return width;
    }

    public static int renderText(Minecraft mc, GuiGraphics graphics, int x, int y, Component text)
    {
        PoseStack stack = graphics.pose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0f);

        stack.pushPose();
        stack.translate(0.0F, 0.0F, 32.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        Lighting.setupFor3DItems();

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        int width = mc.font.width(text.getVisualOrderText());
        graphics.drawString(mc.font, text, x, y, 16777215);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        stack.popPose();

        return width;
    }

    public record Vector(float x, float y, float z)
    {
        public float norm()
        {
            return (float) Math.sqrt(x * x + y * y + z * z);
        }
        public Vector normalize()
        {
            float n = norm();
            return new Vector(x / n, y / n, z / n);
        }
    }

	private static Vector cross(Vector a, Vector b) {
        float x = a.y * b.z - a.z * b.y;
        float y = a.z * b.x - a.x * b.z;
        float z = a.x * b.y - a.y * b.x;
        return new Vector(x, y, z);
    }

    private static Vector sub(Vector a, Vector b) {
        return new Vector(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    private static Vector add(Vector a, Vector b) {
        return new Vector(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    private static Vector mul(Vector a, float f) {
        return new Vector(a.x * f, a.y * f, a.z * f);
    }


}