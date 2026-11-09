package geni.witherutils.common.world.level.block.rack.terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.state.BlockStateProperties;
import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.common.network.dataslot.NetworkDataSlot;
import geni.witherutils.common.world.level.block.rack.MultiStructure;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlock;
import geni.witherutils.common.world.level.block.rack.casing.CaseBlockEntity;
import geni.witherutils.common.world.level.block.rack.controller.energy.ControllerEnergyBlockEntity;
import geni.witherutils.common.world.level.block.rack.controller.fluid.ControllerFluidBlockEntity;
import geni.witherutils.common.world.level.block.rack.controller.item.ControllerItemBlockEntity;
import geni.withercore.init.WUTBlockEntities;
import geni.withercore.common.util.FacingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

public class TerminalBlockEntity extends WitherMachineBlockEntity {

    public int multiBlockX, multiBlockY, multiBlockZ;
    public int multiBlockSize;
    
    public List<TerminalBlockEntity> accessoryTerminals;
    private final List<BlockPos> nbtTerminalList;
    
	public TerminalBlockEntity(BlockPos pos, BlockState state)
	{
		super(WUTBlockEntities.RACK_TERMINAL, pos, state);

        addDataSlot(NetworkDataSlot.INT.create(this::getMultiBlockX, p -> multiBlockX = p));
        addDataSlot(NetworkDataSlot.INT.create(this::getMultiBlockX, p -> multiBlockY = p));
        addDataSlot(NetworkDataSlot.INT.create(this::getMultiBlockX, p -> multiBlockZ = p));
        addDataSlot(NetworkDataSlot.INT.create(this::getMultiBlockSize, p -> multiBlockSize = p));

		this.accessoryTerminals = new ArrayList<>();
		this.nbtTerminalList = new ArrayList<>();
	}

	@Override
	public void serverTick()
	{
		super.serverTick();
		
		isFormed();
	}
	
	@Override
	public void clientTick()
	{
		super.clientTick();
	}

    @Override
    public void onLoad()
    {
        super.onLoad();

        doPostNBTSetup();
    }
    
