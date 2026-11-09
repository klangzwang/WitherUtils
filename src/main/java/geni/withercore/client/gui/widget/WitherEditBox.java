package geni.withercore.client.gui.widget;

import codechicken.lib.gui.modular.lib.GuiRender;
import com.mojang.blaze3d.systems.RenderSystem;
import geni.withercore.client.shader.SdfShaderConfig;
import geni.withercore.client.shader.SdfShaderRenderer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class WitherEditBox extends EditBox {

    public WitherEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
    }
}
