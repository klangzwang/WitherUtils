package geni.witherutils.core.client.gui.widgets;

import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import geni.witherutils.core.client.gui.screen.IWitherScreen;
import geni.witherutils.core.common.math.Vector2i;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ToggleImageButton<U extends Screen & IWitherScreen> extends AbstractWidget {

    private final U addedOn;

    private final ResourceLocation resourceLocation;
    private final int xTexStart;
    private final int yTexStart;
    private final int xDiffTex;
    private final int yDiffTex;
    private final int textureWidth;
    private final int textureHeight;

    private final Supplier<Boolean> getter;
    private final Consumer<Boolean> setter;
    private final Supplier<Component> tooltip;

    public ToggleImageButton(U addedOn, int x, int y, int width, int height, int xTexStart, int yTexStart, int xDiffTex, int yDiffTex,
        ResourceLocation resourceLocation, Supplier<Boolean> getter, Consumer<Boolean> setter, Supplier<Component> tooltip)
    {
        this(addedOn, x, y, width, height, xTexStart, yTexStart, xDiffTex, yDiffTex, resourceLocation, 256, 256, getter, setter, tooltip);
    }

    public ToggleImageButton(U addedOn, int x, int y, int width, int height, int xTexStart, int yTexStart, int xDiffTex, int yDiffTex,
        ResourceLocation resourceLocation, int textureWidth, int textureHeight, Supplier<Boolean> getter, Consumer<Boolean> setter,
        Supplier<Component> tooltip)
    {
        super(x, y, width, height, Component.empty());
        this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.xTexStart = xTexStart;
        this.yTexStart = yTexStart;
        this.xDiffTex = xDiffTex;
        this.yDiffTex = yDiffTex;
        this.resourceLocation = resourceLocation;
        this.addedOn = addedOn;
        this.getter = getter;
        this.setter = setter;
        this.tooltip = tooltip;
    }

    @Nullable
    private Component tooltipCache;

    @Override
    public void renderWidget(GuiGraphics gg, int pMouseX, int pMouseY, float partialTick)
    {
        Vector2i pos = new Vector2i(getX(), getY());
        
        addedOn.renderSimpleArea(gg, pos, pos.add(new Vector2i(width, height)));

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.resourceLocation);

        int xTex = this.xTexStart;
        int yTex = this.yTexStart;

        if (getter.get())
        {
            xTex += xDiffTex;
            yTex += yDiffTex;
        }

        RenderSystem.enableDepthTest();
        gg.blit(this.resourceLocation, getX(), getY(), (float) xTex, (float) yTex, this.width, this.height, this.textureWidth, this.textureHeight);

        if (this.isHovered && tooltipCache != tooltip.get())
        {
            tooltipCache = tooltip.get();
            setTooltip(Tooltip.create(this.tooltip.get().copy().withStyle(ChatFormatting.WHITE)));
        }
    }
    
    @Override
    public void onClick(double mouseX, double mouseY)
    {
        setter.accept(!getter.get());
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {}
}