    private void doPostNBTSetup()
    {
        if (!nbtTerminalList.isEmpty())
        {
            BlockState state = nonNullLevel().getBlockState(getBlockPos());
            if (state.getBlock() instanceof TerminalBlock)
                nonNullLevel().setBlock(getBlockPos(), state.setValue(BlockStateProperties.FORMED, isPrimaryTerminal()), 2);

            accessoryTerminals.clear();
            for (BlockPos valve : nbtTerminalList)
            {
                BlockEntity te = nonNullLevel().getBlockEntity(valve);
                if (te instanceof TerminalBlockEntity)
                {
                    accessoryTerminals.add((TerminalBlockEntity) te);
                }
            }
            nbtTerminalList.clear();
        }
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        
    	this.multiBlockSize = tag.getInt("multiBlockSize");
    	this.multiBlockX = tag.getInt("multiBlockX");
    	this.multiBlockY = tag.getInt("multiBlockY");
    	this.multiBlockZ = tag.getInt("multiBlockZ");
        
        ListTag accList = tag.getList("Terminals", Tag.TAG_COMPOUND);
        nbtTerminalList.clear();
        for (int i = 0; i < accList.size(); ++i)
        {
            CompoundTag posTag = accList.getCompound(i);
            nbtTerminalList.add(new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z")));
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        
        tag.putInt("multiBlockX", multiBlockX);
        tag.putInt("multiBlockY", multiBlockY);
        tag.putInt("multiBlockZ", multiBlockZ);
        tag.putInt("multiBlockSize", multiBlockSize);

        ListTag accList = new ListTag();
        for (TerminalBlockEntity terminal : accessoryTerminals)
        {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", terminal.getBlockPos().getX());
            posTag.putInt("y", terminal.getBlockPos().getY());
            posTag.putInt("z", terminal.getBlockPos().getZ());
            accList.add(posTag);
        }
        tag.put("Terminals", accList);
    }
    
    @Nonnull
    public Level nonNullLevel()
    {
        return Objects.requireNonNull(super.getLevel());
    }
    
    public void onMultiBlockBreak()
    {
        if (isPrimaryTerminal())
        {
            invalidateMultiBlock();
        }
    }
    
    private void invalidateMultiBlock() {
        for (int x = 0; x < multiBlockSize; x++) {
            for (int y = 0; y < multiBlockSize; y++) {
                for (int z = 0; z < multiBlockSize; z++) {
                	
                    BlockEntity te = nonNullLevel().getBlockEntity(new BlockPos(x + multiBlockX, y + multiBlockY, z + multiBlockZ));
                    if (te instanceof CaseBlockEntity teWall)
                    {
                        teWall.setPrimaryTerminal(null);
                    }
                }
            }
        }
        if (accessoryTerminals != null)
        {
            for (TerminalBlockEntity terminal : accessoryTerminals)
            {
            	terminal.multiBlockSize = 0;
            	terminal.multiBlockX = 0;
            	terminal.multiBlockY = 0;
            	terminal.multiBlockZ = 0;

                if (terminal != this)
                {
                	terminal.accessoryTerminals.clear();
                }
                terminal.setChanged();
            }
            accessoryTerminals.clear();
        }
    }

	public static boolean checkForming(Level world, BlockPos pos)
	{
		return calcForming(world, pos);
	}
	private static boolean calcForming(Level world, BlockPos pos)
	{
		return MultiStructure.checkForming(world, pos);
	}

    private boolean isPrimaryTerminal()
    {
        return multiBlockSize > 0;
    }
    
	public int getMultiBlockX()
	{
		return multiBlockX;
	}
	public int getMultiBlockY()
	{
		return multiBlockY;
	}
	public int getMultiBlockZ()
	{
		return multiBlockZ;
	}
	public int getMultiBlockSize()
	{
		return multiBlockSize;
	}
    
    /*
     * 
     * CONTROLLER
     * 
     */
	public boolean isFormed()
	{
		if (level == null)
			return false;
		BlockEntity be = level.getBlockEntity(worldPosition.relative(getCurrentFacing()));
		if(be instanceof ControllerItemBlockEntity)
		{
			ControllerItemBlockEntity ctrl = (ControllerItemBlockEntity) be;
			BlockPos ctrlPos = ctrl.getBlockPos();

			for(BlockPos aroundPos : FacingUtil.AROUND_ALL)
			{
				BlockEntity te = level.getBlockEntity(ctrlPos.offset(aroundPos));
				if (te instanceof CaseBlockEntity)
				{
					CaseBlockEntity caseBe = (CaseBlockEntity) te; 
					return CaseBlock.isFormed(caseBe.getBlockState());
				}
			}
		}
		else if(be instanceof ControllerFluidBlockEntity)
		{
			ControllerFluidBlockEntity ctrl = (ControllerFluidBlockEntity) be;
			BlockPos ctrlPos = ctrl.getBlockPos();

			for(BlockPos aroundPos : FacingUtil.AROUND_ALL)
			{
				BlockEntity te = level.getBlockEntity(ctrlPos.offset(aroundPos));
				if (te instanceof CaseBlockEntity)
				{
					CaseBlockEntity caseBe = (CaseBlockEntity) te; 
					return CaseBlock.isFormed(caseBe.getBlockState());
				}
			}
		}
		else if(be instanceof ControllerEnergyBlockEntity)
		{
			ControllerEnergyBlockEntity ctrl = (ControllerEnergyBlockEntity) be;
			BlockPos ctrlPos = ctrl.getBlockPos();

			for(BlockPos aroundPos : FacingUtil.AROUND_ALL)
			{
				BlockEntity te = level.getBlockEntity(ctrlPos.offset(aroundPos));
				if (te instanceof CaseBlockEntity)
				{
					CaseBlockEntity caseBe = (CaseBlockEntity) te; 
					return CaseBlock.isFormed(caseBe.getBlockState());
				}
			}
		}
		return false;
	}
	
    @Override
    public boolean hasItemCapability()
    {
        return true;
    }

    @Override
    public boolean hasFluidCapability()
    {
        return true;
    }

    @Override
    public boolean hasEnergyCapability()
    {
        return true;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side)
    {
        if (!isFormed() || level == null)
            return null;
        return getItemHandlerOpposite();
    }

    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side)
    {
        if (!isFormed() || level == null)
            return null;
        return getFluidHandlerOpposite();
    }

    @Override
    public IEnergyStorage getEnergyHandler(@Nullable Direction side)
    {
        if (!isFormed() || level == null)
            return null;
        return getEnergyHandlerOpposite();
    }

    @Nullable
    public IItemHandler getItemHandlerOpposite()
    {
        if (level == null)
            return null;
        return level.getCapability(Capabilities.ItemHandler.BLOCK, worldPosition.relative(getCurrentFacing()), null);
    }

    @Nullable
    public IFluidHandler getFluidHandlerOpposite()
    {
        if (level == null)
            return null;
        return level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(getCurrentFacing()), null);
    }

    @Nullable
    public IEnergyStorage getEnergyHandlerOpposite()
    {
        if (level == null)
            return null;
        return level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(getCurrentFacing()), null);
    }
}
