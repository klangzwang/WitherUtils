package geni.witherutils.common.world.level.block;

import geni.withercore.common.world.level.block.WitherAbstractBlock;
import geni.withercore.common.util.SoundUtil;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

import geni.witherapi.block.WitherEntityBlock;
import geni.withercore.init.WUTBlocks;
import geni.witherutils.common.world.level.block.entity.CreativeGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

public class CreativeGeneratorBlock extends WitherAbstractBlock implements WitherEntityBlock {

	public CreativeGeneratorBlock()
    {
        super(WUTBlocks.defaultProps());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(false)));
	}
	
    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
    {
    	builder.add(FACING, LIT);
    }
    
	@Override
	public CreativeGeneratorBlockItem getBlockItem(Item.Properties properties)
	{
		return new CreativeGeneratorBlockItem(this, properties.stacksTo(1));
	}

    @Override
    public ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult)
    {
        BlockEntity te = pLevel.getBlockEntity(pPos);

        if (pPlayer.isShiftKeyDown() || !pLevel.getBlockState(pPos.above()).isAir())
            return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
        else if (te instanceof CreativeGeneratorBlockEntity generator)
        {
            if (!pLevel.isClientSide)
            {
                if (generator.getInventory().getStackInSlot(1).isEmpty())
                {
                    if (pPlayer.getItemInHand(pHand).getItem() instanceof IEnergyStorage)
                    {
                        ItemStack excess = generator.getInventory().insertItem(1, pPlayer.getItemInHand(pHand), false);
                        pPlayer.setItemInHand(pHand, excess);
                    }
                }
                else
                {
                    ItemStack stack = generator.getInventory().extractItem(1, 64, false);
                    ItemEntity entityItem = new ItemEntity(pLevel, pPos.getX() + 0.5, pPos.getY() + 0.6, pPos.getZ() + 0.5, stack);
                    entityItem.setDeltaMovement(0, 0, 0);
                    entityItem.setPickUpDelay(20);
                    pLevel.addFreshEntity(entityItem);
                }
                SoundUtil.playSoundDistrib(pLevel, pPos, SoundEvents.PLAYER_ATTACK_STRONG, 0.75f, 1.0F, false, true);
            }
        }
        return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pHitResult);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState)
    {
        return new CreativeGeneratorBlockEntity(pPos, pState);
    }
}
