package geni.witherutils.core.common.registration.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import geni.witherutils.api.providers.IItemProvider;
import geni.witherutils.core.common.registration.WrappedDeferredRegister;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.ForgeRegistries;

public class UpgradeDeferredRegister extends WrappedDeferredRegister<Item> {

    private final List<IItemProvider> allItems = new ArrayList<>();

    public UpgradeDeferredRegister(String modid) {
        super(modid, ForgeRegistries.ITEMS);
    }

    public static Item.Properties getBaseProperties() {
        return new Item.Properties();
    }

    public UpgradeRegistryObject<Item> register(String name) {
        return register(name, Item::new);
    }

    public UpgradeRegistryObject<Item> registerUnburnable(String name) {
        return registerUnburnable(name, Item::new);
    }

    public UpgradeRegistryObject<Item> register(String name, Rarity rarity) {
        return register(name, properties -> new Item(properties.rarity(rarity)));
    }

    public <ITEM extends Item> UpgradeRegistryObject<ITEM> register(String name, Function<Item.Properties, ITEM> sup) {
        return register(name, () -> sup.apply(getBaseProperties()));
    }

    public <ITEM extends Item> UpgradeRegistryObject<ITEM> registerUnburnable(String name, Function<Item.Properties, ITEM> sup) {
        return register(name, () -> sup.apply(getBaseProperties().fireResistant()));
    }

    public <ITEM extends Item> UpgradeRegistryObject<ITEM> register(String name, Supplier<? extends ITEM> sup) {
        UpgradeRegistryObject<ITEM> registeredItem = register(name, sup, UpgradeRegistryObject::new);
        allItems.add(registeredItem);
        return registeredItem;
    }

    public List<IItemProvider> getAllItems()
    {
        return Collections.unmodifiableList(allItems);
    }
}