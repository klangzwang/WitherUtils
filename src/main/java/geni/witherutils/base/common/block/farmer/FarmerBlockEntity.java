package geni.witherutils.base.common.block.farmer;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import geni.witherutils.api.farm.FarmNotification;
import geni.witherutils.api.farm.FarmingAction;
import geni.witherutils.api.io.energy.EnergyIOMode;
import geni.witherutils.api.soulbank.QuadraticScalable;
import geni.witherutils.api.soulbank.SoulBankModifier;
import geni.witherutils.base.common.base.IInteractBlockEntity;
import geni.witherutils.base.common.base.RedstoneControl;
import geni.witherutils.base.common.base.WitherMachineEnergyFakeBlockEntity;
import geni.witherutils.base.common.init.WUTEntities;
import geni.witherutils.base.common.init.WUTFluids;
import geni.witherutils.base.common.init.WUTParticles;
import geni.witherutils.base.common.init.WUTSounds;
import geni.witherutils.base.common.io.fluid.MachineFluidHandler;
import geni.witherutils.base.common.io.fluid.WitherFluidTank;
import geni.witherutils.base.common.io.item.MachineInventory;
import geni.witherutils.base.common.io.item.MachineInventoryLayout;
import geni.witherutils.base.common.io.item.MultiSlotAccess;
import geni.witherutils.base.common.io.item.SingleSlotAccess;
import geni.witherutils.core.common.fertilizer.Fertilizer;
import geni.witherutils.core.common.iterator.PlanarBlockIterator;
import geni.witherutils.core.common.iterator.PlanarBlockIterator.Orientation;
import geni.witherutils.core.common.sync.BlockPosDataSlot;
import geni.witherutils.core.common.sync.BooleanDataSlot;
import geni.witherutils.core.common.sync.EnumDataSlot;
import geni.witherutils.core.common.sync.FluidStackDataSlot;
import geni.witherutils.core.common.sync.SyncMode;
import geni.witherutils.core.common.util.FakePlayerUtil;
import geni.witherutils.core.common.util.ItemStackUtil;
import geni.witherutils.core.common.util.ItemUtil;
import geni.witherutils.core.common.util.NNList;
import geni.witherutils.core.common.util.SoundUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

public class FarmerBlockEntity extends WitherMachineEnergyFakeBlockEntity implements MenuProvider, IInteractBlockEntity {

	public static final SingleSlotAccess TOOL = new SingleSlotAccess();
	public static final SingleSlotAccess FERT = new SingleSlotAccess();
	public static final SingleSlotAccess SEEDS1 = new SingleSlotAccess();
	public static final SingleSlotAccess SEEDS2 = new SingleSlotAccess();
	public static final SingleSlotAccess SEEDS3 = new SingleSlotAccess();
	public static final SingleSlotAccess SEEDS4 = new SingleSlotAccess();
	public static final MultiSlotAccess OUTPUTS = new MultiSlotAccess();
	
    public static final QuadraticScalable CAPACITY = new QuadraticScalable(SoulBankModifier.ENERGY_CAPACITY, () -> 16000);
    public static final QuadraticScalable TRANSFER = new QuadraticScalable(SoulBankModifier.ENERGY_TRANSFER, () -> 2000);
    public static final QuadraticScalable USAGE = new QuadraticScalable(SoulBankModifier.ENERGY_USE, () -> 0);
	
	public static final int TANK_CAPACITY = 4 * FluidType.BUCKET_VOLUME;
	public static final int TRANSFER_FLUID_PER_TICK = FluidType.BUCKET_VOLUME / 20;
	
    private final WitherFluidTank fluidTank;
    private final MachineFluidHandler fluidHandler;
    
    private PlanarBlockIterator blockIterator;

    private FarmingAction mode = FarmingAction.IDLE;

    private BlockPos farmingPos = this.getBlockPos();
    
    private float spraytimer;
	private int timer;

    @OnlyIn(Dist.CLIENT)
    public boolean soulBankInstalled;
    public boolean showFarmingPos = false;
    
    private float maxProgress = 20.0f;
    private float slideProgress;
    private float prevSlideProgress;
    
    public boolean lockedSW, lockedSE, lockedNW, lockedNE;
    
    private @Nonnull NNList<ItemStack> overflowQueue = new NNList<>();
    private final @Nonnull Set<FarmNotification> notification = EnumSet.noneOf(FarmNotification.class);
    
