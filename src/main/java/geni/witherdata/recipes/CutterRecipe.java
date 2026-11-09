package geni.witherdata.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import geni.withercore.common.recipe.OutputStack;
import geni.withercore.common.recipe.WitherRecipe;
import geni.withercore.init.WUTRecipes;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

public class CutterRecipe implements WitherRecipe<RecipeInput> {
	
    private final Ingredient input;
    private final ItemStack output;
    private final float damage;
    private final SoundEvent sound;

    public CutterRecipe(Ingredient input, ItemStack output, float damage, SoundEvent sound)
    {
        this.input = input;
        this.output = output;
        this.damage = damage;
        this.sound = sound;
    }
	
	/*@Override
	public double getBaseSaturationCost() { return 0; }
	@Override
	public int getBaseEnergyCost() { return 0; }*/

	@Override
	public List<OutputStack> craft(RecipeInput container, RegistryAccess registryAccess) { return null; }
	@Override
	public List<OutputStack> getResultStacks(RegistryAccess registryAccess) { return null; }
	
	@Override
	public boolean matches(RecipeInput recipeInput, Level p_345375_)
	{
    	return input.test(recipeInput.getItem(0));
	}
	
	@Override
	public ItemStack assemble(RecipeInput p_345149_, Provider p_346030_)
	{
        return this.output.copy();
	}

	@Override
	public boolean canCraftInDimensions(int pWidth, int pHeight)
	{
        return true;
	}
	
	@Override
	public ItemStack getResultItem(Provider pRegistries)
	{
		return output;
	}
    
    public Ingredient input()
    {
        return input;
    }
    
    public ItemStack output()
    {
        return output;
    }

    public SoundEvent sound()
    {
        return sound;
    }

    public float damage()
    {
        return damage;
    }
    
    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return WUTRecipes.CUTTER.serializer().get();
    }
    
    @Override
    public RecipeType<?> getType()
    {
        return WUTRecipes.CUTTER.type().get();
    }

    public record RecipeIn(ItemStack input) implements RecipeInput
    {
        @Override
        public ItemStack getItem(int slotIndex)
        {
            return input;
        }

        @Override
        public int size()
        {
            return 1;
        }
    }

    public static class Serializer implements RecipeSerializer<CutterRecipe>
    {
        public static final MapCodec<CutterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
    		Ingredient.CODEC.fieldOf("input").forGetter(CutterRecipe::input),
    		ItemStack.CODEC.fieldOf("output").forGetter(CutterRecipe::output),
            Codec.FLOAT.fieldOf("damage").forGetter(CutterRecipe::damage),
            SoundEvent.DIRECT_CODEC.fieldOf("sound").forGetter(CutterRecipe::sound))
    		.apply(inst, CutterRecipe::new));
        
        public static final StreamCodec<RegistryFriendlyByteBuf, CutterRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, CutterRecipe::input,
            ItemStack.STREAM_CODEC, CutterRecipe::output,
            ByteBufCodecs.FLOAT, CutterRecipe::damage,
            SoundEvent.DIRECT_STREAM_CODEC, CutterRecipe::sound,
            CutterRecipe::new);

        @Override
        public MapCodec<CutterRecipe> codec()
        {
            return CODEC;
        }
        
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CutterRecipe> streamCodec()
        {
            return STREAM_CODEC;
        }
    }
}
