package geni.witherutils.common.world.level.block.rack.controller.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

public class ControllerFluidBlockItemHandlerCapability implements IFluidHandlerItem {

    private final ItemStack container;
    private final int capacity;
    @Nullable
    private final HolderLookup.Provider registries;

    public ControllerFluidBlockItemHandlerCapability(ItemStack container, int capacity)
    {
        this(container, capacity, null);
    }

    public ControllerFluidBlockItemHandlerCapability(ItemStack container, int capacity, @Nullable HolderLookup.Provider registries)
    {
        this.container = container;
        this.capacity = capacity;
        this.registries = registries;
    }

    @Override
    public ItemStack getContainer()
    {
        return container;
    }

    public FluidStack getFluid()
    {
        return getFluid(this.registries);
    }

    public FluidStack getFluid(@Nullable HolderLookup.Provider lookupProvider)
    {
        CustomData customData = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        CompoundTag tag = customData.copyTag();
        if (tag.contains("BlockEntityTag", 10))
        {
            tag = tag.getCompound("BlockEntityTag");
        }
        if (tag.contains("fluid", 10))
        {
            if (lookupProvider != null)
            {
                return FluidStack.parseOptional(lookupProvider, tag.getCompound("fluid"));
            }
        }
        return FluidStack.EMPTY;
    }

    public void setFluid(@Nullable HolderLookup.Provider lookupProvider, FluidStack stack)
    {
        CustomData.update(DataComponents.CUSTOM_DATA, container, tag -> {
            CompoundTag beTag = tag.contains("BlockEntityTag", 10) ? tag.getCompound("BlockEntityTag") : new CompoundTag();
            if (stack.isEmpty())
            {
                beTag.remove("fluid");
            }
            else if (lookupProvider != null)
            {
                beTag.put("fluid", stack.saveOptional(lookupProvider));
            }
            tag.put("BlockEntityTag", beTag);
        });
    }

    @Override
    public int getTanks()
    {
        return 1;
    }

    @Nonnull
    @Override
    public FluidStack getFluidInTank(int tank)
    {
        return tank == 0 ? getFluid() : FluidStack.EMPTY;
    }

    @Override
    public int getTankCapacity(int tank)
    {
        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
    {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        if (container.getCount() != 1 || resource.isEmpty() || !isFluidValid(0, resource))
        {
            return 0;
        }

        FluidStack current = getFluid();
        if (current.isEmpty())
        {
            int fillAmount = Math.min(capacity, resource.getAmount());
            if (action.execute())
            {
                FluidStack result = resource.copyWithAmount(fillAmount);
                setFluid(registries, result);
            }
            return fillAmount;
        }

        if (!FluidStack.isSameFluidSameComponents(current, resource))
        {
            return 0;
        }

        int available = capacity - current.getAmount();
        int fillAmount = Math.min(available, resource.getAmount());
        if (fillAmount > 0 && action.execute())
        {
            FluidStack result = current.copyWithAmount(current.getAmount() + fillAmount);
            setFluid(registries, result);
        }
        return fillAmount;
    }

    @Nonnull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        if (container.getCount() != 1 || resource.isEmpty())
        {
            return FluidStack.EMPTY;
        }

        FluidStack current = getFluid();
        if (current.isEmpty() || !FluidStack.isSameFluidSameComponents(current, resource))
        {
            return FluidStack.EMPTY;
        }

        return drain(resource.getAmount(), action);
    }

    @Nonnull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        if (container.getCount() != 1 || maxDrain <= 0)
        {
            return FluidStack.EMPTY;
        }

        FluidStack current = getFluid();
        if (current.isEmpty())
        {
            return FluidStack.EMPTY;
        }

        int drainAmount = Math.min(current.getAmount(), maxDrain);
        FluidStack result = current.copyWithAmount(drainAmount);

        if (action.execute())
        {
            int newAmount = current.getAmount() - drainAmount;
            if (newAmount <= 0)
            {
                setFluid(registries, FluidStack.EMPTY);
            }
            else
            {
                setFluid(registries, current.copyWithAmount(newAmount));
            }
        }
        return result;
    }
}
