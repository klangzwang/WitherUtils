package geni.witherdata.recipes;

import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTSounds;
import geni.witherutils.WitherUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class CutterRecipeProvider extends RecipeProvider {

    public CutterRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries)
    {
        super(packOutput, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput)
    {
        for (var entry : WUTBlocks.CTM_CONCRETE.entrySet()) {
            var blockObj = entry.getValue();
            build(blockObj.asItem(), Ingredient.of(Blocks.GRAY_CONCRETE), 0, WUTSounds.HAMMERHIT.get(), 1, recipeOutput);
        }
        for (var entry : WUTBlocks.CTM_DIRT.entrySet()) {
            var blockObj = entry.getValue();
            build(blockObj.asItem(), Ingredient.of(Blocks.DIRT), 0, SoundEvents.GRASS_HIT, 1, recipeOutput);
        }
        for (var entry : WUTBlocks.CTM_GLASS.entrySet()) {
            var blockObj = entry.getValue();
            build(blockObj.asItem(), Ingredient.of(Blocks.GLASS), 0, SoundEvents.GLASS_HIT, 1, recipeOutput);
        }
        for (var entry : WUTBlocks.CTM_METAL.entrySet()) {
            var blockObj = entry.getValue();
            build(blockObj.asItem(), Ingredient.of(Blocks.IRON_BLOCK), 0, WUTSounds.HAMMERHIT.get(), 24, recipeOutput);
        }
        for (var entry : WUTBlocks.CTM_STONE.entrySet()) {
            var blockObj = entry.getValue();
            build(blockObj.asItem(), Ingredient.of(Blocks.STONE), 0, SoundEvents.STONE_PLACE, 1, recipeOutput);
        }
        for (var entry : WUTBlocks.CTM_WOOD.entrySet()) {
            var blockObj = entry.getValue();
            build(blockObj.asItem(), Ingredient.of(Blocks.OAK_PLANKS), 0, SoundEvents.WOOD_PLACE, 1, recipeOutput);
        }

        build(Blocks.ACACIA_PLANKS, Ingredient.of(ItemTags.ACACIA_LOGS), 0, SoundEvents.WOOD_HIT, 1, recipeOutput);
        build(WUTBlocks.BRICKSDARK.get().asItem(), Ingredient.of(Blocks.STONE_BRICKS), 0, SoundEvents.STONE_HIT, 1, recipeOutput);
    }

    protected void build(ItemLike output, Ingredient input, float damage, SoundEvent sound, int count, RecipeOutput recipeOutput)
    {
        build(output, input, damage, sound, count, "", recipeOutput);
    }

    protected void build(ItemLike output, Ingredient input, float damage, SoundEvent sound, int count, String suffix, RecipeOutput recipeOutput)
    {
        ItemStack outStack = output.asItem().getDefaultInstance();
        outStack.setCount(count);

        recipeOutput.accept(
            WitherUtils.loc("cutter/" + BuiltInRegistries.ITEM.getKey(output.asItem()).getPath() + suffix),
            new CutterRecipe(input, outStack, damage, sound),
            null);
    }
}
