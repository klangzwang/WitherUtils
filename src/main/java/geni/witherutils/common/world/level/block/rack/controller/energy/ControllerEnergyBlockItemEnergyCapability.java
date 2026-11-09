package geni.witherutils.common.world.level.block.rack.controller.energy;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ControllerEnergyBlockItemEnergyCapability implements IEnergyStorage {

    private final ItemStack container;
    private final int capacity;
    private final int maxTransfer;

    public ControllerEnergyBlockItemEnergyCapability(ItemStack container, int capacity, int maxTransfer)
    {
        this.container = container;
        this.capacity = capacity;
        this.maxTransfer = maxTransfer;
    }

    public ItemStack getContainer()
    {
        return container;
    }

    @Override
    public int getEnergyStored()
    {
        CustomData customData = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("BlockEntityTag", 10))
        {
            tag = tag.getCompound("BlockEntityTag");
        }
        if (tag.contains("energy"))
        {
            return tag.getInt("energy");
        }
        if (tag.contains("energystored"))
        {
            return tag.getInt("energystored");
        }
        return 0;
    }

    public void setEnergyStored(int energy)
    {
        int clamped = Math.max(0, Math.min(energy, capacity));
        CustomData.update(DataComponents.CUSTOM_DATA, this.container, tag -> {
            CompoundTag beTag = tag.contains("BlockEntityTag", 10) ? tag.getCompound("BlockEntityTag") : new CompoundTag();
            beTag.putInt("energy", clamped);
            beTag.putInt("energystored", clamped);
            tag.put("BlockEntityTag", beTag);
        });
    }

    @Override
    public int getMaxEnergyStored()
    {
        return capacity;
    }

    @Override
    public boolean canReceive()
    {
        return maxTransfer > 0;
    }

    @Override
    public boolean canExtract()
    {
        return maxTransfer > 0;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate)
    {
        if (!canReceive() || maxReceive <= 0 || container.getCount() != 1)
        {
            return 0;
        }

        int current = getEnergyStored();
        int energyReceived = Math.min(capacity - current, Math.min(this.maxTransfer, maxReceive));
        if (!simulate && energyReceived > 0)
        {
            setEnergyStored(current + energyReceived);
        }
        return energyReceived;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate)
    {
        if (!canExtract() || maxExtract <= 0 || container.getCount() != 1)
        {
            return 0;
        }

        int current = getEnergyStored();
        int energyExtracted = Math.min(current, Math.min(this.maxTransfer, maxExtract));
        if (!simulate && energyExtracted > 0)
        {
            setEnergyStored(current - energyExtracted);
        }
        return energyExtracted;
    }
}
