package geni.witherutils.common.world.level.block.rack.controller.energy;

import java.util.ArrayList;
import java.util.List;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.withercore.common.util.ItemStackUtil;
import geni.witherapi.block.IBlockMulti;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class ControllerEnergyBlock extends WitherAbstractBlock implements IBlockMulti, WitherEntityBlock {

    public ControllerEnergyBlock(BlockBehaviour.Properties props)
    {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(false)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context)
    {
        return Shapes.or(
                Block.box(2, 2, 2, 14, 14, 14),
                Block.box(5, 5, 0, 11, 11, 2),
                Block.box(0, 5, 5, 2, 11, 11),
                Block.box(5, 14, 5, 11, 16, 11),
                Block.box(5, 5, 14, 11, 11, 16),
                Block.box(5, 0, 5, 11, 2, 11),
                Block.box(14, 5, 5, 16, 11, 11)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACING, LIT);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        return new ArrayList<>();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, placer, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ControllerEnergyBlockEntity controller)
        {
            IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemEnergy != null)
            {
                controller.setEnergyStored(itemEnergy.getEnergyStored());
            }
            else
            {
                CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
                if (customData.contains("energy"))
                {
                    controller.setEnergyStored(customData.copyTag().getInt("energy"));
                }
            }
        }
        level.setBlockAndUpdate(pos, state);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity ent, ItemStack stackTool)
    {
        super.playerDestroy(world, player, pos, state, ent, stackTool);

        ItemStack energyStack = new ItemStack(this);
        if (ent instanceof ControllerEnergyBlockEntity controller)
        {
            int stored = controller.getEnergyStored();
            if (stored > 0)
            {
                IEnergyStorage itemHandler = energyStack.getCapability(Capabilities.EnergyStorage.ITEM);
                if (itemHandler != null)
                {
                    itemHandler.receiveEnergy(stored, false);
                }
                else
                {
                    CustomData.update(DataComponents.CUSTOM_DATA, energyStack, tag -> {
                        tag.putInt("energy", stored);
                        tag.putInt("energystored", stored);
                    });
                }
            }
        }
        ItemStackUtil.dropItemStackMotionless(world, pos, energyStack);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (level.getBlockEntity(pos) instanceof ControllerEnergyBlockEntity controller)
        {
            IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (itemEnergy != null)
            {
                if (!level.isClientSide)
                {
                    // Transfer energy between held item and controller
                    if (player.isShiftKeyDown())
                    {
                        // Shift-click: extract from controller into item
                        int needed = itemEnergy.getMaxEnergyStored() - itemEnergy.getEnergyStored();
                        int toExtract = Math.min(needed, ControllerEnergyBlockEntity.MAX_TRANSFER);
                        int extracted = controller.getEnergyStorage().extractEnergy(toExtract, false);
                        itemEnergy.receiveEnergy(extracted, false);
                    }
                    else
                    {
                        // Normal click: charge controller from item
                        int available = itemEnergy.extractEnergy(ControllerEnergyBlockEntity.MAX_TRANSFER, true);
                        int received = controller.getEnergyStorage().receiveEnergy(available, false);
                        itemEnergy.extractEnergy(received, false);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (level.getBlockEntity(pos) instanceof ControllerEnergyBlockEntity controller)
        {
            if (!level.isClientSide)
            {
                player.displayClientMessage(
                        Component.literal(ChatFormatting.GOLD + "RACK Energy: " + ChatFormatting.WHITE +
                                ControllerEnergyBlockEntity.getFormatedFullNumber(controller.getEnergyStored()) + " / " +
                                ControllerEnergyBlockEntity.getFormatedFullNumber(ControllerEnergyBlockEntity.ENERGY_CAPACITY) +
                                ChatFormatting.RED + " RF"),
                        true
                );
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new ControllerEnergyBlockEntity(pos, state);
    }
}
