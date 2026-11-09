package geni.witherutils.common.world.level.block.rack.controller.item;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.items.IItemHandler;

public class ControllerItemBlockItemHandlerCapability implements IItemHandler {
	
    private final ItemStack container;
    private final int slotLimit;
    @Nullable
    private final HolderLookup.Provider registries;

    public ControllerItemBlockItemHandlerCapability(ItemStack container, int slotLimit)
    {
        this(container, slotLimit, null);
    }

    public ControllerItemBlockItemHandlerCapability(ItemStack container, int slotLimit, @Nullable HolderLookup.Provider registries)
    {
        this.container = container;
        this.slotLimit = slotLimit;
        this.registries = registries;
    }

    @Override
    public int getSlots()
    {
        return 1;
    }

    public ItemStack getContainer()
    {
        return container;
    }

    public ItemStack getStack()
    {
        return getStack(this.registries);
    }

    public ItemStack getStack(@Nullable HolderLookup.Provider lookupProvider)
    {
        CustomData customData = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("BlockEntityTag", 10))
        {
            tag = tag.getCompound("BlockEntityTag");
        }
        if (tag.contains("blstack", 10))
        {
            if (lookupProvider != null)
            {
                return ItemStack.parseOptional(lookupProvider, tag.getCompound("blstack"));
            }
        }
        else if (tag.contains("blStack", 10))
        {
            if (lookupProvider != null)
            {
                return ItemStack.parseOptional(lookupProvider, tag.getCompound("blStack"));
            }
        }
        return ItemStack.EMPTY;
    }

    public void setStack(@Nullable HolderLookup.Provider lookupProvider, ItemStack stack)
    {
        CustomData.update(DataComponents.CUSTOM_DATA, this.container, tag -> {
            CompoundTag beTag = tag.contains("BlockEntityTag", 10) ? tag.getCompound("BlockEntityTag") : new CompoundTag();
            if (stack.isEmpty())
            {
                beTag.remove("blstack");
                beTag.remove("blStack");
            }
            else if (lookupProvider != null)
            {
                beTag.put("blstack", stack.saveOptional(lookupProvider));
                beTag.put("blStack", stack.saveOptional(lookupProvider));
            }
            tag.put("BlockEntityTag", beTag);
        });
    }

    public int getStored()
    {
        CustomData customData = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("BlockEntityTag", 10))
        {
            tag = tag.getCompound("BlockEntityTag");
        }
        if (tag.contains("stored"))
        {
            return tag.getInt("stored");
        }
        if (tag.contains("itemsstored"))
        {
            return tag.getInt("itemsstored");
        }
        return 0;
    }

    public void setStored(int stored)
    {
        CustomData.update(DataComponents.CUSTOM_DATA, this.container, tag -> {
            CompoundTag beTag = tag.contains("BlockEntityTag", 10) ? tag.getCompound("BlockEntityTag") : new CompoundTag();
            beTag.putInt("stored", stored);
            beTag.putInt("itemsstored", stored);
            tag.put("BlockEntityTag", beTag);
        });
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int slot)
    {
        if (slot != 0)
        {
            return ItemStack.EMPTY;
        }
        ItemStack stack = getStack();
        int stored = getStored();
        if (stack.isEmpty() || stored <= 0)
        {
            return ItemStack.EMPTY;
        }
        ItemStack copy = stack.copy();
        copy.setCount(Math.min(copy.getMaxStackSize(), stored));
        return copy;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
    {
        if (slot != 0 || stack.isEmpty() || !isItemValid(slot, stack))
        {
            return stack;
        }

        int currentStored = getStored();
        int availableSpace = slotLimit - currentStored;
        if (availableSpace <= 0)
        {
            return stack;
        }

        int toInsert = Math.min(availableSpace, stack.getCount());
        if (!simulate)
        {
            ItemStack currentStack = getStack();
            if (currentStack.isEmpty())
            {
                setStack(registries, stack.copyWithCount(1));
            }
            setStored(currentStored + toInsert);
        }

        if (toInsert >= stack.getCount())
        {
            return ItemStack.EMPTY;
        }
        return stack.copyWithCount(stack.getCount() - toInsert);
    }

    @Nonnull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate)
    {
        if (slot != 0 || amount <= 0)
        {
            return ItemStack.EMPTY;
        }

        int currentStored = getStored();
        ItemStack currentStack = getStack();
        if (currentStored <= 0 || currentStack.isEmpty())
        {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(Math.min(currentStored, amount), currentStack.getMaxStackSize());
        ItemStack result = currentStack.copyWithCount(toExtract);

        if (!simulate)
        {
            int newStored = currentStored - toExtract;
            setStored(newStored);
            if (newStored <= 0)
            {
                setStack(registries, ItemStack.EMPTY);
            }
        }
        return result;
    }

    @Override
    public int getSlotLimit(int slot)
    {
        return slotLimit;
    }

    @Override
    public boolean isItemValid(int slot, @Nonnull ItemStack stack)
    {
        if (slot != 0 || stack.isEmpty())
        {
            return false;
        }
        ItemStack currentStack = getStack();
        if (currentStack.isEmpty() || getStored() == 0)
        {
            return true;
        }
        return ItemStack.isSameItemSameComponents(currentStack, stack);
    }
}