	public FarmerBlockEntity(BlockPos pos, BlockState state)
	{
        super(EnergyIOMode.Input, CAPACITY, TRANSFER, USAGE, WUTEntities.FARMER.get(), pos, state);

        this.fluidTank = createFluidTank(TANK_CAPACITY, TRANSFER_FLUID_PER_TICK);
        this.fluidHandler = new MachineFluidHandler(getIOConfig(), fluidTank);
        
        addCapabilityProvider(fluidHandler);

        addDataSlot(new FluidStackDataSlot(getFluidTank()::getFluid, getFluidTank()::setFluid, SyncMode.WORLD));
        add2WayDataSlot(new FluidStackDataSlot(fluidTank::getFluid, fluidTank::setFluid, SyncMode.GUI));
        
        addDataSlot(new BooleanDataSlot(this::isSoulBankInstalled, p -> soulBankInstalled = p, SyncMode.WORLD));
        addDataSlot(new EnumDataSlot<>(this::getMode, p -> mode = p, SyncMode.WORLD));
        
        add2WayDataSlot(new BooleanDataSlot(this::getLockedSW, p -> lockedSW = p, SyncMode.GUI));
        add2WayDataSlot(new BooleanDataSlot(this::getLockedSE, p -> lockedSE = p, SyncMode.GUI));
        add2WayDataSlot(new BooleanDataSlot(this::getLockedNW, p -> lockedNW = p, SyncMode.GUI));
        add2WayDataSlot(new BooleanDataSlot(this::getLockedNE, p -> lockedNE = p, SyncMode.GUI));
        
        add2WayDataSlot(new BooleanDataSlot(this::getShowFarmingPos, this::setShowFarmingPos, SyncMode.GUI));
        
        addDataSlot(new BlockPosDataSlot(this::getFarmingPos, p -> farmingPos = p, SyncMode.WORLD));
	}
	
	@Override
	public void serverTick()
	{
		super.serverTick();
		
    	if(fakePlayer == null)
    		this.fakePlayer = initFakePlayer((ServerLevel) level,
    				ForgeRegistries.BLOCKS.getKey(getBlockState().getBlock()).getPath(), this);
		
	    if(!canAct() || energyStorage.getEnergyStored() <= 0 || isOutputFull())
	    {
	    	setMode(FarmingAction.IDLE);
	    	setLitProperty(false);
	        return;
	    }

        setLitProperty(true);

        this.timer--;
        if(this.timer > 0)
            return;

        BlockPos farmingPos = findNextPos();
        if(farmingPos == null)
        {
            return;
        }
        else
        {
        	this.farmingPos = farmingPos;
        	energyStorage.takeEnergy(1);
            timer = 10 / (int)this.getSoulBankData().getBase();
        }
        
	    if (!executeHarvest())
	    {
	    	executeTilling();
	    	executePlanting();
			executeMoisture();
	    	executeBonemeal();
	    }
	}

	@Override
	public void clientTick()
	{
		super.clientTick();

        if (!fluidTank.isEmpty() && canWater(null) && mode != FarmingAction.IDLE)
        {
			spraytimer += 0.01F;
			sprayParticles();
	
			if(spraytimer > 2.0F)
				spraytimer = -2.0F;
        }
        
        prevSlideProgress = slideProgress;
        if(level.getBlockState(worldPosition).getValue(FarmerBlock.LIT))
        {
            if(slideProgress < Math.max(0, maxProgress))
            {
                slideProgress += 5.0f;
            }
        }
        else if(slideProgress > 0)
        {
            slideProgress -= 5.0f;
        }

        if(getRedstoneControl() == RedstoneControl.ACTIVE_WITHOUT_SIGNAL || getRedstoneControl().isActive(level.hasNeighborSignal(worldPosition)))
		{
			if (getInventory().getStackInSlot(getSoulBankSlot()).isEmpty())
			{
				setSingleNotification(FarmNotification.NO_BANK);
			}
			else
			{
				removeNotification(FarmNotification.NO_BANK);
				
				if (fluidTank.getFluidAmount() > 0)
				{
					removeNotification(FarmNotification.NO_WATER);
					
					if (isOutputFull())
					{
						setNotification(FarmNotification.OUTPUT_FULL);
					}
					else
					{
						removeNotification(FarmNotification.OUTPUT_FULL);
						
						if (getEnergyStorage().getEnergyStored() > 0)
						{
							removeNotification(FarmNotification.NO_POWER);
							
							if (!TOOL.getItemStack(inventory).isEmpty())
							{
								removeNotification(FarmNotification.NO_HOE);
								
								if(SEEDS1.getItemStack(inventory).isEmpty() || 
										   SEEDS2.getItemStack(inventory).isEmpty() || 
										   SEEDS3.getItemStack(inventory).isEmpty() || 
										   SEEDS4.getItemStack(inventory).isEmpty())
								{
									if (!SEEDS1.getItemStack(inventory).isEmpty())
										removeNotification(FarmNotification.NO_SEEDS);
									else
										setSingleNotification(FarmNotification.NO_SEEDS);
									
									if (!SEEDS2.getItemStack(inventory).isEmpty())
										removeNotification(FarmNotification.NO_SEEDS);
									else
										setSingleNotification(FarmNotification.NO_SEEDS);
									
									if (!SEEDS3.getItemStack(inventory).isEmpty())
										removeNotification(FarmNotification.NO_SEEDS);
									else
										setSingleNotification(FarmNotification.NO_SEEDS);
									
									if (!SEEDS4.getItemStack(inventory).isEmpty())
										removeNotification(FarmNotification.NO_SEEDS);
									else
										setSingleNotification(FarmNotification.NO_SEEDS);
								}
							}
							else
							{
								setSingleNotification(FarmNotification.NO_HOE);
							}
						}
						else
						{
							setSingleNotification(FarmNotification.NO_POWER);
						}
					}
				}
				else
				{
					setSingleNotification(FarmNotification.NO_WATER);
				}
			}
		}
	}
	
