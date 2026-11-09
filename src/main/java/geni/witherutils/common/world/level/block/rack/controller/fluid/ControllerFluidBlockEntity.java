package geni.witherutils.common.world.level.block.rack.controller.fluid;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.init.WUTBlockEntities;
import geni.withercore.common.inventory.blockentity.fluid.SmartSyncTank;
import geni.withercore.common.network.dataslot.NetworkDataSlot;
import geni.withercore.common.registration.impl.TileEntityTypeRegistryObject;
import geni.withercore.common.util.FacingUtil;
import geni.witherutils.common.config.common.BlockConfig;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlock;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ControllerFluidBlockEntity extends WitherMachineBlockEntity {

    private static final HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    public static final int DEFAULT_FLUID_CAPACITY = 64000;
    public static final int FLUIDCAPACITY = DEFAULT_FLUID_CAPACITY;

    private final SmartSyncTank fluidTank;
    private int fluidStored;

    public ControllerFluidBlockEntity(BlockPos pos, BlockState state)
    {
        this(WUTBlockEntities.RACKFLUID_CONTROLLER, pos, state);
    }

    public ControllerFluidBlockEntity(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);

        int cap = BlockConfig.getFluidControllerCapacity();
        this.fluidTank = new SmartSyncTank(this, cap > 0 ? cap : FLUIDCAPACITY);

        addDataSlot(NetworkDataSlot.FLUID_STACK.create(this.fluidTank::getFluid, this.fluidTank::setFluid));
        addDataSlot(NetworkDataSlot.INT.create(this::getFluidStored, this::setFluidStored));
        this.fluidStored = 0;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();
        fluidTank.tick();
        this.fluidStored = fluidTank.getFluidAmount();
    }

    @Override
    public void clientTick()
    {
        super.clientTick();
        fluidTank.tick();
        setLitProperty(!fluidTank.isEmpty());
        this.fluidStored = fluidTank.getFluidAmount();
    }

    /*
     * 
     * FORMED
     * 
     */
    public boolean isFormed()
    {
        if (level == null)
            return false;

        for (BlockPos aroundPos : FacingUtil.AROUND_ALL)
        {
            if (!aroundPos.equals(BlockPos.ZERO))
            {
                BlockEntity te = level.getBlockEntity(worldPosition.offset(aroundPos));
                if (te instanceof CaseBlockEntity caseBe)
                {
                    return CaseBlock.isFormed(caseBe.getBlockState());
                }
            }
        }
        return false;
    }

    /*
     * 
     * CAPABILITY
     * 
     */
    @Override
    public boolean hasFluidCapability()
    {
        return true;
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side)
    {
        return fluidTank;
    }

    public FluidStack getDisplayStack()
    {
        return fluidTank.getFluid();
    }

    public int getFluidStored()
    {
        return fluidStored;
    }

    public void setFluidStored(int stored)
    {
        this.fluidStored = stored;
        setChanged();
    }

    public SmartSyncTank getFluidTank()
    {
        return fluidTank;
    }

    /*
     * 
     * LOAD / SAVE
     * 
     */
    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        if (tag.contains("fluid", 10))
        {
            this.fluidTank.readFromNBT(registries, tag.getCompound("fluid"));
        }
        this.fluidStored = this.fluidTank.getFluidAmount();
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.put("fluid", this.fluidTank.writeToNBT(registries));
    }

    /*
     * 
     * FORMAT AMOUNT
     * 
     */
    public String getFormatedDisplayAmount()
    {
        if (fluidStored == 0)
            return "0";

        if (fluidStored >= 1000)
        {
            String buckets = getFormatedBigNumber(fluidStored / 1000) + " x1000";
            int remainder = fluidStored % 1000;
            return remainder != 0 ? buckets + " + " + remainder : buckets;
        }
        return String.valueOf(fluidStored);
    }

    private static final DecimalFormat FORMATTER_WITH_UNITS = new DecimalFormat("####0.#");

    public static String getFormatedBigNumber(int number)
    {
        if (number >= 1000000000) {
            float numb = number / 1000000000F;
            return FORMATTER_WITH_UNITS.format(numb) + "B";
        } else if (number >= 1000000) {
            float numb = number / 1000000F;
            if (number > 100000000) numb = Math.round(numb);
            return FORMATTER_WITH_UNITS.format(numb) + "M";
        } else if (number >= 1000) {
            float numb = number / 1000F;
            if (number > 100000) numb = Math.round(numb);
            return FORMATTER_WITH_UNITS.format(numb) + "K";
        }
        return String.valueOf(number);
    }

    /*
     * 
     * INTERACTION
     * 
     */
    public boolean onRightClicked(Player player, InteractionHand hand)
    {
        if (level == null || level.isClientSide())
            return true;

        return FluidUtil.interactWithFluidHandler(player, hand, fluidTank);
    }
}
