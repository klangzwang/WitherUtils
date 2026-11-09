package geni.witherutils.client.block.render.entity;

import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import geni.withercore.common.helper.RenderHelper;
import geni.withercore.client.block.render.entity.AbstractBlockEntityRenderer;
import geni.witherutils.common.world.level.block.entity.CreativeGeneratorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class CreativeGeneratorRenderer extends AbstractBlockEntityRenderer<CreativeGeneratorBlockEntity> {

    private static final ResourceLocation GRADIENT_MASK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("witherutils", "textures/bw_gradient_mask.png");
    public static ShaderInstance vanishingShader;

	public CreativeGeneratorRenderer(Context context)
	{
		super(context);
	}

    @Override
    public void render(CreativeGeneratorBlockEntity te, float partialTick, PoseStack matrix, MultiBufferSource bufferIn, Minecraft mc, ClientLevel level, LocalPlayer player, int combinedLight, int overlayLight)
    {
        if(te.getLevel() == null)
            return;

        matrix.pushPose();
        renderCube(te, partialTick, matrix, bufferIn, mc, level, player, combinedLight, overlayLight);
        matrix.popPose();

        matrix.pushPose();
        renderItem(te, partialTick, matrix, bufferIn, mc, level, player, combinedLight, overlayLight);
        matrix.popPose();
    }

    public void renderItem(CreativeGeneratorBlockEntity tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlay)
    {
        matrix.pushPose();

        ItemStack stack = tile.getInventory().getStackInSlot(CreativeGeneratorBlockEntity.INPUT.getIndex());

        if(!stack.isEmpty())
        {
            float time = tile.getLevel().getLevelData().getGameTime() + partialTicks;
            double offset = Math.sin(time * 1.0 / 8.0D) / 10.0D;

            double x = Vector3.CENTER.x - 0.0F;
            double y = Vector3.CENTER.y - 0.0F;
            double z = Vector3.CENTER.z - 0.0F;

            matrix.translate(x, y, z);
            matrix.translate(0, 0.5, 0);

            matrix.translate(0, 0.5 + offset, 0);
            matrix.mulPose(Axis.ZP.rotationDegrees(90));
            matrix.mulPose(Axis.XP.rotationDegrees(time * 4.0F * 1.0f));

            matrix.scale(0.5f, 0.5f, 0.5f);

            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            BakedModel bakedModel = itemRenderer.getModel(stack, mc.level, null, 0);
            itemRenderer.render(stack, ItemDisplayContext.FIXED, true, matrix, buffer, light, overlay, bakedModel);
        }

        matrix.popPose();
    }

    public void renderCube(CreativeGeneratorBlockEntity te, float partialTick, PoseStack matrixStack, MultiBufferSource bufferIn, Minecraft mc, ClientLevel level, LocalPlayer player, int combinedLight, int overlayLight)
    {
        float xMin, xMax, yMin, yMax, zMin, zMax = 0;

        xMax = 1.984375F;
        yMax = 0.984375F;
        zMax = 1.984375F;
        xMin = 0.015625F;
        zMin = 0.015625F;
        yMin = 0.015625F;

        float alpha = 0.25F;
        float red = (0xFFFFFFFF >> 16 & 0xFF) / 255.0F;
        float green = (0xFFFFFFFF >> 8 & 0xFF) / 255.0F;
        float blue = (0xFFFFFFFF & 0xFF) / 255.0F;

        VertexConsumer buffer = ItemRenderer.getCompassFoilBuffer(bufferIn, RenderType.translucent(), matrixStack.last());
        TextureAtlasSprite textureAtlasSprite = RenderHelper.getBlockTexture(BuiltInRegistries.BLOCK.getKey(Blocks.MAGENTA_CONCRETE));

        float uMin = textureAtlasSprite.getU0();
        float uMax = textureAtlasSprite.getU1();
        float vMin = textureAtlasSprite.getV0();
        float vMax = textureAtlasSprite.getV1();

        float vHeight = vMax - vMin;

        addVertexWithUV(buffer, matrixStack, xMax, yMax, zMax, uMax, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMax, zMin, uMin, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMax, zMin, uMin, vMax, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMax, zMax, uMax, vMax, red, green, blue, alpha, combinedLight);

        addVertexWithUV(buffer, matrixStack, xMax, yMin, zMin, uMax, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMin, zMin, uMin, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMax, zMin, uMin, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMax, zMin, uMax, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);

        addVertexWithUV(buffer, matrixStack, xMax, yMin, zMax, uMin, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMax, zMax, uMin, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMax, zMax, uMax, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMin, zMax, uMax, vMin, red, green, blue, alpha, combinedLight);

        addVertexWithUV(buffer, matrixStack, xMax, yMin, zMin, uMin, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMax, zMin, uMin, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMax, zMax, uMax, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMin, zMax, uMax, vMin, red, green, blue, alpha, combinedLight);

        addVertexWithUV(buffer, matrixStack, xMin, yMin, zMax, uMin, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMax, zMax, uMin, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMax, zMin, uMax, vMin + (vHeight * yMax), red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMin, zMin, uMax, vMin, red, green, blue, alpha, combinedLight);

        addVertexWithUV(buffer, matrixStack, xMax, yMin, zMin, uMax, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMax, yMin, zMax, uMin, vMin, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMin, zMax, uMin, vMax, red, green, blue, alpha, combinedLight);
        addVertexWithUV(buffer, matrixStack, xMin, yMin, zMin, uMax, vMax, red, green, blue, alpha, combinedLight);
    }

    private void addVertexWithUV(VertexConsumer buffer, PoseStack matrixStack, float x, float y, float z, float u, float v, float red, float green, float blue, float alpha, int combinedLight)
    {
        buffer.addVertex(matrixStack.last().pose(), x / 2f, y, z / 2f).setColor(red, green, blue, alpha).setUv(u, v).setUv2(combinedLight, 240).setNormal(1, 0, 0);
    }

    @Override
    public int getViewDistance()
    {
        return 256;
    }

    @Override
    public boolean shouldRenderOffScreen(CreativeGeneratorBlockEntity p_188185_1_)
    {
        return true;
    }
}