    protected @Nullable BlockPos findNextPos()
    {
        int i = 20;

        while(i-- > 0)
        {
            BlockPos farmingPos = getNextCoord();
            if (!farmingPos.equals(getBlockPos()) && level.isLoaded(farmingPos))
            {
            	return farmingPos;
            }
        }
        return null;
    }

    private @Nonnull BlockPos getNextCoord()
    {
        if(blockIterator == null || !blockIterator.hasNext())
        {
            blockIterator = new PlanarBlockIterator(getBlockPos(), Orientation.HORIZONTAL, getFarmSize());
        }
        return blockIterator.next();
    }
    
	private void executeTilling()
    {
		BlockState farmState = level.getBlockState(farmingPos.below(1));
		Block farmBlock = farmState.getBlock();
		BlockPos farmOffset = farmingPos.subtract(worldPosition);
        ItemStack heldstack = TOOL.getItemStack(inventory);
        
        if(level.getBlockState(farmingPos).isAir() && heldstack.getItem() instanceof HoeItem)
        {
            if(farmBlock == Blocks.GRASS_BLOCK || farmBlock == Blocks.DIRT_PATH || farmBlock == Blocks.DIRT || farmBlock instanceof GrassBlock)
            {
    			if(farmOffset.getX() > 0 && farmOffset.getZ() < 0 || farmOffset.getX() < 0 && farmOffset.getZ() > 0 || farmOffset.getX() < 0 && farmOffset.getZ() < 0 || farmOffset.getX() > 0 && farmOffset.getZ() > 0)
    			{
    				FakePlayerUtil.rightClickInDirection(fakePlayer.get(), level, farmingPos, Direction.DOWN, level.getBlockState(farmingPos));
    				FakePlayerUtil.setupFakePlayerForUse(fakePlayer.get(), farmingPos, Direction.DOWN, heldstack, false);
    				setMode(FarmingAction.TILLING);
    				energyStorage.takeEnergy(100);
    			}
            }
        }
    }

	public boolean hasSeed(@Nonnull ItemStack seeds, @Nonnull BlockPos pos)
	{
		return ItemUtil.areStacksEqual(seeds, mapBlockPosToSeedSlot(pos).getItemStack(inventory));
	}
	@Nonnull
	public ItemStack getSeedTypeInSuppliesFor(@Nonnull BlockPos pos)
	{
		return mapBlockPosToSeedSlot(pos).getItemStack(inventory);
	}
	@Nonnull
	public ItemStack takeSeedFromSupplies(@Nonnull ItemStack seeds, @Nonnull BlockPos pos, boolean simulate)
	{
		SingleSlotAccess slot = mapBlockPosToSeedSlot(pos);
		ItemStack inv = slot.getItemStack(inventory);
		if(!inv.isEmpty() && (seeds.isEmpty() || ItemUtil.areStacksEqual(seeds, inv)))
		{
			if (inv.getCount() > 1 || !this.isSlotLocked(slot))
			{
				if (simulate)
				{
					return inv.copy().split(1);
				}
				else
				{
					this.setChanged();
					return inv.split(1);
				}
			}
		}
		return ItemStack.EMPTY;
	}
	private @Nonnull SingleSlotAccess mapBlockPosToSeedSlot(@Nonnull BlockPos pos)
	{
		BlockPos offset = pos.subtract(getBlockPos());
		if (offset.getX() <= 0 && offset.getZ() > 0)
		{
			return SEEDS1;
		}
		else if (offset.getX() > 0 && offset.getZ() >= 0)
		{
			return SEEDS2;
		}
		else if (offset.getX() < 0 && offset.getZ() <= 0)
		{
			return SEEDS3;
		}
		return SEEDS4;
	}
	
	public boolean isSlotLocked(@Nonnull BlockPos pos)
	{
		return this.isSlotLocked(mapBlockPosToSeedSlot(pos));
	}
	public boolean isSlotLocked(SingleSlotAccess slot)
	{
        return slot.getLocked();
	}
	public void setSlotLocked(SingleSlotAccess slot, boolean value)
	{
        slot.setLocked(slot, value);
	}
	
