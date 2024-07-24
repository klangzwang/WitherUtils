package geni.witherutils.core.common.helper;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

public class ModelRegistryHelper {

    private final List<Pair<ModelResourceLocation, BakedModel>> registerModels = new LinkedList<>();
    private final List<IModelBakeCallbackPre> modelBakePreCallbacks = new LinkedList<>();
    private final List<IModelBakeCallback> modelBakeCallbacks = new LinkedList<>();

    public ModelRegistryHelper(IEventBus eventBus)
    {
        eventBus.register(this);
    }

    public ModelRegistryHelper()
    {
//        FMLJavaModLoadingContext.get().getModEventBus().register(this);
    }

    public void registerPreBakeCallback(IModelBakeCallbackPre callback)
    {
        modelBakePreCallbacks.add(callback);
    }

    public void registerCallback(IModelBakeCallback callback)
    {
        modelBakeCallbacks.add(callback);
    }

    public void register(ModelResourceLocation location, BakedModel model)
    {
        registerModels.add(new ImmutablePair<>(location, model));
    }

    @SubscribeEvent
    public void onModelBake(ModelEvent.BakingCompleted event)
    {
        for (IModelBakeCallbackPre callback : modelBakePreCallbacks)
        {
            callback.onModelBakePre(event);
        }
        for (Pair<ModelResourceLocation, BakedModel> pair : registerModels)
        {
            event.getModels().put(pair.getKey(), pair.getValue());
        }
        for (IModelBakeCallback callback : modelBakeCallbacks)
        {
            callback.onModelBake(event);
        }
    }

    public interface IModelBakeCallbackPre
    {
        void onModelBakePre(ModelEvent.BakingCompleted event);
    }

    public interface IModelBakeCallback
    {
        void onModelBake(ModelEvent.BakingCompleted event);
    }
}
