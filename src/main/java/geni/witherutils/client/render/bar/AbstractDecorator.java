package geni.witherutils.client.render.bar;

import geni.withercore.client.gui.GuiToolkit;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;

public abstract class AbstractDecorator implements IItemDecorator {

	protected final GuiToolkit guiToolKit;

    public AbstractDecorator()
    {
		this.guiToolKit = new GuiToolkit();
    }

	@Override
	public boolean render(GuiGraphics gg, Font font, ItemStack stack, int xOffset, int yOffset)
	{
		return true;
	}
}
