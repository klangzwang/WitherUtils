package geni.witherutils.common.world.level.block.rack.controller.item;

import java.util.ArrayList;
import java.util.List;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.withercore.common.util.ItemStackUtil;
import geni.witherapi.block.IBlockMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.neoforged.neoforge.items.IItemHandler;

public class ControllerItemBlock extends WitherAbstractBlock implements IBlockMulti, WitherEntityBlock {
	
    public ControllerItemBlock(BlockBehaviour.Properties props)
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
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack)
    {
        super.setPlacedBy(level, pos, state, entity, stack);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ControllerItemBlockEntity controller)
        {
            IItemHandler stackHandler = stack.getCapability(Capabilities.ItemHandler.ITEM);
            if (stackHandler != null)
            {
                ItemStack storedStack = stackHandler.getStackInSlot(0);
                if (!storedStack.isEmpty())
                {
                    controller.setBlStack(storedStack.copyWithCount(1));
                    controller.setItemsStored(storedStack.getCount());
                }
            }
        }
        level.setBlockAndUpdate(pos, state);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player)
    {
        if (level.getBlockEntity(pos) instanceof ControllerItemBlockEntity controller)
        {
            controller.onLeftClicked(player);
        }
        super.attack(state, level, pos, player);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if (level.getBlockEntity(pos) instanceof ControllerItemBlockEntity controller)
        {
            if (!level.isClientSide)
            {
                controller.onRightClicked(player, hand);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (level.getBlockEntity(pos) instanceof ControllerItemBlockEntity controller)
        {
            if (!level.isClientSide)
            {
                controller.onRightClicked(player, InteractionHand.MAIN_HAND);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hit);
    }

    @Override
    public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity ent, ItemStack stackTool)
    {
        super.playerDestroy(world, player, pos, state, ent, stackTool);

        ItemStack ctrlStack = new ItemStack(this);
        if (ent instanceof ControllerItemBlockEntity controller)
        {
            IItemHandler handler = ctrlStack.getCapability(Capabilities.ItemHandler.ITEM);
            if (handler instanceof ControllerItemBlockItemHandlerCapability cap)
            {
                /*cap.setStack(controller.getBlStack());*/
                cap.setStored(controller.getItemsStored());
            }
            else
            {
                ControllerItemBlockItemHandlerCapability cap = new ControllerItemBlockItemHandlerCapability(ctrlStack, ControllerItemBlockEntity.ITEMCAPACITY);
                /*cap.setStack(controller.getBlStack());*/
                cap.setStored(controller.getItemsStored());
            }
        }
        ItemStackUtil.dropItemStackMotionless(world, pos, ctrlStack);
    }
    
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new ControllerItemBlockEntity(pos, state);
	}
}