	private void executePlanting()
    {
		BlockState hoestate = level.getBlockState(farmingPos);
		BlockState farmState = level.getBlockState(farmingPos.below(1));
		Block farmBlock = farmState.getBlock();
		BlockPos farmOffset = farmingPos.subtract(worldPosition);
		ItemStack heldstack = ItemStack.EMPTY;
		
        if(level.getBlockState(farmingPos).isAir() && farmBlock instanceof FarmBlock)
        {
            if(farmOffset.getX() <= 0 && farmOffset.getZ() > 0)
            {
           		if(lockedSW && SEEDS1.getItemStack(inventory).getCount() == 1)
           			return;
            	if(SEEDS1.getItemStack(inventory).getCount() != 0)
            		heldstack = SEEDS1.getItemStack(inventory);
            }
            else if(farmOffset.getX() > 0 && farmOffset.getZ() >= 0)
            {
           		if(lockedSE && SEEDS2.getItemStack(inventory).getCount() == 1)
           			return;
            	if(SEEDS2.getItemStack(inventory).getCount() != 0)
            		heldstack = SEEDS2.getItemStack(inventory);
            }
            else if(farmOffset.getX() < 0 && farmOffset.getZ() <= 0)
            {
           		if(lockedNW && SEEDS3.getItemStack(inventory).getCount() == 1)
           			return;
            	if(SEEDS3.getItemStack(inventory).getCount() != 0)
            		heldstack = SEEDS3.getItemStack(inventory);
            }
            else
            {
           		if(lockedNE && SEEDS4.getItemStack(inventory).getCount() == 1)
           			return;
            	if(SEEDS4.getItemStack(inventory).getCount() != 0)
            		heldstack = SEEDS4.getItemStack(inventory);
            }
            
            if(!heldstack.isEmpty())
            {
				FakePlayerUtil.rightClickInDirection(fakePlayer.get(), level, farmingPos, Direction.DOWN, hoestate);
				FakePlayerUtil.setupFakePlayerForUse(fakePlayer.get(), farmingPos, Direction.DOWN, heldstack, false);
            	setMode(FarmingAction.PLANTING);
    			energyStorage.takeEnergy(10);
            }

//    		if(hasSeed(SEEDS1.getItemStack(inventory), farmOffset))
//    		{
//    			if(lockedSW)
//    			{
//    				if(SEEDS1.getItemStack(inventory).getCount() == 1)
//    					return;
//    			}
//                if(farmOffset.getX() <= 0 && farmOffset.getZ() > 0)
//                {
//                	if(!SEEDS1.getItemStack(inventory).isEmpty())
//                		heldstack = SEEDS1.getItemStack(inventory);
//                }
//    		}
//    		if(hasSeed(SEEDS2.getItemStack(inventory), farmOffset))
//    		{
//    			if(lockedSE)
//    			{
//    				if(SEEDS2.getItemStack(inventory).getCount() == 1)
//    					return;
//    			}
//                if(farmOffset.getX() > 0 && farmOffset.getZ() >= 0)
//                {
//                	if(!SEEDS2.getItemStack(inventory).isEmpty())
//                		heldstack = SEEDS2.getItemStack(inventory);
//                }
//    		}
//    		if(hasSeed(SEEDS3.getItemStack(inventory), farmOffset))
//    		{
//    			if(lockedNW)
//    			{
//    				if(SEEDS3.getItemStack(inventory).getCount() == 1)
//    					return;
//    			}
//                if(farmOffset.getX() < 0 && farmOffset.getZ() <= 0)
//                {
//                	if(!SEEDS3.getItemStack(inventory).isEmpty())
//                		heldstack = SEEDS3.getItemStack(inventory);
//                }
//    		}
//    		if(hasSeed(SEEDS4.getItemStack(inventory), farmOffset))
//    		{
//    			if(lockedNE)
//    			{
//    				if(SEEDS4.getItemStack(inventory).getCount() == 1)
//    					return;
//    			}
//                if(farmOffset.getX() >= 0 && farmOffset.getZ() < 0)
//                {
//                	if(!SEEDS4.getItemStack(inventory).isEmpty())
//                		heldstack = SEEDS4.getItemStack(inventory);
//                }
//    		}
        }
    }
	
	private void executeMoisture()
    {
		BlockState farmState = level.getBlockState(farmingPos.below(1));
		Block farmBlock = farmState.getBlock();
		
        if(farmBlock instanceof FarmBlock)
        {
            if (!fluidTank.isEmpty() && canWater(null) && farmState.getValue(BlockStateProperties.MOISTURE) < 7)
            {
                if (this.level.random.nextFloat() >= 0.45F)
               		this.level.setBlock(farmingPos.below(), farmState.setValue(BlockStateProperties.MOISTURE, 7), 2);
            	setMode(FarmingAction.MOISTURE);
               	fluidTank.drain(1, FluidAction.EXECUTE);
            }
        }
    }
    
