package geni.witherutils.common.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.joml.Quaternionf;

import geni.witherutils.WitherUtils;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherdata.recipes.HammerRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class HammerRecipeCategory implements IRecipeCategory<HammerRecipe> {

    public static final ResourceLocation TEXTURES = WitherUtils.loc("textures/jei/wut_recipe.png");

    public static final mezz.jei.api.recipe.RecipeType<HammerRecipe> TYPE = mezz.jei.api.recipe.RecipeType.create(WitherUtils.MODID, "hammer", HammerRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;

    public HammerRecipeCategory(@Nonnull IGuiHelper guiHelper)
    {
        this.background = guiHelper.createDrawable(TEXTURES, 0, 0, 162, 80);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(WUTItems.HAMMER.get()));
    }

	@Nonnull
    public static List<HammerRecipe> getRecipes()
    {
        List<HammerRecipe> recipes = new ArrayList<>();
        recipes.add(new HammerRecipe(new ItemStack(Blocks.ANVIL), new ItemStack(WUTItems.HAMMER.get()), new ItemStack(WUTBlocks.ANVIL.get())));
        recipes.add(new HammerRecipe(new ItemStack(Blocks.BLACK_CONCRETE), new ItemStack(WUTItems.HAMMER.get()), new ItemStack(WUTBlocks.SLICEDCONCRETEBLACK.get(), 8)));
        recipes.add(new HammerRecipe(new ItemStack(Blocks.GRAY_CONCRETE), new ItemStack(WUTItems.HAMMER.get()), new ItemStack(WUTBlocks.SLICEDCONCRETEGRAY.get(), 8)));
        recipes.add(new HammerRecipe(new ItemStack(Blocks.WHITE_CONCRETE), new ItemStack(WUTItems.HAMMER.get()), new ItemStack(WUTBlocks.SLICEDCONCRETEWHITE.get(), 8)));
        return recipes;
    }

    @Nonnull
    public static List<ItemStack> getCatalysts()
    {
        return List.of(new ItemStack(WUTItems.HAMMER.get()));
    }

    @Override
    public int getWidth()
    {
        return 162;
    }

    @Override
    public int getHeight()
    {
        return 80;
    }

    @Nonnull
    @Override
    public mezz.jei.api.recipe.RecipeType<HammerRecipe> getRecipeType()
    {
        return TYPE;
    }

    @Nonnull
    @Override
    public Component getTitle()
    {
        return Component.literal("Hammer");
    }

    @Nonnull
    @Override
    public IDrawable getIcon()
    {
        return icon;
    }

    @Override
    public void setRecipe(@Nonnull IRecipeLayoutBuilder builder, @Nonnull HammerRecipe recipe, @Nonnull IFocusGroup focuses)
    {
        builder.addSlot(RecipeIngredientRole.INPUT, 8 + 1, 24 + 1).addItemStack(recipe.input());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 102 + 1, 24 + 1).addItemStack(recipe.output());
    }

	@Override
    public void draw(@Nonnull HammerRecipe recipe, @Nonnull IRecipeSlotsView recipeSlotsView, @Nonnull GuiGraphics guiGraphics, double mouseX, double mouseY)
    {
        background.draw(guiGraphics, 0, 0);

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();

        Quaternionf quat = new Quaternionf();
        quat.rotationXYZ(30 * Mth.DEG_TO_RAD, 45 * Mth.DEG_TO_RAD, 0);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(52, 40, 128 - 32);
        guiGraphics.pose().scale(16, -16, 16);
        guiGraphics.pose().mulPose(quat);
        
        BlockState state = Block.byItem(recipe.block().getItem()).defaultBlockState();
        dispatcher.renderSingleBlock(state, guiGraphics.pose(), bufferSource, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, null);

        guiGraphics.pose().popPose();

        Quaternionf rotquat = new Quaternionf();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(63, 16, 128 - 16);
        float scale = 16f;
        guiGraphics.pose().scale(scale, scale, scale);

        rotquat.rotationXYZ(0, 0.25f, 0.5f);
        guiGraphics.pose().mulPose(rotquat);
        
		float time = 0.0F;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null) {
			time = mc.level.getLevelData().getGameTime() + mc.getFps();
		}
		float offset = (float) (Math.sin(time / 3.0D) / 10.0D);

        rotquat.rotationXYZ(0, 0, 12 * offset);
        guiGraphics.pose().mulPose(rotquat);
        
        ItemStack toDisplay = recipe.input();
        itemRenderer.renderStatic(toDisplay, ItemDisplayContext.FIXED, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, guiGraphics.pose(), bufferSource, null, 0);
        guiGraphics.pose().popPose();

        bufferSource.endBatch();
    }
}
