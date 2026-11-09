package geni.witherutils.common.world.level.block.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.common.inventory.blockentity.item.MachineInventory;
import geni.withercore.common.network.dataslot.NetworkDataSlot;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

import geni.withercore.common.helper.Ticker;
import geni.withercore.common.inventory.blockentity.item.MachineInventoryLayout;
import geni.withercore.common.inventory.blockentity.item.SingleSlotAccess;
import geni.withercore.common.util.BlockEntityUtil;
import geni.withercore.init.WUTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

public class CreativeGeneratorBlockEntity extends WitherMachineBlockEntity {

	private final EnergyStorage energyOut = new EnergyStorage(Integer.MAX_VALUE, 3200);
	private final EnergyStorage energyTrash = new EnergyStorage(Integer.MAX_VALUE, 3200);
	
	private final FluidTank TANKOUT = new FluidTank(Integer.MAX_VALUE);
	private final FluidTank TANKTRASH = new FluidTank(4000);
	
	private final ItemStackHandler ItemTrash = new ItemStackHandler(1);
    public static final SingleSlotAccess OUTPUT = new SingleSlotAccess();

    List<ItemStack> outStacks = new ArrayList<>();

    public final Ticker ticker = new Ticker(30);

    public static final SingleSlotAccess INPUT = new SingleSlotAccess();

	public CreativeGeneratorBlockEntity(BlockPos pos, BlockState state)
	{
		super(WUTBlockEntities.CREATIVE_GENERATOR, pos, state);
        addDataSlot(NetworkDataSlot.ITEM_STACK.create(() -> INPUT.getItemStack(this), f -> INPUT.setStackInSlot(this, f)));
	}

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider)
    {
        super.loadAdditional(tag, lookupProvider);
        this.getInventory().deserializeNBT(lookupProvider, tag.getCompound("Items"));
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider lookupProvider)
    {
        super.saveAdditional(tag, lookupProvider);
        tag.put("Items", getInventory().serializeNBT(lookupProvider));
    }

	/*
	 * 
	 * ITEMS
	 * 
	 */
    @Override
    public MachineInventoryLayout getInventoryLayout()
    {
        return MachineInventoryLayout
        		.builder()
        		.outputSlot()
        		.slotAccess(OUTPUT)
        		.setStackLimit(1)
                .inputSlot()
                .slotAccess(INPUT)
                .setStackLimit(1)
        		.build();
    }
    @Override
    protected MachineInventory createMachineInventory(MachineInventoryLayout layout)
    {
        return new MachineInventory(layout)
        {
            protected void onContentsChanged(int slot)
            {
                super.onContentsChanged(slot);
                if (level == null)
                    return;
                setChanged();
            }

            @Override
            public int getSlotLimit(int slot)
            {
                return 1;
            }
        };
    }
    @Override
    public boolean hasItemCapability()
    {
        return true;
    }
    @Override
    public IItemHandler getItemHandler(Direction dir)
    {
    	if(dir == Direction.EAST)
    		return ItemTrash;
    	else if(dir == Direction.NORTH)
    		return getInventory();
    	else
    		return null;
    }

    /*
     * 
     * ENERGY
     * 
     */
    @Override
    public boolean hasEnergyCapability()
    {
        return true;
    }
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction dir)
    {
    	if(dir == Direction.EAST)
    		return energyTrash;
    	else if(dir == Direction.WEST)
    		return energyOut;
    	else
    		return null;
    }
    
    /*
     * 
     * FLUID
     * 
     */
    @Override
    public boolean hasFluidCapability()
    {
        return true;
    }
	@Override
	public IFluidHandler getFluidHandler(@Nullable Direction dir)
	{
    	if(dir == Direction.EAST)
    		return TANKTRASH;
    	else if(dir == Direction.SOUTH)
    		return TANKOUT;
    	else
    		return null;
	}
	
    @Override
    public void serverTick()
    {
        super.serverTick();

        if (!inventory.getStackInSlot(0).isEmpty() && energyOut.getEnergyStored() > 0) {
            Optional<IEnergyStorage> capability = Optional.ofNullable(inventory.getStackInSlot(0).getCapability(Capabilities.EnergyStorage.ITEM, null));
            int received = (capability.<Integer>map(cap -> Integer.valueOf(cap.receiveEnergy(energyOut.getEnergyStored(), false))).orElse(Integer.valueOf(0))).intValue();
            //boolean canTakeUp = (capability.<Boolean>map(cap -> Boolean.valueOf((cap.getEnergyStored() >= cap.getMaxEnergyStored()))).orElse(Boolean.valueOf(false))).booleanValue();
            if (received > 0)
                energyOut.extractEnergy(received, false);
        }

        for(var facing : Direction.values())
        {
            IEnergyStorage energy = BlockEntityUtil.getCapability(level, Capabilities.EnergyStorage.BLOCK, getBlockPos().relative(facing), facing);
            if (energy != null)
            {

            }
        }

        if (getNeighbouringCapability(Capabilities.ItemHandler.BLOCK, Direction.NORTH) != null && OUTPUT.getItemStack(getInventory()).isEmpty())
        {
        	if(outStacks.isEmpty())
        	{
        		BuiltInRegistries.ITEM.entrySet().forEach(items -> outStacks.add(items.getValue().getDefaultInstance()));
        		BuiltInRegistries.BLOCK.entrySet().forEach(blocks -> outStacks.add(blocks.getValue().asItem().getDefaultInstance()));
        		Collections.shuffle(outStacks);
        	}
        	else
        	{
            	getInventory().setStackInSlot(0, outStacks.get(level.random.nextInt(outStacks.size())));
        	}
        }
        if (!ItemTrash.getStackInSlot(0).isEmpty())
        {
        	ItemTrash.getStackInSlot(0).shrink(64);
        }

        if (TANKOUT.getFluidAmount() != Integer.MAX_VALUE)
        {
        	TANKOUT.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        }
        if (TANKTRASH.getFluidAmount() > 1000)
        {
        	TANKTRASH.setFluid(new FluidStack(Fluids.WATER, 0));
        }

        if (energyOut.getEnergyStored() != Integer.MAX_VALUE)
        {
        	energyOut.receiveEnergy(1000, false);
        }
        if (energyTrash.getEnergyStored() > 0)
        {
            energyTrash.extractEnergy(Integer.MAX_VALUE, false);
        }
    }
}
