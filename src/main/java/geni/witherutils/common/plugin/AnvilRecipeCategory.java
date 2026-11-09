package geni.witherutils.common.plugin;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import geni.witherutils.WitherUtils;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.withercore.init.WUTSounds;
import geni.witherdata.recipes.AnvilRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

public class AnvilRecipeCategory implements IRecipeCategory<AnvilRecipe> {
	
	@SuppressWarnings("unused")
	private static final int FONT = 4210752;
    private final IDrawableStatic gui;
    private final IDrawable icon;
    
	public AnvilRecipeCategory(IGuiHelper guiHelper)
	{
		gui = guiHelper.drawableBuilder(WitherUtils.loc("textures/jei/anvil_recipe.png"), 0, 0, 169, 69).setTextureSize(169, 69).build();
		icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(WUTBlocks.ANVIL.get()));
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
	public RecipeType<AnvilRecipe> getRecipeType()
	{
		return WitherPluginJEI.ANVIL;
	}

	@Override
	public Component getTitle()
	{
		return Component.translatable(WUTBlocks.ANVIL.get().getDescriptionId());
	}

	@Override
	public void setRecipe(IRecipeLayoutBuilder builder, AnvilRecipe recipe, IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 90, 5).addIngredients(recipe.input());
		builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 43).addItemStack(recipe.output());
	}
	
	@Override
	public void draw(AnvilRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics gg, double mouseX, double mouseY)
	{
		gui.draw(gg, 0, 0);
	    var font = Minecraft.getInstance().font;
	    gg.drawString(font, recipe.hitcounter() + " Hits needed", 90, 30, Color.BLACK.getRGB(), false);
	    gg.drawString(font, "x" + recipe.count(), 110, 47, Color.BLACK.getRGB(), false);
	    if(recipe.bonus() > 0)
	    gg.drawString(font, "Bonus: x" + recipe.bonus(), 90, 63, Color.BLACK.getRGB(), false);
	}

	public static List<AnvilRecipe> getDefaultRecipes()
	{
		List<AnvilRecipe> list = new ArrayList<>();
		list.add(new AnvilRecipe(Ingredient.of(Items.BONE), new ItemStack(Items.BONE_MEAL, 3), 3, SoundEvents.BONE_BLOCK_BREAK, 3, 2));
		list.add(new AnvilRecipe(Ingredient.of(Items.IRON_INGOT), new ItemStack(WUTItems.WITHERSTEEL_NUGGET.get(), 3), 3, WUTSounds.HAMMERHIT.get(), 4, 0));
		list.add(new AnvilRecipe(Ingredient.of(WUTItems.WITHERSTEEL_INGOT.get()), new ItemStack(WUTItems.WITHERSTEEL_PLATE.get(), 1), 6, WUTSounds.HAMMERHIT.get(), 1, 0));
		list.add(new AnvilRecipe(Ingredient.of(WUTItems.WITHERSTEEL_PLATE.get()), new ItemStack(WUTItems.WITHERSTEEL_GEAR.get(), 1), 6, WUTSounds.HAMMERHIT.get(), 1, 0));
		list.add(new AnvilRecipe(Ingredient.of(WUTItems.WITHERSTEEL_NUGGET.get()), new ItemStack(WUTItems.WITHERSTEEL_ROD.get(), 1), 3, WUTSounds.HAMMERHIT.get(), 1, 0));
		list.add(new AnvilRecipe(Ingredient.of(Items.BLAZE_ROD), new ItemStack(Items.BLAZE_POWDER, 2), 2, WUTSounds.HAMMERHIT.get(), 2, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.GLASS_BOTTLE), new ItemStack(Items.GLASS, 1), 2, WUTSounds.HAMMERHIT.get(), 2, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.CAULDRON), new ItemStack(Items.IRON_INGOT, 9), 6, WUTSounds.HAMMERHIT.get(), 6, 1));
		list.add(new AnvilRecipe(Ingredient.of(ItemTags.WOOL), new ItemStack(Items.STRING, 1), 4, WUTSounds.HAMMERHIT.get(), 4, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.NETHERITE_INGOT), new ItemStack(Items.NETHERITE_SCRAP, 9), 3, WUTSounds.HAMMERHIT.get(), 3, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.FURNACE), new ItemStack(Items.COBBLESTONE, 3), 8, WUTSounds.HAMMERHIT.get(), 8, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.CHEST), new ItemStack(Items.OAK_PLANKS, 3), 8, WUTSounds.HAMMERHIT.get(), 8, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.LADDER), new ItemStack(Items.STICK, 2), 7, WUTSounds.HAMMERHIT.get(), 7, 1));
		list.add(new AnvilRecipe(Ingredient.of(Items.GLOWSTONE), new ItemStack(Items.GLOWSTONE_DUST, 1), 4, WUTSounds.HAMMERHIT.get(), 4, 0));
		list.add(new AnvilRecipe(Ingredient.of(Items.CLAY), new ItemStack(Items.CLAY_BALL, 1), 4, WUTSounds.HAMMERHIT.get(), 4, 0));
		list.add(new AnvilRecipe(Ingredient.of(Items.STONE_BRICKS), new ItemStack(Items.STONE, 3), 4, WUTSounds.HAMMERHIT.get(), 4, 1));
		return list;
	}
}
