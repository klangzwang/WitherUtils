package geni.witherutils.common.plugin;

import geni.withercore.init.WUTItems;
import geni.witherdata.recipes.CutterRecipe;
import geni.witherdata.recipes.HammerRecipe;
import geni.witherutils.WitherUtils;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import static mezz.jei.api.recipe.RecipeIngredientRole.INPUT;
import static mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT;

public class CutterRecipeCategory implements IRecipeCategory<CutterRecipe> {

	public static final RecipeType<CutterRecipe> TYPE = RecipeType.create(WitherUtils.MODID, "cutter", CutterRecipe.class);

	private static final int FONT = 4210752;
	private final IDrawableStatic gui;
	private final IDrawable icon;

	public CutterRecipeCategory(IGuiHelper helper)
	{
		gui = helper.drawableBuilder(WitherUtils.loc("textures/jei/cutter_recipe.png"), 0, 0, 169, 69).setTextureSize(169, 69).build();
		icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(WUTItems.CUTTER.get()));
	}

	@Override
	public int getWidth()
	{
		return 169;
	}

	@Override
	public int getHeight()
	{
		return 69;
	}

	@Override
	public @Nullable IDrawable getIcon()
	{
		return icon;
	}

	@Override
	public RecipeType<CutterRecipe> getRecipeType()
	{
		return WitherPluginJEI.CUTTER;
	}

	@Override
	public Component getTitle()
	{
		return Component.translatable(WUTItems.CUTTER.get().getDescriptionId());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, CutterRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(INPUT, 13, 26).addIngredients(recipe.input());
		builder.addSlot(OUTPUT, 139, 26).addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));
	}

	@Override
	public void draw(CutterRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gg, double mouseX, double mouseY)
	{
	    var font = Minecraft.getInstance().font;
	    gg.drawString(font, "Cutting from", 54, 12, FONT, false);
	}
}
