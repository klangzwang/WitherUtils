package geni.witherutils.common.world.level.block.rack.controller.fluid;

import java.util.ArrayList;
import java.util.List;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.witherapi.block.WitherEntityBlock;
import geni.withercore.common.util.ItemStackUtil;
import geni.witherapi.block.IBlockMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public class ControllerFluidBlock extends WitherAbstractBlock implements IBlockMulti, WitherEntityBlock {
	
    public ControllerFluidBlock(BlockBehaviour.Properties props)
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
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit)
	{
		if (level.getBlockEntity(pos) instanceof ControllerFluidBlockEntity controller)
		{
			if (controller.onRightClicked(player, hand))
			{
				return ItemInteractionResult.sidedSuccess(level.isClientSide);
			}
		}
		return super.useItemOn(stack, state, level, pos, player, hand, hit);
	}

	@Override
	public void setPlacedBy(Level world, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack)
	{
		super.setPlacedBy(world, pos, state, placer, stack);
		BlockEntity container = world.getBlockEntity(pos);
		if (container instanceof ControllerFluidBlockEntity controller)
		{
			IFluidHandler itemFluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
			if (itemFluidHandler != null)
			{
				FluidStack fluidInItem = itemFluidHandler.getFluidInTank(0);
				if (!fluidInItem.isEmpty())
				{
					controller.getFluidTank().fill(fluidInItem, IFluidHandler.FluidAction.EXECUTE);
				}
			}
			else
			{
				CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
				if (customData.contains("fluid"))
				{
					controller.getFluidTank().readFromNBT(world.registryAccess(), customData.copyTag().getCompound("fluid"));
				}
			}
		}
		world.setBlockAndUpdate(pos, state);
	}

	@Override
	public void playerDestroy(Level world, Player player, BlockPos pos, BlockState state, BlockEntity ent, ItemStack stackTool)
	{
		super.playerDestroy(world, player, pos, state, ent, stackTool);
		ItemStack tankStack = new ItemStack(this);
		if (ent instanceof ControllerFluidBlockEntity controller)
		{
			FluidStack fs = controller.getFluidTank().getFluid();
			if (!fs.isEmpty())
			{
				IFluidHandler itemHandler = tankStack.getCapability(Capabilities.FluidHandler.ITEM);
				if (itemHandler != null)
				{
					itemHandler.fill(fs, IFluidHandler.FluidAction.EXECUTE);
				}
				else
				{
					CustomData.update(DataComponents.CUSTOM_DATA, tankStack, tag -> {
						tag.put("fluid", controller.getFluidTank().writeToNBT(world.registryAccess()));
					});
				}
			}
		}
        ItemStackUtil.dropItemStackMotionless(world, pos, tankStack);
	}
    
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new ControllerFluidBlockEntity(pos, state);
	}
}
