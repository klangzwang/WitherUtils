package geni.witherutils.client.ponder;

import geni.withercore.init.WUTBlocks;
import geni.withercore.init.WUTItems;
import geni.witherutils.common.world.level.block.SolarPanelBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class WitherPonderRegistry {

    private WitherPonderRegistry() {
    }

    public static boolean hasPonder(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        return item instanceof SolarPanelBlockItem
                || item == WUTBlocks.SOLARBASIC.get().asItem()
                || item == WUTBlocks.SOLARADV.get().asItem()
                || item == WUTBlocks.SOLARULTRA.get().asItem()
                || item == WUTBlocks.SOLARCASE.get().asItem()
                || item == WUTBlocks.CREATIVE_GENERATOR.get().asItem()
                || item == WUTBlocks.ANVIL.get().asItem()
                || item == WUTItems.HAMMER.get()
                || item == WUTBlocks.WITHERSTEEL_BLOCK.get().asItem()
                || item == WUTItems.WITHERSTEEL_INGOT.get()
                || item == WUTItems.WITHERSTEEL_NUGGET.get()
                || item == WUTItems.WITHERSTEEL_PLATE.get()
                || item == WUTItems.WITHERSTEEL_GEAR.get()
                || item == WUTItems.WITHERSTEEL_ROD.get()
                || item == WUTItems.WITHERSTEEL_CASE.get()
                || item == WUTBlocks.ANGEL.get().asItem()
                || item == WUTBlocks.RACK_CASE.get().asItem()
                || item == WUTBlocks.RACK_TERMINAL.get().asItem()
                || item == WUTBlocks.RACKITEM_CONTROLLER.get().asItem()
                || item == WUTBlocks.RACKFLUID_CONTROLLER.get().asItem()
                || item == WUTBlocks.RACKENERGY_CONTROLLER.get().asItem()
                || item == WUTBlocks.FLOORSENSOR.get().asItem()
                || item == WUTBlocks.WALL_SENSOR.get().asItem()
                || item == WUTBlocks.GREENHOUSE.get().asItem()
                || item == WUTBlocks.STEEL_CATWALK.get().asItem()
                || item == WUTBlocks.STEEL_POLE.get().asItem()
                || item == WUTBlocks.STEEL_BEAM.get().asItem()
                || item == WUTBlocks.STEEL_RAILING.get().asItem()
                || item == WUTBlocks.BRICKSDARK.get().asItem()
                || item == WUTBlocks.BRICKSLAVA.get().asItem()
                || item == WUTBlocks.SLICEDCONCRETEBLACK.get().asItem()
                || item == WUTBlocks.SLICEDCONCRETEGRAY.get().asItem()
                || item == WUTBlocks.SLICEDCONCRETEWHITE.get().asItem()
                || item == WUTItems.CUTTER.get()
                || item == WUTItems.WRENCH.get()
                || item == WUTItems.SOULBANK_CASE.get()
                || item == WUTItems.SOULBANK_BASIC.get()
                || item == WUTItems.SOULBANK_ADVANCED.get()
                || item == WUTItems.SOULBANK_ULTRA.get();
    }

    public static void openPonderScreen(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || !hasPonder(stack)) {
            return;
        }
        Item item = stack.getItem();

        if (item == WUTBlocks.ANVIL.get().asItem()
                || item == WUTItems.HAMMER.get()
                || item == WUTBlocks.WITHERSTEEL_BLOCK.get().asItem()
                || item == WUTItems.WITHERSTEEL_INGOT.get()
                || item == WUTItems.WITHERSTEEL_NUGGET.get()
                || item == WUTItems.WITHERSTEEL_PLATE.get()
                || item == WUTItems.WITHERSTEEL_GEAR.get()
                || item == WUTItems.WITHERSTEEL_ROD.get()
                || item == WUTItems.WITHERSTEEL_CASE.get()) {
            mc.setScreen(new AnvilPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.ANGEL.get().asItem()) {
            mc.setScreen(new AngelPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.RACK_CASE.get().asItem()
                || item == WUTBlocks.RACK_TERMINAL.get().asItem()
                || item == WUTBlocks.RACKITEM_CONTROLLER.get().asItem()
                || item == WUTBlocks.RACKFLUID_CONTROLLER.get().asItem()
                || item == WUTBlocks.RACKENERGY_CONTROLLER.get().asItem()) {
            mc.setScreen(new RackPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.FLOORSENSOR.get().asItem() || item == WUTBlocks.WALL_SENSOR.get().asItem()) {
            mc.setScreen(new SensorPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.GREENHOUSE.get().asItem()) {
            mc.setScreen(new GreenhousePonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.STEEL_CATWALK.get().asItem()
                || item == WUTBlocks.STEEL_POLE.get().asItem()
                || item == WUTBlocks.STEEL_BEAM.get().asItem()
                || item == WUTBlocks.STEEL_RAILING.get().asItem()) {
            mc.setScreen(new CatwalkPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.BRICKSDARK.get().asItem()
                || item == WUTBlocks.BRICKSLAVA.get().asItem()) {
            mc.setScreen(new BricksPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTBlocks.SLICEDCONCRETEBLACK.get().asItem()
                || item == WUTBlocks.SLICEDCONCRETEGRAY.get().asItem()
                || item == WUTBlocks.SLICEDCONCRETEWHITE.get().asItem()) {
            mc.setScreen(new SlicedConcretePonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTItems.WRENCH.get()) {
            mc.setScreen(new WrenchPonderScreen(stack.copy(), mc.screen));
        } else if (item == WUTItems.SOULBANK_CASE.get()
                || item == WUTItems.SOULBANK_BASIC.get()
                || item == WUTItems.SOULBANK_ADVANCED.get()
                || item == WUTItems.SOULBANK_ULTRA.get()) {
            mc.setScreen(new SoulBankPonderScreen(stack.copy(), mc.screen));
        } else {
            mc.setScreen(new WitherPonderScreen(stack.copy(), mc.screen));
        }
    }
}
