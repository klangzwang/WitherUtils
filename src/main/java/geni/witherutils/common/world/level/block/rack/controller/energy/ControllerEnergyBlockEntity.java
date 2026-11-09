package geni.witherutils.common.world.level.block.rack.controller.energy;

import java.text.DecimalFormat;

import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.init.WUTBlockEntities;
import geni.withercore.common.network.dataslot.NetworkDataSlot;
import geni.withercore.common.registration.impl.TileEntityTypeRegistryObject;
import geni.withercore.common.util.FacingUtil;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlock;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import geni.withercore.common.inventory.blockentity.energy.MachineEnergyStorage;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ControllerEnergyBlockEntity extends WitherMachineBlockEntity {

    public static final int DEFAULT_ENERGY_CAPACITY = 24_000_000;
    public static final int DEFAULT_MAX_TRANSFER = 6_400;

    public static final int ENERGY_CAPACITY = DEFAULT_ENERGY_CAPACITY;
    public static final int MAX_TRANSFER = DEFAULT_MAX_TRANSFER;

    private final RackEnergyStorage energyStorage;

    public ControllerEnergyBlockEntity(BlockPos pos, BlockState state)
    {
        this(resolveBlockEntityType(), pos, state);
    }

    public ControllerEnergyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(WUTBlockEntities.RACKENERGY_CONTROLLER, pos, state);
        this.energyStorage = new RackEnergyStorage(ENERGY_CAPACITY, MAX_TRANSFER);
        super.energyStorage = this.energyStorage;
        addDataSlot(NetworkDataSlot.INT.create(this::getEnergyStored, this::setEnergyStored));
    }

    public ControllerEnergyBlockEntity(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
        this.energyStorage = new RackEnergyStorage(ENERGY_CAPACITY, MAX_TRANSFER);
        super.energyStorage = this.energyStorage;
        addDataSlot(NetworkDataSlot.INT.create(this::getEnergyStored, this::setEnergyStored));
    }

    private static BlockEntityType<?> resolveBlockEntityType()
    {
        try
        {
            var field = geni.withercore.init.WUTBlockEntities.class.getField("RACKENERGY_CONTROLLER");
            Object obj = field.get(null);
            if (obj instanceof TileEntityTypeRegistryObject<?> regObj)
            {
                return regObj.get();
            }
        }
        catch (Throwable ignored)
        {
        }
        return null;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();
        this.energyStorage.setCanTransfer(isFormed());
    }

    @Override
    public void clientTick()
    {
        super.clientTick();
        setLitProperty(getEnergyStored() > 0);
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
    public boolean hasEnergyCapability()
    {
        return true;
    }

    @Nullable
    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction dir)
    {
        return energyStorage;
    }

    public int getEnergyStored()
    {
        return energyStorage.getEnergyStored();
    }

    public void setEnergyStored(int energy)
    {
        energyStorage.setEnergy(energy);
        setChanged();
    }

    public int getMaxEnergyStored()
    {
        return energyStorage.getMaxEnergyStored();
    }

    public RackEnergyStorage getEnergyStorage()
    {
        return energyStorage;
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
        if (tag.contains("energy"))
        {
            setEnergyStored(tag.getInt("energy"));
        }
        else if (tag.contains("energystored"))
        {
            setEnergyStored(tag.getInt("energystored"));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", getEnergyStored());
        tag.putInt("energystored", getEnergyStored());
    }

    /*
     *
     * FORMAT AMOUNT
     *
     */
    private static final DecimalFormat FORMATTER_WITH_UNITS = new DecimalFormat("####0.#");
    private static final DecimalFormat FORMATTER_COMMAS = new DecimalFormat("#,###");

    public String getFormatedDisplayAmount()
    {
        int stored = getEnergyStored();
        if (stored == 0)
            return "0 RF";

        return getFormatedBigNumber(stored) + " RF";
    }

    public static String getFormatedBigNumber(int number)
    {
        if (number >= 1_000_000_000)
        {
            float numb = number / 1_000_000_000F;
            return FORMATTER_WITH_UNITS.format(numb) + "B";
        }
        else if (number >= 1_000_000)
        {
            float numb = number / 1_000_000F;
            return FORMATTER_WITH_UNITS.format(numb) + "M";
        }
        else if (number >= 1_000)
        {
            float numb = number / 1_000F;
            return FORMATTER_WITH_UNITS.format(numb) + "K";
        }
        return String.valueOf(number);
    }

    public static String getFormatedFullNumber(int number)
    {
        return FORMATTER_COMMAS.format(number);
    }

    /*
     *
     * CUSTOM ENERGY STORAGE
     *
     */
    public class RackEnergyStorage extends MachineEnergyStorage {

        private boolean canTransfer = true;

        public RackEnergyStorage(int capacity, int maxTransfer)
        {
            super(capacity, maxTransfer);
        }

        public void setCanTransfer(boolean canTransfer)
        {
            this.canTransfer = canTransfer;
        }

        public boolean canTransfer()
        {
            return canTransfer;
        }

        public void setEnergy(int energy)
        {
            this.energy = Math.max(0, Math.min(energy, capacity));
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate)
        {
            if (!canReceive() || !canTransfer)
                return 0;

            int received = super.receiveEnergy(maxReceive, simulate);
            if (!simulate && received > 0)
            {
                setChanged();
            }
            return received;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate)
        {
            if (!canExtract() || !canTransfer)
                return 0;

            int extracted = super.extractEnergy(maxExtract, simulate);
            if (!simulate && extracted > 0)
            {
                setChanged();
            }
            return extracted;
        }
    }
}