	private void executeBonemeal()
	{
        ItemStack stack = FERT.getItemStack(inventory);
        if (!stack.isEmpty())
        {
            if(energyStorage.getEnergyStored() < 160)
            	return;
        	if(hasUsedBonemeal())
        	{
                stack.shrink(1);
	        	setMode(FarmingAction.FERTILIZE);
	    		energyStorage.takeEnergy(160);
        	}
        }
    	FluidStack tankstack = getTankFluid();
       	if(!tankstack.isEmpty() && tankstack.getFluid() == WUTFluids.FERTILIZER.get())
       	{
            if(fluidTank.getFluidAmount() < 250)
            	return;
       		
            if (level.random.nextFloat() >= 0.9F)
            {
            	if(hasUsedBonemeal())
            	{
    	        	setMode(FarmingAction.FERTILIZE);
    	        	fluidTank.drain(250, FluidAction.EXECUTE);
                    level.levelEvent(1505, farmingPos, 0);
            	}
            }
       	}
    }
	public boolean hasUsedBonemeal()
	{
        if (level.isLoaded(farmingPos))
        {
            BlockState state = this.level.getBlockState(farmingPos);
            Block block = state.getBlock();
            if (block instanceof BonemealableBlock)
            {
                if (((BonemealableBlock) block).isValidBonemealTarget(level, farmingPos, state, false) && ((BonemealableBlock) block).isBonemealSuccess(level, level.random, farmingPos, state))
                {
                    ((BonemealableBlock) block).performBonemeal((ServerLevel) level, level.random, farmingPos, state);
                    if (((BonemealableBlock) block).isValidBonemealTarget(level, farmingPos, state, false))
                    {
                    	return true;
                    }
                }
            }
        }
		return false;
	}

//    private void executeBonemeal(@Nonnull BlockPos farmingPos, @Nonnull BlockState bs)
//    {
//        if(bs.getBlock() instanceof CropBlock crop)
//        {
//        	FluidStack tankstack = getTankFluid();
//           	if(!tankstack.isEmpty() && tankstack.getFluid() == FluidFertilizerHolder.getFertilizerFluid() && crop.getAge(bs) < crop.getMaxAge())
//           	{
//	        	setMode(FarmingAction.FERTILIZE);
//           		
//                if (level.random.nextFloat() >= 0.9F)
//                {
//                	crop.growCrops(level, farmingPos, bs);
//                    level.levelEvent(1505, farmingPos, 0);
//                }
//           	}
//
//    		ItemStack heldstack = FERT.getItemStack(inventory);
//	        if(!heldstack.isEmpty() && crop.getAge(bs) < crop.getMaxAge())
//	        {
//	        	setMode(FarmingAction.FERTILIZE);
//	        	
//				setupFakeRightClick(heldstack, farmingPos);
//	    		energyStorage.takeEnergy(160);
//	        }
//        }
//    }
	
    private boolean executeHarvest()
    {
		BlockState farmState = level.getBlockState(farmingPos);
		Block farmBlock = farmState.getBlock();
    	
        if(farmBlock instanceof CropBlock crop)
        {
            if(crop.getAge(farmState) == crop.getMaxAge())
            {
                List<ItemStack> drops = CropBlock.getDrops(farmState, (ServerLevel) level, farmingPos, null);
    			for(ItemStack itemstack : drops)
    			{
    				if(Block.byItem(itemstack.getItem()) instanceof CropBlock)
    					drops.forEach(stack -> ItemHandlerHelper.insertItem(inventory, stack, false));
    				ItemStackUtil.insertItemMultiSlot(inventory, itemstack, OUTPUTS);
    			}
    			level.destroyBlock(farmingPos, false);
            	setMode(FarmingAction.HARVEST);
    			energyStorage.takeEnergy(500);
    	        return true;
            }
        }
        return false;
    }
    
    public int getFarmSize()
    {
    	int farmsize = 0;
    	switch((int)this.getSoulBankData().getBase())
    	{
    		case(0):
    			farmsize = 0;
    			break;
    		case(1):
    			farmsize = 3;
    			break;
    		case(2):
    			farmsize = 4;
    			break;
    		case(3):
    			farmsize = 6;
    			break;
    	}
    	return farmsize;
    }
    
