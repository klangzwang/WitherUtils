package geni.witherutils.client.block.render.entity.rack;

import codechicken.lib.vec.Vector3;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import geni.withercore.client.block.render.entity.AbstractBlockEntityRenderer;
import geni.witherutils.common.world.level.block.rack.controller.fluid.ControllerFluidBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

public class ControllerFluidRenderer extends AbstractBlockEntityRenderer<ControllerFluidBlockEntity> {
	
    public ControllerFluidRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(ControllerFluidBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, Minecraft mc, ClientLevel level, LocalPlayer player, int light, int combinedOverlayIn)
    {
        if (tile.getLevel() == null)
            return;
        if (!tile.isFormed())
            return;
        
        matrixStack.pushPose();
        renderFluidAndText(tile, partialTicks, matrixStack, bufferIn, light, combinedOverlayIn);
        matrixStack.popPose();
	}
    
	public void renderFluidAndText(ControllerFluidBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		for(Direction facing : Direction.values())
		{
			if(facing != Direction.UP && facing != Direction.DOWN)
			{
				matrixStack.pushPose();
				
				double x = Vector3.CENTER.x;
				double y = Vector3.CENTER.y;
				double z = Vector3.CENTER.z;

				matrixStack.translate(x, y, z);
				matrixStack.scale(0.5f, 0.5f, 0.5f);
				matrixStack.mulPose(Axis.YP.rotationDegrees(45));
				
				if(facing == Direction.NORTH)
				{
					matrixStack.translate(3.2, 2.4, 0);
				}
				else if(facing == Direction.EAST)
				{
					matrixStack.translate(0, 2.4, 3.2);
				}
				else if(facing == Direction.SOUTH)
				{
					matrixStack.translate(-3.2, 2.4, 0);
				}
				else if(facing == Direction.WEST)
				{
					matrixStack.translate(0, 2.4, -3.2);
				}

				matrixStack.popPose();

				renderText(matrixStack, bufferIn, combinedOverlayIn, Component.literal(ChatFormatting.WHITE + tile.getFormatedDisplayAmount()), facing, 0.015f);
			}
		}
	}
	
	private void renderText(PoseStack matrix, MultiBufferSource renderer, int overlayLight, Component text, Direction side, float maxScale)
	{
		matrix.pushPose();

		float displayWidth = 1;
		float displayHeight = 1;
		matrix.translate(displayWidth / 2, 1.22, displayHeight / 2);

		if (side == Direction.NORTH) {
			matrix.translate(0.0, 0.5, -1.2501);
			matrix.mulPose(Axis.YP.rotationDegrees(180));
		} else if (side == Direction.SOUTH) {
			matrix.translate(0.0, 0.5, 1.2501);
		} else if (side == Direction.WEST) {
			matrix.translate(-1.2501, 0.5, 0.0);
			matrix.mulPose(Axis.YP.rotationDegrees(270));
		} else if (side == Direction.EAST) {
			matrix.translate(1.2501, 0.5, 0.0);
			matrix.mulPose(Axis.YP.rotationDegrees(90));
		}

		@SuppressWarnings("resource")
		Font font = Minecraft.getInstance().font;

		int requiredWidth = Math.max(font.width(text), 1);
		int requiredHeight = font.lineHeight + 2;
		float scaler = 0.4F;
		float scaleX = displayWidth / requiredWidth;
		float scale = scaleX * scaler;
		if (maxScale > 0) {
			scale = Math.min(scale, maxScale);
		}

		matrix.scale(scale, -scale, scale);
		int realHeight = (int) Math.floor(displayHeight / scale);
		int realWidth = (int) Math.floor(displayWidth / scale);
		int offsetX = (realWidth - requiredWidth) / 2;
		int offsetY = (realHeight - requiredHeight) / 2;
		font.drawInBatch(text, offsetX - realWidth / 2f, 3 + offsetY - realHeight / 2f, overlayLight, false, matrix.last().pose(), renderer, Font.DisplayMode.NORMAL, 0, 0xF000F0);
		matrix.popPose();
	}
}
