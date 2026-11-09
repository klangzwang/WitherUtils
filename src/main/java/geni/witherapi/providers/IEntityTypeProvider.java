package geni.witherapi.providers;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

@MethodsReturnNonnullByDefault
public interface IEntityTypeProvider extends IBaseProvider {

    EntityType<?> getEntityType();

    @Override
    default ResourceLocation getRegistryName() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(getEntityType());
    }
}