    /*
     * 
     * INVENTORY 
     * 
     */
    @Override
    protected MachineInventory createMachineInventory(MachineInventoryLayout layout)
    {
        return new MachineInventory(getIOConfig(), layout)
        {
            @Override
            protected void onContentsChanged(int slot)
            {
                onInventoryContentsChanged(slot);
                setChanged();
            }
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate)
            {
            	if(slot == getInventory().getLayout().getSoulBankSlot())
            		SoundUtil.playSlotSound(level, worldPosition, WUTSounds.SLOT.get(), 1.0f, 1.0f);
	      		return super.extractItem(slot, amount, simulate);
            }
            @Override
            public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
            {
            	if(slot == getInventory().getLayout().getSoulBankSlot())
            		SoundUtil.playSlotSound(level, worldPosition, WUTSounds.REACT.get(), 0.4f, 1.0f);
            	else if(slot == TOOL.getIndex())
            		return stack.getItem() instanceof HoeItem ? super.insertItem(slot, stack, simulate) : stack;
            	else if(slot == FERT.getIndex())
            		return stack.getItem() instanceof BoneMealItem ||
            				ModList.get().isLoaded("industrialforegoing") &&
            				ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath() == "fertilizer" ? super.insertItem(slot, stack, simulate) : stack;
            	else if(slot == SEEDS1.getIndex() || slot == SEEDS2.getIndex() || slot == SEEDS3.getIndex() || slot == SEEDS4.getIndex())
            		return Block.byItem(stack.getItem()) instanceof CropBlock ? super.insertItem(slot, stack, simulate) : stack;
            	
	      		return super.insertItem(slot, stack, simulate);
            }
        	@Override
        	public boolean isItemValid(int slot, ItemStack stack)
        	{
        		if(slot == TOOL.getIndex())
        			return stack.getItem() instanceof HoeItem;
        		if(slot == FERT.getIndex())
        			return Fertilizer.isFertilizer(stack);
            	if(slot == SEEDS1.getIndex())
            		return getLockedSW() ? ItemUtil.areStacksEqual(stack, getStackInSlot(slot)) : true;
            	if(slot == SEEDS2.getIndex())
            		return getLockedSE() ? ItemUtil.areStacksEqual(stack, getStackInSlot(slot)) : true;
            	if(slot == SEEDS3.getIndex())
            		return getLockedNW() ? ItemUtil.areStacksEqual(stack, getStackInSlot(slot)) : true;
            	if(slot == SEEDS4.getIndex())
            		return getLockedNE() ? ItemUtil.areStacksEqual(stack, getStackInSlot(slot)) : true;
           		return super.isItemValid(slot, stack);
        	}
        	@Override
        	protected int getStackLimit(int slot, @NotNull ItemStack stack)
        	{
        		if(slot == SEEDS1.getIndex() || slot == SEEDS2.getIndex() || slot == SEEDS3.getIndex() || slot == SEEDS4.getIndex())
        			return 12;
        		return super.getStackLimit(slot, stack);
        	}
        };
    }
    
    @Override
    public MachineInventoryLayout getInventoryLayout()
    {
        return MachineInventoryLayout.builder()
            .inputSlot().slotAccess(TOOL)
            .inputSlot().slotAccess(FERT)
            .inputSlot().slotAccess(SEEDS1)
            .inputSlot().slotAccess(SEEDS2)
            .inputSlot().slotAccess(SEEDS3)
            .inputSlot().slotAccess(SEEDS4)
            .outputSlot(4).slotAccess(OUTPUTS)
            .soulbank().build();
    }
	
    public boolean getLockedSW()
    {
    	return SEEDS1.getLocked();
    }
    public void setLockedSW(boolean lockedSW)
    {
    	SEEDS1.setLocked(lockedSW);
    }
    
    public boolean getLockedSE()
    {
    	return SEEDS2.getLocked();
    }
    public void setLockedSE(boolean lockedSE)
    {
    	SEEDS2.setLocked(lockedSE);
    }

    public boolean getLockedNW()
    {
    	return SEEDS3.getLocked();
    }
    public void setLockedNW(boolean lockedNW)
    {
    	SEEDS3.setLocked(lockedNW);
    }

    public boolean getLockedNE()
    {
    	return SEEDS4.getLocked();
    }
    public void setLockedNE(boolean lockedNE)
    {
    	SEEDS4.setLocked(lockedNE);
    }
	
	/*
	 * 
	 * SCREEN
	 * 
	 */
    @Override
    public InteractionResult onBlockUse(BlockState state, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (!level.isClientSide)
        {
			BlockEntity tileEntity = level.getBlockEntity(worldPosition);
			if(tileEntity instanceof MenuProvider)
			{
				NetworkHooks.openScreen((ServerPlayer) player, (MenuProvider) tileEntity, tileEntity.getBlockPos());
                SoundUtil.playSoundFromServer((ServerPlayer) player, WUTSounds.PICKRIB.get(), 1.0f, 1.0f);
			}
			else
			{
				throw new IllegalStateException("Our named container provider is missing!");
			}
        }
        return InteractionResult.SUCCESS;
    }
    
    /*
     * 
     * TANK
     * 
     */
    private WitherFluidTank createFluidTank(int capacity, int maxTransfer)
    {
    	return new WitherFluidTank(this, capacity)
        {
			@Override
    		public boolean isFluidValid(FluidStack stack)
    		{
    			return stack.getFluid() == Fluids.WATER || stack.getFluid() == WUTFluids.FERTILIZER.get();
    		}
            @Override
            protected void onContentsChanged()
            {
            	onTankContentsChanged();
                super.onContentsChanged();
                setChanged();
            }
        };
    }

	private BoundingBox wateringBounds = new BoundingBox(getBlockPos().below()).moved((int)2, 0, (int)2);
	
	public boolean canWater(@Nonnull Vec3 toMatch)
	{
		return wateringBounds != null && fluidTank.getFluidAmount() > 0;
	}
	
	public @Nonnull BoundingBox getBounds()
	{
		return new BoundingBox(getBlockPos()).moved((int)2, 0, (int)2);
	}
	public FluidStack getFluid()
	{
		return fluidTank == null ? FluidStack.EMPTY : fluidTank.getFluid();
	}
	public float getCapacity()
	{
		return TANK_CAPACITY;
	}
	
    public WitherFluidTank getFluidTank()
    {
        return fluidTank;
    }
	public FluidStack getTankFluid()
	{
		return fluidTank.getFluid();
	}
	public void setFluid(FluidStack fluid)
	{
		fluidTank.setFluid(fluid);
	}
	public boolean hasTank()
	{
		if(fluidTank.getTanks() <= 0)
			return false;
		return true;
	}

	@Nonnull
	public WitherFluidTank[] getOutputTanks()
	{
		return new WitherFluidTank[0];
	}
	public void setTanksDirty()
	{
		setChanged();
	}

	private void sprayParticles()
	{
		ParticleOptions type = null;
       	if(getTankFluid().getFluid() == WUTFluids.FERTILIZER.get())
       		type = WUTParticles.FERTSPRAY.get();
       	else
       		type = WUTParticles.LIQUIDSPRAY.get();

		for(int i = 0; i < 4; i++)
		{
			level.addParticle(type,
				worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.1D,
				worldPosition.getY() + 1.1,
				worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.1D,
				spraytimer, 2.0D, level.random.nextGaussian() * spraytimer);
			level.addParticle(type,
				worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.1D,
				worldPosition.getY() + 1.1,
				worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.1D,
				-spraytimer, 2.0D, level.random.nextGaussian() * -spraytimer);
			level.addParticle(type,
				worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.1D,
				worldPosition.getY() + 1.0,
				worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.1D,
				-spraytimer, 2.0D, level.random.nextGaussian() * spraytimer);
			level.addParticle(type,
				worldPosition.getX() + 0.5 + level.random.nextGaussian() * 0.1D,
				worldPosition.getY() + 1.0,
				worldPosition.getZ() + 0.5 + level.random.nextGaussian() * 0.1D,
				spraytimer, 2.0D, level.random.nextGaussian() * -spraytimer);
		}
	}

	@Override
	public void resetFakeStamina()
	{
	}
	
    @Override
    public void saveAdditional(CompoundTag pTag)
    {
        super.saveAdditional(pTag);
        pTag.put("fluid", fluidTank.writeToNBT(new CompoundTag()));
        pTag.putBoolean("lockedsw", lockedSW);
        pTag.putBoolean("lockedse", lockedSE);
        pTag.putBoolean("lockednw", lockedNW);
        pTag.putBoolean("lockedne", lockedNE);
    }

    @Override
    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        fluidTank.readFromNBT(pTag.getCompound("fluid"));
        lockedSW = pTag.getBoolean("lockedsw");
        lockedSE = pTag.getBoolean("lockedse");
        lockedNW = pTag.getBoolean("lockednw");
        lockedNE = pTag.getBoolean("lockedne");
    }

    public float getSlideProgress(float partialTicks)
    {
        float partialSlideProgress = prevSlideProgress + (slideProgress - prevSlideProgress) * partialTicks;
        float normalProgress = partialSlideProgress / (float) maxProgress;
        return 0.815F * (1.0F - ((float) Math.sin(Math.toRadians(90.0 + 180.0 * normalProgress)) / 2.0F + 0.5F));
    }
    
    private void onTankContentsChanged()
    {
        if (level != null)
        {
            if (!level.isClientSide())
            {
            }
        }
    }
    
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory playerInventory, Player playerEntity)
    {
        return new FarmerContainer(this, playerInventory, i);
    }
    
    /*
     * 
     * MODE
     * 
     * 
     */
	public FarmingAction getMode()
	{
		return mode;
	}
	public void setMode(FarmingAction mode)
	{
		this.mode = mode;
        setChanged();
	}

	/*
	 * 
	 * FARMINGPOS
	 * 
	 */
	public BlockPos getFarmingPos()
	{
		return farmingPos;
	}
	public boolean getShowFarmingPos()
	{
		return showFarmingPos;
	}
	public void setShowFarmingPos(boolean showFarmingPos)
	{
		this.showFarmingPos = showFarmingPos;
	}
	/*
	 * 
	 * NOTIFICATION
	 * 
	 */
	public @Nonnull Set<FarmNotification> getNotification()
	{
	    return notification;
	};

	public void setSingleNotification(@Nonnull FarmNotification note)
	{
		setNotification(note);
		for (Iterator<FarmNotification> itr = notification.iterator(); itr.hasNext();)
		{
			if (itr.next() != note)
			{
				itr.remove();
			}
		}
	}
	public void setNotification(@Nonnull FarmNotification note)
	{
		if (!notification.contains(note))
		{
			notification.add(note);
		}
	}
	
	public void removeNotification(FarmNotification note)
	{
		getNotification().remove(note);
	}

	public void clearNotification(boolean all)
	{
		if (hasNotification())
		{
			if (all)
			{
				getNotification().clear();
			}
			else
			{
				for (Iterator<FarmNotification> itr = notification.iterator(); itr.hasNext();)
				{
					if (itr.next().isAutoCleanup())
					{
						itr.remove();
					}
				}
			}
		}
	}

	public boolean hasNotification()
	{
		return !getNotification().isEmpty();
	}

	private boolean isOutputFull()
	{
//		if (!overflowQueue.isEmpty())
//		{
//			NNList<ItemStack> old = overflowQueue;
//			overflowQueue = new NNList<>();
//			handleExtraItems(old, worldPosition);
//		}
    	for (SingleSlotAccess access : OUTPUTS.getAccesses())
        {
    		ItemStack curStack = access.getItemStack(this);
    		if (curStack.isEmpty() || curStack.getCount() < curStack.getMaxStackSize())
    		{
				return false;
    		}
//            if(!inventory.getStackInSlot(access.getIndex()).isEmpty())
//            {
//            	return true;
//            }
        }
		return true;
	}

