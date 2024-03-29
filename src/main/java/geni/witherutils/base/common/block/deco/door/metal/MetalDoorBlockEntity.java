package geni.witherutils.base.common.block.deco.door.metal;

import geni.witherutils.base.common.base.IInteractBlockEntity;
import geni.witherutils.base.common.base.WitherMachineBlockEntity;
import geni.witherutils.base.common.init.WUTEntities;
import geni.witherutils.base.common.init.WUTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MetalDoorBlockEntity extends WitherMachineBlockEntity implements IInteractBlockEntity {

	private boolean powered = false;
    private float maxProgress = 20.0f;
    private float slideProgress;
    private float prevSlideProgress;

	public MetalDoorBlockEntity(BlockPos pos, BlockState state)
	{
		super(WUTEntities.METALDOOR.get(), pos, state);
	}

	@Override
	public void serverTick()
	{
		super.serverTick();
	}

	@Override
	public void clientTick()
	{
		super.clientTick();
		
		boolean flag = level.hasNeighborSignal(worldPosition) || level.hasNeighborSignal(worldPosition.above());
		powered = level.getBlockState(worldPosition).getValue(MetalDoorBlock.LIT) || flag;
		
        prevSlideProgress = slideProgress;
        if(powered)
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
	}
	
    @Override
    public InteractionResult onBlockUse(BlockState state, Player player, InteractionHand hand, BlockHitResult hit)
    {
        if(player.isShiftKeyDown())
        	return InteractionResult.FAIL;
        
        final boolean open = !state.getValue(MetalDoorBlock.LIT);
        
        level.setBlock(worldPosition, state.setValue(MetalDoorBlock.LIT, open), 2 | 8 | 16);
        level.playSound(null, worldPosition, open ? WUTSounds.HANGARDOOROPEN.get() : WUTSounds.HANGARDOORCLOSE.get(), SoundSource.BLOCKS, 0.6f, 1.4f);

        return InteractionResult.SUCCESS;
    }
    
    public float getSlideProgress(float partialTicks)
    {
        float partialSlideProgress = prevSlideProgress + (slideProgress - prevSlideProgress) * partialTicks;
        float normalProgress = partialSlideProgress / (float) maxProgress;
        return 0.815F * (1.0F - ((float) Math.sin(Math.toRadians(90.0 + 180.0 * normalProgress)) / 2.0F + 0.5F));
    }

    public boolean isDoorWideOpen()
    {
    	if(slideProgress >= maxProgress)
    		return true;
    	else
    		return false;
    }
    
    public boolean isPowered()
    {
        return powered;
    }
    public void setPowered(boolean powered)
    {
        this.powered = powered;
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getLevel().getBlockState(this.getBlockPos()), this.getLevel().getBlockState(this.getBlockPos()), 3);
    }

	@Override
	public void load(CompoundTag tag)
	{
		powered = tag.getBoolean("powered");
		super.load(tag);
	}
	@Override
	public void saveAdditional(CompoundTag tag)
	{
		tag.putBoolean("powered", this.powered);
		super.saveAdditional(tag);
	}
}
