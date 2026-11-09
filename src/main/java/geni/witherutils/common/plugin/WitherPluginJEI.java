package geni.witherutils.common.plugin;

import java.util.ArrayList;
import java.util.List;

import geni.witherapi.client.gui.IHideJeiOverlay;
import geni.withercore.client.gui.WitherAbstractScreen;
import geni.witherdata.recipes.CutterRecipe;
import geni.witherutils.WitherUtils;
import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.withercore.init.WUTRecipes;
import geni.witherdata.recipes.AnvilRecipe;
import geni.witherutils.client.gui.screen.CutterScreen;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.Blocks;

@JeiPlugin
public class WitherPluginJEI implements IModPlugin {

	private static final ResourceLocation ID = WitherUtils.loc("jei");

	public static final RecipeType<AnvilRecipe> ANVIL = RecipeType.create(WitherUtils.MODID, "anvil", AnvilRecipe.class);
    public static final RecipeType<CutterRecipe> CUTTER = RecipeType.create(WitherUtils.MODID, "cutter", CutterRecipe.class);

    public static final String TRANSLATION_LINE_BREAK = "\\n";
    
	@Override
	public ResourceLocation getPluginUid()
	{
		return ID;
	}
	
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {
        IJeiHelpers helpers = registry.getJeiHelpers();

        registry.addRecipeCategories(new AnvilRecipeCategory(helpers.getGuiHelper()));
    	registry.addRecipeCategories(new HammerRecipeCategory(helpers.getGuiHelper()));
        registry.addRecipeCategories(new CutterRecipeCategory(helpers.getGuiHelper()));
    }
    
	@Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
    	registration.addRecipeCatalyst(new ItemStack(WUTBlocks.ANVIL.get()), ANVIL);
    	registration.addRecipeCatalyst(new ItemStack(WUTItems.HAMMER.get()), ANVIL);
    	registration.addRecipeCatalyst(new ItemStack(WUTItems.HAMMER.get()), HammerRecipeCategory.TYPE);
    	registration.addRecipeCatalyst(new ItemStack(Blocks.ANVIL), HammerRecipeCategory.TYPE);
        registration.addRecipeCatalyst(new ItemStack(WUTItems.CUTTER.get()), CutterRecipeCategory.TYPE);
    }
	
	@Override
    public void registerRecipes(IRecipeRegistration registry)
    {
        List<AnvilRecipe> anvilRecipes = new ArrayList<>();
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.level.getRecipeManager() != null) {
                anvilRecipes.addAll(minecraft.level.getRecipeManager().getAllRecipesFor(WUTRecipes.ANVIL.type().get()).stream().map(RecipeHolder::value).toList());
            } else if (minecraft.getConnection() != null && minecraft.getConnection().getRecipeManager() != null) {
                anvilRecipes.addAll(minecraft.getConnection().getRecipeManager().getAllRecipesFor(WUTRecipes.ANVIL.type().get()).stream().map(RecipeHolder::value).toList());
            }
        } catch (Exception ignored) {
        }
        if (anvilRecipes.isEmpty()) {
            anvilRecipes.addAll(AnvilRecipeCategory.getDefaultRecipes());
        }
        registry.addRecipes(ANVIL, anvilRecipes);
    	registry.addRecipes(HammerRecipeCategory.TYPE, HammerRecipeCategory.getRecipes());

        //

        List<CutterRecipe> cutterRecipes = new ArrayList<>();
        try {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.level != null && minecraft.level.getRecipeManager() != null) {
                cutterRecipes.addAll(minecraft.level.getRecipeManager()
                        .getAllRecipesFor(WUTRecipes.CUTTER.type().get())
                        .stream()
                        .map(RecipeHolder::value)
                        .toList());
            } else if (minecraft.getConnection() != null && minecraft.getConnection().getRecipeManager() != null) {
                cutterRecipes.addAll(minecraft.getConnection().getRecipeManager()
                        .getAllRecipesFor(WUTRecipes.CUTTER.type().get())
                        .stream()
                        .map(RecipeHolder::value)
                        .toList());
            }
        } catch (Exception ignored) {}

        registry.addRecipes(CUTTER, cutterRecipes);

        //

        registry.addIngredientInfo(
                new ItemStack(WUTItems.HAMMER.get()),
                VanillaTypes.ITEM_STACK,
                Component.translatable("jei.witherutils.hammer.info")
        );
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGuiContainerHandler(CutterScreen.class, new IGuiContainerHandler<CutterScreen>() {
            @Override
            public List<Rect2i> getGuiExtraAreas(CutterScreen containerScreen) {
                if (containerScreen instanceof IHideJeiOverlay) {
                    return List.of(new Rect2i(0, 0, containerScreen.width, containerScreen.height));
                }
                return List.of();
            }
        });
    }
}