//	public void handleExtraItems(@Nonnull NonNullList<ItemStack> items, @Nullable BlockPos pos)
//	{
//		NNList.wrap(items).apply(new Callback<ItemStack>()
//		{
//			@Override
//			public void apply(@Nonnull ItemStack stack)
//			{
//				handleExtraItem(stack, NullHelper.first(pos, getBlockPos()));
//			}
//		});
//	}
//	public void handleExtraItem(@Nonnull ItemStack stack, @Nullable BlockPos drop)
//	{
//		if (!stack.isEmpty())
//		{
//			if (drop == null)
//			{
//				drop = getBlockPos();
//			}
//			InsertCallback insertCallback = new InsertCallback(stack, this);
//			insertCallback.apply(mapBlockPosToSeedSlot(drop));
//			if (!stack.isEmpty())
//			{
//				FarmerSlots.SEEDS.apply(insertCallback);
//				if (!stack.isEmpty())
//				{
//					FarmerSlots.OUTPUTS.apply(insertCallback);
//					if (!stack.isEmpty())
//					{
//						Block.popResource(getLevel(), drop, stack.copy());
//						
////						if (FarmConfig.useOutputQueue.get())
////						{
////							enQueueOverflow(stack.copy());
////						}
////						else
////						{
////							Block.popResource(getLevel(), drop, stack.copy());
////						}
//						stack.setCount(0);
//					}
//				}
//			}
//		}
//	}
//	
//	public void enQueueOverflow(@Nonnull ItemStack stack)
//	{
//		overflowQueue.add(stack);
//	}

