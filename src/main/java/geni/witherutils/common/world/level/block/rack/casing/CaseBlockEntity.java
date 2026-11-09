package geni.witherutils.common.world.level.block.rack.casing;

import geni.withercore.common.world.level.block.state.MultiBlockState;
import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.init.WUTBlockEntities;
import geni.withercore.common.registration.impl.TileEntityTypeRegistryObject;
import geni.witherutils.WitherUtils;
import geni.witherutils.common.world.level.block.rack.terminal.TerminalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import static geni.withercore.common.world.level.block.state.BlockStateProperties.MBSTATE;

public class CaseBlockEntity extends WitherMachineBlockEntity {

    private TerminalBlockEntity teTerminal;
    private BlockPos terminalPos;

    public CaseBlockEntity(BlockPos pos, BlockState state)
    {
        super(WUTBlockEntities.RACK_CASE, pos, state);
    }
    protected CaseBlockEntity(TileEntityTypeRegistryObject<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }
    
    public TerminalBlockEntity getTerminal()
    {
    	return teTerminal;
    }
    
    @Nonnull
    public Level nonNullLevel()
    {
        return Objects.requireNonNull(super.getLevel());
    }
    
    public TerminalBlockEntity getPrimaryTerminal()
    {
        if (teTerminal == null && terminalPos != null)
        {
            setPrimaryTerminal(nonNullLevel().getBlockEntity(terminalPos) instanceof TerminalBlockEntity v ? v : null);
        }
        return teTerminal;
    }

    public void setPrimaryTerminal(TerminalBlockEntity newTerminal)
    {
        boolean terminalChanging = teTerminal != newTerminal || newTerminal == null && terminalPos != null || newTerminal != null && terminalPos == null;
        terminalPos = newTerminal == null ? null : newTerminal.getBlockPos();
        
        if (terminalChanging && !nonNullLevel().isClientSide)
        {
        	teTerminal = newTerminal;
            CubicStateManager.addDeferredUpdate(this, teTerminal);
            setChanged();
        }
    }
    public void onBlockBreak()
    {
    	teTerminal = getPrimaryTerminal();
        if (teTerminal != null)
        {
        	teTerminal.onMultiBlockBreak();
        }
    }

    private void updateBlockState(TerminalBlockEntity terminal)
    {
        if (!this.isRemoved() && (terminal == null || !terminal.isRemoved()))
        {
            if (getBlockState().hasProperty(MBSTATE))
                nonNullLevel().setBlock(getBlockPos(), calcNewCubicState(terminal), Block.UPDATE_CLIENTS);
        }
    }
    
