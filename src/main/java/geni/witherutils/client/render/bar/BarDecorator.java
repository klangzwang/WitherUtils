package geni.witherutils.client.render.bar;

import geni.witherutils.common.world.level.block.CutterBlock;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class BarDecorator extends AbstractDecorator {

    public static final BarDecorator INSTANCE = new BarDecorator();

	private static final float COUNT_BLIT_HEIGHT = 200;

	@Override
	public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
		if (stack.getItem() instanceof BlockItem blockItem) {
			if (blockItem.getBlock() instanceof CutterBlock) {

				guiGraphics.pose().pushPose();

				long time = System.currentTimeMillis();

				float pulse = (float) Math.sin(time / 250.0) * 0.05F;
				float scale = 0.5F + pulse;

				int textWidth = font.width("●");
				int textHeight = font.lineHeight;
				float widthDiff = textWidth * (0.4F - scale);
				float heightDiff = textHeight * (0.4F - scale);

				guiGraphics.pose().translate(xOffset + 1.0f, yOffset, COUNT_BLIT_HEIGHT - 1);

				float r = (float) (Math.sin(time / 1800.0) * 0.5 + 0.5);
				float g = 0.6F;
				float b = (float) (Math.cos(time / 1800.0) * 0.5 + 0.5);

				int red   = (int) (r * 255);
				int green = (int) (g * 255);
				int blue  = (int) (b * 255);
				int color = (0xFF << 24) | (red << 16) | (green << 8) | blue;

				guiGraphics.pose().scale(scale, scale, 1.0F);
				guiGraphics.drawString(font, "●", 0, 0, color, false);
				guiGraphics.pose().popPose();

				return true;
			}
		}
		return false;
	}
}
