package geni.witherutils.client.gui.screen;

import codechicken.lib.gui.modular.lib.GuiRender;
import geni.witherapi.client.gui.IHideJeiOverlay;
import geni.withercore.client.gui.GuiToolkit;
import geni.withercore.client.gui.WitherAbstractItemScreen;
import geni.withercore.client.gui.WitherSlotDecorator;
import geni.withercore.client.gui.widget.WitherCTMPreview;
import geni.withercore.common.helper.ColorHelper;
import geni.withercore.init.WUTBlocks;
import geni.witherdata.recipes.CutterRecipe;
import geni.witherutils.WitherUtils;
import geni.witherutils.common.menu.CutterMenu;
import geni.witherutils.common.world.item.CutterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.util.List;

public class CutterScreen extends WitherAbstractItemScreen<CutterMenu, CutterItem> implements IHideJeiOverlay {

    private float scrollOffs;
    private boolean scrolling;
    private int startIndex;
    private boolean displayRecipes;

    private WitherCTMPreview ctmPreview;

    public CutterScreen(CutterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        menu.registerUpdateListener(this::containerChanged);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 74;
    }

    @Override
    protected void init() {
        super.init();

        int previewX = this.leftPos + this.imageWidth + 6;
        int previewY = this.topPos;
        int previewH = this.imageHeight;
        int previewW = Math.max(160, Math.min(270, this.width - previewX - 12));

        if (this.ctmPreview == null) {
            this.ctmPreview = new WitherCTMPreview(previewX, previewY, previewW, previewH);
        } else {
            this.ctmPreview.setPosition(previewX, previewY);
            this.ctmPreview.setWidth(previewW);
            this.ctmPreview.setHeight(previewH);
        }

        /*this.addRenderableWidget(this.ctmPreview);*/
        /*this.updatePreview();*/
    }