    private BlockState calcNewCubicState(TerminalBlockEntity terminal)
    {
        MultiBlockState cubicState = MultiBlockState.NONE;
        if (terminal != null && !terminal.isRemoved())
        {
            boolean xMin = getBlockPos().getX() == terminal.multiBlockX;
            boolean yMin = getBlockPos().getY() == terminal.multiBlockY;
            boolean zMin = getBlockPos().getZ() == terminal.multiBlockZ;
            boolean xMax = getBlockPos().getX() == terminal.multiBlockX + terminal.multiBlockSize - 1;
            boolean yMax = getBlockPos().getY() == terminal.multiBlockY + terminal.multiBlockSize - 1;
            boolean zMax = getBlockPos().getZ() == terminal.multiBlockZ + terminal.multiBlockSize - 1;

            // Corners
        	if(xMin && zMin && yMin)
        		cubicState = MultiBlockState.XMIN_ZMIN_LO;
        	else if(xMin && zMax && yMin)
        		cubicState = MultiBlockState.XMIN_ZMAX_LO;
        	else if(xMax && zMin && yMin)
        		cubicState = MultiBlockState.XMAX_ZMIN_LO;
        	else if(xMax && zMax && yMin)
        		cubicState = MultiBlockState.XMAX_ZMAX_LO;
        	else if(xMin && zMin && yMax)
        		cubicState = MultiBlockState.XMIN_ZMIN_HI;
        	else if(xMin && zMax && yMax)
        		cubicState = MultiBlockState.XMIN_ZMAX_HI;
        	else if(xMax && zMin && yMax)
        		cubicState = MultiBlockState.XMAX_ZMIN_HI;
        	else if(xMax && zMax && yMax)
        		cubicState = MultiBlockState.XMAX_ZMAX_HI;

            // Edges
        	else if (yMin && xMin || yMax && xMax || yMin && xMax || yMax && xMin)
            {
            	cubicState = MultiBlockState.XEDGE;
            }
            else if (yMin && zMin || yMax && zMax || yMin && zMax || yMax && zMin)
            {
            	cubicState = MultiBlockState.ZEDGE;
            }
            else if (!yMin && !yMax)
            {
            	if (xMin && zMin)
                	cubicState = MultiBlockState.XMIN_ZMIN_YEDGE;
            	if (xMin && zMax)
                	cubicState = MultiBlockState.XMIN_ZMAX_YEDGE;
            	if (xMax && zMin)
                	cubicState = MultiBlockState.XMAX_ZMIN_YEDGE;
            	if (xMax && zMax)
                	cubicState = MultiBlockState.XMAX_ZMAX_YEDGE;

            	if (!xMin && !xMax && !zMin && !zMax)
            	{
//            		cubicState = MultiBlockState.CONTROLLER;
            	}
            	else if (xMin && zMin || xMax && zMax || xMin && zMax || xMax && zMin)
                {
//                	cubicState = MultiBlockState.YEDGE;
                }
                else
                {
                	if (xMin)
                    	cubicState = MultiBlockState.XMIN_CENTER;
                	else if (zMin)
                    	cubicState = MultiBlockState.ZMIN_CENTER;
                	else if (xMax)
                    	cubicState = MultiBlockState.XMAX_CENTER;
                	else if (zMax)
                    	cubicState = MultiBlockState.ZMAX_CENTER;
                }
            }
            else
            {
            	if (yMin)
            		cubicState = MultiBlockState.YMIN_CENTER;
            	else if (yMax)
            		cubicState = MultiBlockState.YMAX_CENTER;
            }
        }
        return getBlockState().setValue(MBSTATE, cubicState);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);

        if (tag.getBoolean("noTerminal"))
        {
            terminalPos = null;
        }
        else if (tag.contains("termX"))
        {
            terminalPos = new BlockPos(tag.getInt("termX"), tag.getInt("termY"), tag.getInt("termZ"));
        }
        else if (tag.contains("terminalX"))
        {
            terminalPos = new BlockPos(tag.getInt("terminalX"), tag.getInt("terminalY"), tag.getInt("terminalZ"));
        }
        teTerminal = null;
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if (terminalPos == null)
        {
            tag.putBoolean("noTerminal", true);
        }
        else
        {
            tag.putInt("termX", terminalPos.getX());
            tag.putInt("termY", terminalPos.getY());
            tag.putInt("termZ", terminalPos.getZ());
        }
    }
    @EventBusSubscriber(modid = WitherUtils.MODID)
    public static class CubicStateManager
    {
        private static final Deque<CubicAndTerminal> todo = new ArrayDeque<>();

        @SubscribeEvent
        public static void onServerTick(ServerTickEvent.Post event)
        {
            while (!todo.isEmpty())
            {
                CubicAndTerminal element = todo.poll();
                if (element != null && element.cubic() != null)
                {
                    element.cubic().updateBlockState(element.terminal());
                }
            }
        }

        private static void addDeferredUpdate(CaseBlockEntity cubic, TerminalBlockEntity terminal)
        {
            todo.offer(new CubicAndTerminal(cubic, terminal));
        }

        private record CubicAndTerminal(CaseBlockEntity cubic, TerminalBlockEntity terminal)
        {
        }
    }
}
