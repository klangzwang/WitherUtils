package geni.witherutils.common.world.level.block.rack.terminal;

import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.state.BlockStateProperties;
import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.witherapi.block.IBlockMulti;
import geni.witherutils.common.world.level.block.rack.controller.energy.ControllerEnergyBlockEntity;
import geni.witherutils.common.world.level.block.rack.controller.fluid.ControllerFluidBlockEntity;
import geni.witherutils.common.world.level.block.rack.controller.item.ControllerItemBlockEntity;
import geni.withercore.common.util.BlockEntityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class TerminalBlock extends WitherAbstractBlock implements IBlockMulti, WitherEntityBlock {
	
	public static final DirectionProperty FACING = net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
    public static final BooleanProperty FORMED = BlockStateProperties.FORMED;
    
    public TerminalBlock(BlockBehaviour.Properties props)
	{
		super(props);
        registerDefaultState(defaultBlockState().setValue(FORMED, false));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx)
    {
        BlockState state = super.getStateForPlacement(ctx);
        if (state != null)
        {
            Direction f = ctx.getNearestLookingDirection();
            state = state.setValue(FACING, reversePlacementRotation() ? f.getOpposite() : f);
        }
        return state;
    }
    protected boolean reversePlacementRotation()
    {
        return false;
    }

	@Override
	public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos)
	{
        Block block = state.getBlock();
        if(block != this)
        {
            return block.getLightEmission(state, level, pos);
        }
        return state.getValue(FORMED) ? 9 : 0;
	}
    
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity par5EntityLiving, ItemStack iStack)
    {
        super.setPlacedBy(level, pos, state, par5EntityLiving, iStack);
        if (!level.isClientSide && TerminalBlockEntity.checkForming(level, pos)) {};
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(BlockStateProperties.FORMED, FACING);
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

	@Override
    public BlockState mirror(BlockState state, Mirror mirrorIn)
    {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    protected boolean rotateForgeWay()
    {
        return true;
    }
    protected boolean rotateCustom(Level world, BlockPos pos, BlockState state, Direction side)
    {
        return false;
    }
    
	@Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
    {
        if (state.getBlock() != newState.getBlock())
        {
            invalidateMultiBlock(world, pos);
        }
        super.onRemove(state, world, pos, newState, isMoving);
    }

    private void invalidateMultiBlock(Level world, BlockPos pos)
    {
        if (!world.isClientSide)
        {
            BlockEntityUtil.getBlockEntityAt(world, pos, TerminalBlockEntity.class).ifPresent(teTerminal -> {
            	
                if (teTerminal.multiBlockSize > 0)
                {
                	teTerminal.onMultiBlockBreak();
                }
                else if (!teTerminal.accessoryTerminals.isEmpty())
                {
                	teTerminal.accessoryTerminals.stream()
                            .filter(terminal -> terminal.multiBlockSize > 0)
                            .findFirst()
                            .ifPresent(TerminalBlockEntity::onMultiBlockBreak);
                }
            });
        }
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player)
    {
        if (state.getValue(FORMED))
        {
            BlockEntity te = level.getBlockEntity(pos.relative(state.getValue(FACING)));
            if (te instanceof ControllerItemBlockEntity itemController)
            {
                itemController.onLeftClicked(player);
            }
        }
        super.attack(state, level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (state.getValue(FORMED))
        {
            BlockEntity te = level.getBlockEntity(pos.relative(state.getValue(FACING)));
            if (te instanceof ControllerItemBlockEntity itemController)
            {
                if (!level.isClientSide)
                {
                    itemController.onRightClicked(player, hand);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            else if (te instanceof ControllerFluidBlockEntity fluidController)
            {
                if (fluidController.onRightClicked(player, hand))
                {
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
            else if (te instanceof ControllerEnergyBlockEntity energyController)
            {
                IEnergyStorage itemEnergy = stack.getCapability(Capabilities.EnergyStorage.ITEM);
                if (itemEnergy != null)
                {
                    if (!level.isClientSide)
                    {
                        if (player.isShiftKeyDown())
                        {
                            int needed = itemEnergy.getMaxEnergyStored() - itemEnergy.getEnergyStored();
                            int toExtract = Math.min(needed, ControllerEnergyBlockEntity.MAX_TRANSFER);
                            int extracted = energyController.getEnergyStorage().extractEnergy(toExtract, false);
                            itemEnergy.receiveEnergy(extracted, false);
                        }
                        else
                        {
                            int available = itemEnergy.extractEnergy(ControllerEnergyBlockEntity.MAX_TRANSFER, true);
                            int received = energyController.getEnergyStorage().receiveEnergy(available, false);
                            itemEnergy.extractEnergy(received, false);
                        }
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (state.getValue(FORMED))
        {
            BlockEntity te = level.getBlockEntity(pos.relative(state.getValue(FACING)));
            if (te instanceof ControllerItemBlockEntity itemController)
            {
                if (!level.isClientSide)
                {
                    itemController.onRightClicked(player, InteractionHand.MAIN_HAND);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            else if (te instanceof ControllerEnergyBlockEntity energyController)
            {
                if (!level.isClientSide)
                {
                    player.displayClientMessage(
                            Component.literal(ChatFormatting.GOLD + "RACK Energy: " + ChatFormatting.WHITE +
                                    ControllerEnergyBlockEntity.getFormatedFullNumber(energyController.getEnergyStored()) + " / " +
                                    ControllerEnergyBlockEntity.getFormatedFullNumber(ControllerEnergyBlockEntity.ENERGY_CAPACITY) +
                                    ChatFormatting.RED + " RF"),
                            true
                    );
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }
    
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new TerminalBlockEntity(pos, state);
    }
}