//	public @Nonnull ItemStack getStackInSlot(int slot)
//	{
//		if (slot < 0 || slot > 3)
//		{
//			return ItemStack.EMPTY;
//		}
//		final ItemStack itemStack = INPUTS.get(slot).getItemStack(inventory);
//		return itemStack == null ? ItemStack.EMPTY : itemStack;
//	}
//
//	public void setInventorySlotContents(int slot, @Nonnull ItemStack contents)
//	{
//		INPUTS.get(slot).setStackInSlot(inventory, contents.copy());
//		if (INPUTS.get(slot).getItemStack(inventory).getCount() > 64)
//		{
//			INPUTS.get(slot).getItemStack(inventory).setCount(64);
//			contents.shrink(64);
//			Block.popResource(level, worldPosition, contents);
//		}
//		setChanged();
//	}

//	/*
//	 * 
//	 * INSERTCALLBACK
//	 * 
//	 */
//	private class InsertCallback implements ShortCallback<FarmerSlots>
//	{
//		private final @Nonnull ItemStack stack;
//		private FarmerSlots emptySlot = null;
//		private final FarmerBlockEntity owner;
//		
//		public InsertCallback(@Nonnull ItemStack stack, FarmerBlockEntity owner)
//		{
//			this.stack = stack;
//			this.owner = owner;
//		}
//
//		@Override
//		public boolean apply(@Nonnull FarmerSlots slot)
//		{
//			if (slot.isValid(owner, stack))
//			{
//				ItemStack slotStack = slot.get(owner);
//				if (slotStack.isEmpty())
//				{
//					if (emptySlot == null)
//					{
//						emptySlot = slot;
//					}
//				}
//				else if (ItemUtil.areStackMergable(stack, slotStack) && !ItemUtil.isStackFull(slotStack))
//				{
//					int addable = Math.max(0, Math.min(slotStack.getMaxStackSize(), slot.getInventoryStackLimit(owner)) - slotStack.getCount());
//					if (addable >= stack.getCount())
//					{
//						slotStack.grow(stack.getCount());
//						stack.setCount(0);
//						owner.setChanged();
//					}
//					else
//					{
//						slotStack.grow(addable);
//						stack.shrink(addable);
//						owner.setChanged();
//					}
//				}
//			}
//			return stack.isEmpty();
//		}
//
//		@Override
//		public boolean finish()
//		{
//			if (emptySlot != null)
//			{
//				emptySlot.set(owner, stack.copy());
//				stack.setCount(0);
//				owner.setChanged();
//			}
//			return stack.isEmpty();
//		}
//	}

}
