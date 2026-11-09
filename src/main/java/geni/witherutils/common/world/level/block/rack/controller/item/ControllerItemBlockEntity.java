package geni.witherutils.common.world.level.block.rack.controller.item;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.Nonnull;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;
import software.bernie.geckolib.util.RenderUtil;

public class ControllerItemBlockEntity extends WitherMachineBlockEntity {

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private static final HashMap<UUID, Long> INTERACTION_LOGGER = new HashMap<>();

    public static final int DEFAULT_ITEM_CAPACITY = 262144;
    public static final int ITEMCAPACITY = DEFAULT_ITEM_CAPACITY;

    private ItemStack blStack = ItemStack.EMPTY;
    private int itemsStored = 0;
    private boolean hasItemNBT = false;

    private final ItemControllerHandler itemHandler;

    public ControllerItemBlockEntity(BlockPos pos, BlockState state)
    {
        this(WUTBlockEntities.RACKITEM_CONTROLLER, pos, state);
    }

    public ControllerItemBlockEntity(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);

        this.itemHandler = new ItemControllerHandler(ITEMCAPACITY);

        addDataSlot(NetworkDataSlot.ITEM_STACK.create(this::getBlStack, this::setBlStack));
        addDataSlot(NetworkDataSlot.INT.create(this::getItemsStored, this::setItemsStored));
    }

    @Override
    public void serverTick()
    {
        super.serverTick();
        itemHandler.setCanHandle(isFormed());
    }

    @Override
    public void clientTick()
    {
        super.clientTick();
        setLitProperty(itemsStored > 0);
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
    public boolean hasItemCapability()
    {
        return true;
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side)
    {
        return itemHandler;
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
        this.hasItemNBT = tag.getBoolean("hasitemnbt");
        this.itemsStored = tag.getInt("itemsstored");

        if (tag.contains("blstack", 10))
        {
            this.blStack = ItemStack.parseOptional(registries, tag.getCompound("blstack"));
        }
        else if (tag.contains("blStack", 10))
        {
            this.blStack = ItemStack.parseOptional(registries, tag.getCompound("blStack"));
        }
        else
        {
            this.blStack = ItemStack.EMPTY;
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        tag.putBoolean("hasitemnbt", this.hasItemNBT);
        tag.putInt("itemsstored", this.itemsStored);

        if (!this.blStack.isEmpty())
        {
            tag.put("blstack", this.blStack.saveOptional(registries));
        }
    }

    /*
     *
     * GETTER / SETTER
     *
     */
    public ItemStack getBlStack()
    {
        return blStack;
    }

    public ItemStack getDisplayStack()
    {
        return blStack;
    }

    public void setBlStack(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            this.blStack = ItemStack.EMPTY;
            this.hasItemNBT = false;
        }
        else
        {
            this.blStack = stack.copyWithCount(1);
            this.hasItemNBT = !this.blStack.isComponentsPatchEmpty();
        }
        setChanged();
    }

    public int getItemsStored()
    {
        return itemsStored;
    }

    public void setItemsStored(int stored)
    {
        this.itemsStored = Math.max(0, stored);
        if (this.itemsStored == 0)
        {
            this.blStack = ItemStack.EMPTY;
        }
        setChanged();
    }

    public boolean hasItemNBT()
    {
        return hasItemNBT;
    }

    public void setHasItemNBT(boolean hasItemNBT)
    {
        this.hasItemNBT = hasItemNBT;
        setChanged();
    }

    /*
     *
     * FORMAT AMOUNT
     *
     */
    public String getFormatedDisplayAmount()
    {
        if (itemsStored == 0)
            return "0";

        if (itemsStored >= 64)
        {
            String stacks = getFormatedBigNumber(itemsStored / 64) + " x64";
            int remainder = itemsStored % 64;
            return remainder != 0 ? stacks + " + " + remainder : stacks;
        }
        return String.valueOf(itemsStored);
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
     * ITEM HANDLER
     *
     */
    public class ItemControllerHandler implements IItemHandler {

        private final int capacity;
        private boolean canHandle = true;

        public ItemControllerHandler(int capacity)
        {
            this.capacity = capacity;
        }

        public void setCanHandle(boolean canHandle)
        {
            this.canHandle = canHandle;
        }

        public boolean canHandle()
        {
            return canHandle;
        }

        @Override
        public int getSlots()
        {
            return 1;
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot)
        {
            if (slot != 0 || blStack.isEmpty() || itemsStored <= 0)
            {
                return ItemStack.EMPTY;
            }
            ItemStack copied = blStack.copy();
            copied.setCount(itemsStored);
            return copied;
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate)
        {
            if (slot != 0 || stack.isEmpty() || !isItemValid(slot, stack) || !canHandle)
            {
                return stack;
            }

            int availableSpace = capacity - itemsStored;
            if (availableSpace <= 0)
            {
                return stack;
            }

            int toInsert = Math.min(availableSpace, stack.getCount());
            if (!simulate)
            {
                if (blStack.isEmpty())
                {
                    setBlStack(stack);
                }
                setItemsStored(itemsStored + toInsert);
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
            if (slot != 0 || !canHandle || amount <= 0 || blStack.isEmpty() || itemsStored <= 0)
            {
                return ItemStack.EMPTY;
            }

            int toExtract = Math.min(itemsStored, amount);
            ItemStack result = blStack.copyWithCount(toExtract);

            if (!simulate)
            {
                setItemsStored(itemsStored - toExtract);
            }
            return result;
        }

        @Override
        public int getSlotLimit(int slot)
        {
            return capacity;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack)
        {
            if (slot != 0 || stack.isEmpty())
            {
                return false;
            }
            if (blStack.isEmpty() || itemsStored == 0)
            {
                return true;
            }
            return ItemStack.isSameItemSameComponents(blStack, stack);
        }
    }

    /*
     *
     * CLICK INSERT / EXTRACT
     *
     */
    public void onLeftClicked(Player player)
    {
        if (level == null || level.isClientSide())
            return;

        int amountToExtract = player.isShiftKeyDown() ? 64 : 1;
        ItemStack extracted = itemHandler.extractItem(0, amountToExtract, false);
        if (!extracted.isEmpty())
        {
            ItemHandlerHelper.giveItemToPlayer(player, extracted);
        }
    }

    public void onRightClicked(Player player, InteractionHand hand)
    {
        if (level == null || level.isClientSide())
            return;

        ItemStack heldItem = player.getItemInHand(hand);
        long currentTime = System.currentTimeMillis();
        long lastInteraction = INTERACTION_LOGGER.getOrDefault(player.getUUID(), 0L);

        if (!heldItem.isEmpty() && itemHandler.isItemValid(0, heldItem))
        {
            ItemStack remainder = itemHandler.insertItem(0, heldItem, false);
            player.setItemInHand(hand, remainder);
        }
        else if (currentTime - lastInteraction < 300)
        {
            // Double click: deposit all matching items from inventory
            for (int i = 0; i < player.getInventory().getContainerSize(); i++)
            {
                ItemStack invStack = player.getInventory().getItem(i);
                if (!invStack.isEmpty() && itemHandler.isItemValid(0, invStack))
                {
                    ItemStack remainder = itemHandler.insertItem(0, invStack, false);
                    player.getInventory().setItem(i, remainder);
                }
            }
        }
        INTERACTION_LOGGER.put(player.getUUID(), currentTime);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "item_controller", 5, state -> {

            state.setControllerSpeed(((float) this.getItemsStored() / DEFAULT_ITEM_CAPACITY) * 8.0f);

            if (this.getItemsStored() > 0) {
                return state.setAndContinue(RawAnimation.begin().thenPlay("animation.itemrack.startup").thenLoop("animation.itemrack.rotate"));
            }

            return state.setAndContinue(RawAnimation.begin().thenPlay("animation.itemrack.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }

    @Override
    public double getTick(Object object) {
        return RenderUtil.getCurrentTick();
    }
}