    public static boolean isAthenaCtmBlock(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof BlockItem bi)) {
            return false;
        }
        Block block = bi.getBlock();
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        if (key.getNamespace().equals(WitherUtils.MODID) && key.getPath().contains("ctm")) {
            return true;
        }
        return WUTBlocks.CTM_CONCRETE.values().stream().anyMatch(ro -> ro.get() == block)
                || WUTBlocks.CTM_DIRT.values().stream().anyMatch(ro -> ro.get() == block)
                || WUTBlocks.CTM_GLASS.values().stream().anyMatch(ro -> ro.get() == block)
                || WUTBlocks.CTM_METAL.values().stream().anyMatch(ro -> ro.get() == block)
                || WUTBlocks.CTM_STONE.values().stream().anyMatch(ro -> ro.get() == block)
                || WUTBlocks.CTM_WOOD.values().stream().anyMatch(ro -> ro.get() == block);
    }

    private ItemStack getOutputStack() {
        ItemStack slotStack = this.menu.resultSlot.getItem();
        if (!slotStack.isEmpty()) {
            return slotStack;
        }
        int sel = this.menu.getSelectedRecipeIndex();
        if (this.menu.hasInputItem() && sel >= 0 && sel < this.menu.getNumRecipes()) {
            return this.menu.getRecipes().get(sel).value().getResultItem(this.minecraft.level.registryAccess());
        }
        return ItemStack.EMPTY;
    }

    private void updatePreview() {
        if (this.ctmPreview == null) {
            return;
        }
        ItemStack outputStack = getOutputStack();
        boolean isCtm = isAthenaCtmBlock(outputStack);

        this.ctmPreview.visible = isCtm;
        this.ctmPreview.active = isCtm;

        if (isCtm) {
            this.ctmPreview.setItemStack(outputStack);
        }
    }

    @Override
    public void modularRender(GuiGraphics gg, GuiRender render, GuiToolkit toolKit, float partialTicks, int mouseX, int mouseY) {

        toolKit.drawScaledText(render, "CUTTER", leftPos + 1, topPos - 12, 2.5f, ColorHelper.LIGHT_GRAY.getRGB(), false, 100);
        toolKit.drawStraightLine(render, toolKit.leftPos() + 8, toolKit.bottomPos() - 28, imageWidth - 16, 1, Color.LIGHT_GRAY);
        WitherSlotDecorator.renderPlayerHotbar(gg, toolKit.leftPos() + 8, toolKit.bottomPos() - 24);

        /*int x0 = this.leftPos;
        int y0 = this.topPos;

        // 1. Main Container Background (176 x 166) - Dark Slate Theme
        gg.fill(x0, y0, x0 + this.imageWidth, y0 + this.imageHeight, 0xFF121417);
        // Bevel highlight (top and left edges)
        gg.fill(x0 + 1, y0 + 1, x0 + this.imageWidth - 1, y0 + 2, 0xFF353C47);
        gg.fill(x0 + 1, y0 + 1, x0 + 2, y0 + this.imageHeight - 1, 0xFF353C47);
        // Bevel shadow (bottom and right edges)
        gg.fill(x0 + 1, y0 + this.imageHeight - 2, x0 + this.imageWidth - 1, y0 + this.imageHeight - 1, 0xFF0B0D0F);
        gg.fill(x0 + this.imageWidth - 2, y0 + 1, x0 + this.imageWidth - 1, y0 + this.imageHeight - 1, 0xFF0B0D0F);
        // Main panel fill
        gg.fill(x0 + 2, y0 + 2, x0 + this.imageWidth - 2, y0 + this.imageHeight - 2, 0xFF1E2229);

        // Header accent line under title & Title text
        gg.fill(x0 + 8, y0 + 15, x0 + 44, y0 + 16, 0xFFD49B24);
        gg.drawString(this.font, this.title, x0 + 8, y0 + 5, 0xFFE2E8F0, false);

        // 2. Input Slot Frame at (x0 + 20, y0 + 33, 18, 18)
        drawSlotBox(gg, x0 + 19, y0 + 32, 18, 18, 0xFF2A303A, 0xFF14161B);
        gg.drawCenteredString(this.font, "In", x0 + 28, y0 + 21, 0xFFA0AAB8);

        // 3. Output Slot Frame at (x0 + 143, y0 + 33, 18, 18) - with golden highlight
        drawSlotBox(gg, x0 + 142, y0 + 32, 18, 18, 0xFFD49B24, 0xFF14161B);
        gg.drawCenteredString(this.font, "Out", x0 + 151, y0 + 21, 0xFFFFD700);

        // Flow indicators
        gg.drawString(this.font, "►", x0 + 41, y0 + 37, 0xFF5A6475, false);
        gg.drawString(this.font, "►", x0 + 130, y0 + 37, 0xFF5A6475, false);

        // 4. Recipes Selection Panel (x0 + 51, y0 + 14, 73, 56)
        gg.fill(x0 + 51, y0 + 14, x0 + 125, y0 + 70, 0xFF131519);
        gg.fill(x0 + 51, y0 + 14, x0 + 125, y0 + 15, 0xFF0D0E11); // Top shadow
        gg.fill(x0 + 51, y0 + 14, x0 + 52, y0 + 70, 0xFF0D0E11); // Left shadow
        gg.fill(x0 + 51, y0 + 69, x0 + 125, y0 + 70, 0xFF2B313B); // Bottom highlight
        gg.fill(x0 + 124, y0 + 14, x0 + 125, y0 + 70, 0xFF2B313B); // Right highlight

        // 4x3 Recipe Slot buttons background
        int selectedIndex = this.menu.getSelectedRecipeIndex();
        int numRecipes = this.menu.getNumRecipes();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 4; col++) {
                int slotIndex = this.startIndex + row * 4 + col;
                int bx = x0 + 52 + col * 16;
                int by = y0 + 15 + row * 18;

                if (slotIndex < numRecipes) {
                    boolean isSelected = (slotIndex == selectedIndex);
                    boolean isHovered = (mouseX >= bx && mouseX < bx + 16 && mouseY >= by && mouseY < by + 18);

                    if (isSelected) {
                        gg.fill(bx, by, bx + 16, by + 18, 0xFF3D3214);
                        gg.renderOutline(bx, by, 16, 18, 0xFFFFD700);
                    } else if (isHovered) {
                        gg.fill(bx, by, bx + 16, by + 18, 0xFF2C333E);
                        gg.renderOutline(bx, by, 16, 18, 0xFF5E6D82);
                    } else {
                        gg.fill(bx, by, bx + 16, by + 18, 0xFF1C2026);
                        gg.renderOutline(bx, by, 16, 18, 0xFF262C35);
                    }
                } else {
                    gg.fill(bx, by, bx + 16, by + 18, 0xFF16181D);
                    gg.renderOutline(bx, by, 16, 18, 0xFF20242B);
                }
            }
        }

        // 5. Scrollbar Track & Thumb (x0 + 118, y0 + 15, 6, 54)
        gg.fill(x0 + 118, y0 + 15, x0 + 124, y0 + 69, 0xFF0D0F12);
        if (this.isScrollBarActive()) {
            int thumbY = y0 + 15 + (int) ((54 - 15) * this.scrollOffs);
            int thumbColor = this.scrolling ? 0xFFFFD700 : 0xFF546073;
            gg.fill(x0 + 119, thumbY, x0 + 123, thumbY + 15, thumbColor);
            gg.fill(x0 + 119, thumbY, x0 + 123, thumbY + 1, 0xFF8594AA);
        } else {
            gg.fill(x0 + 119, y0 + 15, x0 + 123, y0 + 69, 0xFF15181D);
        }

        // 6. Player Inventory Area Background & Slots
        gg.fill(x0 + 7, y0 + 71, x0 + 169, y0 + 72, 0xFF2B313B);
        gg.drawString(this.font, this.playerInventoryTitle, x0 + 8, y0 + 74, 0xFFA0AAB8, false);

        // 3 rows of 9 inventory slots at (x0 + 8 + col * 18, y0 + 84 + row * 18)
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 9; c++) {
                drawSlotBox(gg, x0 + 7 + c * 18, y0 + 83 + r * 18, 18, 18, 0xFF282E37, 0xFF14161B);
            }
        }

        // 1 row of 9 hotbar slots at (x0 + 8 + col * 18, y0 + 142)
        for (int c = 0; c < 9; c++) {
            drawSlotBox(gg, x0 + 7 + c * 18, y0 + 141, 18, 18, 0xFF282E37, 0xFF14161B);
        }

        // 7. Render recipe item icons & status messages
        if (this.menu.hasInputItem()) {
            this.renderRecipes(gg, this.leftPos + 52, this.topPos + 15, this.startIndex + 12);
        } else if (!this.menu.inputSlot.hasItem()) {
            gg.drawCenteredString(this.font, "Insert block", this.leftPos + 88, this.topPos + 38, 0xFFA0AAB8);
        } else {
            gg.drawCenteredString(this.font, "No recipes", this.leftPos + 88, this.topPos + 38, 0xFFC06060);
        }

        // Carving banner text
        if (this.menu.hasInputItem() && this.menu.getSelectedRecipeIndex() >= 0 && this.menu.getSelectedRecipeIndex() < this.menu.getNumRecipes()) {
            ItemStack resultStack = this.menu.getRecipes().get(this.menu.getSelectedRecipeIndex()).value().getResultItem(this.minecraft.level.registryAccess());
            String nameStr = resultStack.getHoverName().getString();
            if (this.font.width(nameStr) > 96) {
                nameStr = this.font.plainSubstrByWidth(nameStr, 90) + "...";
            }
            gg.drawCenteredString(this.font, nameStr, this.leftPos + 88, this.topPos + 64, 0xFFFFD700);
        }*/
    }

    private void drawSlotBox(GuiGraphics gg, int x, int y, int width, int height, int borderColor, int innerColor) {
        gg.fill(x, y, x + width, y + height, innerColor);
        gg.fill(x, y, x + width, y + 1, 0xFF0D0E11);
        gg.fill(x, y, x + 1, y + height, 0xFF0D0E11);
        gg.fill(x, y + height - 1, x + width, y + height, borderColor);
        gg.fill(x + width - 1, y, x + width, y + height, borderColor);
    }

    private void renderRecipes(GuiGraphics gg, int pX, int pY, int pEndIndex) {
        List<RecipeHolder<CutterRecipe>> list = this.menu.getRecipes();
        for (int i = this.startIndex; i < pEndIndex && i < this.menu.getNumRecipes(); i++) {
            int j = i - this.startIndex;
            int col = j % 4;
            int row = j / 4;
            int k = pX + col * 16;
            int l = pY + row * 18 + 1;
            ItemStack stack = list.get(i).value().getResultItem(this.minecraft.level.registryAccess());
            gg.renderItem(stack, k, l);
            gg.renderItemDecorations(this.font, stack, k, l);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics gg, int mouseX, int mouseY) {
        super.renderTooltip(gg, mouseX, mouseY);

        /*if (this.displayRecipes) {
            int recipeX = this.leftPos + 52;
            int recipeY = this.topPos + 15;
            for (int l = this.startIndex; l < this.startIndex + 12 && l < this.menu.getNumRecipes(); l++) {
                int j = l - this.startIndex;
                int col = j % 4;
                int row = j / 4;
                int rx = recipeX + col * 16;
                int ry = recipeY + row * 18;

                if (mouseX >= rx && mouseX < rx + 16 && mouseY >= ry && mouseY < ry + 18) {
                    RecipeHolder<CutterRecipe> rh = this.menu.getRecipes().get(l);
                    ItemStack resultStack = rh.value().getResultItem(this.minecraft.level.registryAccess());
                    gg.renderTooltip(this.font, resultStack, mouseX, mouseY);
                    break;
                }
            }
        }*/
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        /*this.scrolling = false;
        if (this.displayRecipes) {
            int recipeX = this.leftPos + 52;
            int recipeY = this.topPos + 15;

            for (int l = this.startIndex; l < this.startIndex + 12 && l < this.menu.getNumRecipes(); l++) {
                int j = l - this.startIndex;
                int col = j % 4;
                int row = j / 4;
                int rx = recipeX + col * 16;
                int ry = recipeY + row * 18;

                if (mouseX >= (double) rx && mouseX < (double) (rx + 16) &&
                        mouseY >= (double) ry && mouseY < (double) (ry + 18)) {
                    if (this.menu.clickMenuButton(this.minecraft.player, l)) {
                        Minecraft.getInstance().getSoundManager().play(
                                SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F)
                        );
                        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, l);
                        this.updatePreview();
                        return true;
                    }
                }
            }

            int scrollX = this.leftPos + 118;
            int scrollY = this.topPos + 15;
            if (mouseX >= (double) scrollX && mouseX < (double) (scrollX + 6) &&
                    mouseY >= (double) scrollY && mouseY < (double) (scrollY + 54)) {
                this.scrolling = true;
                return true;
            }
        }*/
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        /*if (this.scrolling && this.isScrollBarActive()) {
            int trackTop = this.topPos + 15;
            int trackBottom = trackTop + 54;
            this.scrollOffs = ((float) mouseY - (float) trackTop - 7.5F) / ((float) (trackBottom - trackTop) - 15.0F);
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) this.getOffscreenRows()) + 0.5D) * 4;
            return true;
        }*/
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        /*this.scrolling = false;*/
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        /*if (this.isScrollBarActive() && isOverRecipeArea(mouseX, mouseY)) {
            int i = this.getOffscreenRows();
            float f = (float) scrollY / (float) i;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int) ((double) (this.scrollOffs * (float) i) + 0.5D) * 4;
            return true;
        }*/
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isOverRecipeArea(double mouseX, double mouseY) {
        return mouseX >= (double) (this.leftPos + 51) && mouseX <= (double) (this.leftPos + 125) &&
                mouseY >= (double) (this.topPos + 14) && mouseY <= (double) (this.topPos + 70);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.updatePreview();
    }

    private boolean isScrollBarActive() {
        return this.displayRecipes && this.menu.getNumRecipes() > 12;
    }

    protected int getOffscreenRows() {
        return (this.menu.getNumRecipes() + 4 - 1) / 4 - 3;
    }

    private void containerChanged() {
        this.displayRecipes = this.menu.hasInputItem();
        if (!this.displayRecipes) {
            this.scrollOffs = 0.0F;
            this.startIndex = 0;
        }
        this.updatePreview();
    }
}
