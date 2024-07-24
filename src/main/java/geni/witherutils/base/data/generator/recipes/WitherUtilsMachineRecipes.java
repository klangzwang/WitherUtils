package geni.witherutils.base.data.generator.recipes;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;

public class WitherUtilsMachineRecipes implements DataProvider {
	
    private final String modid;
    private final List<DataProvider> subProviders = new ArrayList<>();

    public WitherUtilsMachineRecipes(String modid)
    {
        this.modid = modid;
    }

    public void addSubProvider(boolean include, DataProvider provider)
    {
        if (include)
            subProviders.add(provider);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput pOutput)
    {
        List<CompletableFuture<?>> list = new ArrayList<>();
        for (DataProvider provider : subProviders)
        {
            list.add(provider.run(pOutput));
        }
        return CompletableFuture.allOf(list.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName()
    {
        return "Wither Utils Data (" + modid + ")";
    }
}
