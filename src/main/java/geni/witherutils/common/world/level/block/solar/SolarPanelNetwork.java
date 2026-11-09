package geni.witherutils.common.world.level.block.solar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import geni.withercore.common.helper.TickHelper;
import geni.witherutils.common.world.level.block.SolarPanelBlock;
import geni.witherutils.common.world.level.block.entity.SolarPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SolarPanelNetwork implements ISolarPanelNetwork {

	public static final @Nonnull List<Direction> FACING_HORIZONTAL = new ArrayList<Direction>();

	private final @Nonnull Set<BlockPos> panels = new HashSet<BlockPos>();
	private final @Nonnull Level world;
	private boolean valid = false;

	private int energyMaxPerTick = 0;
	private int energyAvailablePerTick = 0;
	private int energyAvailableThisTick = 0;
	private long lastTick = -1, nextCollectTick = 0;

	public static void build(@Nonnull SolarPanelBlockEntity panel)
	{
		new SolarPanelNetwork(panel).isValid();
	}

	private SolarPanelNetwork(@Nonnull SolarPanelBlockEntity panel)
	{
		this.world = panel.getLevel();
		this.valid = true;
		panels.add(panel.getBlockPos().immutable());
		panel.setNetwork(this);
		nextCollectTick = 0;
		cleanupMemberlist();
	}

	@Override
	public @Nonnull Set<BlockPos> getPanels() {
		return panels;
	}

	void cleanupMemberlist() {
		if (!panels.isEmpty()) {
			Iterator<BlockPos> iterator = panels.iterator();
			Set<BlockPos> candidates = new HashSet<BlockPos>();
			while (iterator.hasNext()) {
				BlockPos panel = iterator.next();
				if (panel != null && world.isLoaded(panel))
				{
					BlockEntity tileEntity = world.getBlockEntity(panel);
					if (tileEntity instanceof SolarPanelBlockEntity && tileEntity.hasLevel()) {
						if (((SolarPanelBlockEntity) tileEntity).network == this) {
							for (Iterator<Direction> facings = Direction.Plane.HORIZONTAL.iterator(); facings
									.hasNext();) {
								final BlockPos neighbor = panel.relative(facings.next());
								if (!panels.contains(neighbor) && world.isLoaded(neighbor)) {
									candidates.add(neighbor);
								}
							}
							continue;
						}
					}
				}
				iterator.remove();
			}
			while (!candidates.isEmpty()) {
				List<BlockPos> candidateList = new ArrayList<BlockPos>(candidates);
				for (Iterator<BlockPos> candidateItr = candidateList.iterator(); candidateItr.hasNext();) {
					BlockPos candidate = candidateItr.next();
					if (!panels.contains(candidate) && canConnect(candidate)) {
						BlockEntity tileEntity = world.getBlockEntity(candidate);
						if (tileEntity instanceof SolarPanelBlockEntity && tileEntity.hasLevel()) {
							panels.add(candidate.immutable());
							final ISolarPanelNetwork otherNetwork = ((SolarPanelBlockEntity) tileEntity).network;
							if (otherNetwork != this) {
								((SolarPanelBlockEntity) tileEntity).setNetwork(this);
								for (BlockPos other : otherNetwork.getPanels()) {
									if (other != null && !panels.contains(other) && world.isLoaded(other)) {
										candidates.add(other);
									}
								}
								otherNetwork.destroyNetwork();
								for (Iterator<Direction> facings = Direction.Plane.HORIZONTAL.iterator(); facings.hasNext();) {
									final BlockPos neighbor = candidate.relative(facings.next());
									if (!panels.contains(neighbor) && world.isLoaded(neighbor)) {
										candidates.add(neighbor);
									}
								}
							}
						}
					}
					candidates.remove(candidate);
				}
			}
		}
		if (panels.isEmpty()) {
			destroyNetwork();
		}
	}

	private boolean canConnect(@Nonnull BlockPos other)
	{
		if(panels.isEmpty())
		{
			return true;
		}
		BlockState otherState = world.getBlockState(other);
		if(otherState.getBlock() instanceof SolarPanelBlock)
		{
			for(BlockPos panel : panels)
			{
				if(panel != null && world.isLoaded(panel))
				{
					BlockState state = world.getBlockState(panel);
					if(state.getBlock() instanceof SolarPanelBlock)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void destroyNetwork()
	{
		energyMaxPerTick = energyAvailablePerTick = energyAvailableThisTick = 0;
		nextCollectTick = Long.MAX_VALUE;
		panels.clear();
		valid = false;
	}

	@Override
	public boolean isValid()
	{
		return valid;
	}

	private int energyMaxPerTickPerPanel = -1;

	@SuppressWarnings("static-access")
	private void updateEnergy()
	{
		long tick = TickHelper.getServerTickCount();
		if (tick != lastTick)
		{
			lastTick = tick;
			if (tick > nextCollectTick)
			{
				nextCollectTick = tick + 10;
				energyMaxPerTick = energyAvailablePerTick = 0;

				for (BlockPos panel : panels)
				{
					BlockEntity tileEntity = world.getBlockEntity(panel);
					if(tileEntity instanceof SolarPanelBlockEntity solar && tileEntity.hasLevel())
					{
						float lightRatio = solar.calculateLightRatio(world);
						if (panel != null && world.isLoaded(panel))
						{
							if (energyMaxPerTickPerPanel < 0)
							{
								energyMaxPerTickPerPanel = solar.getEnergyPerTick(world, panel);
								if (energyMaxPerTickPerPanel < 0)
								{
									destroyNetwork();
									return;
								}
							}
							energyMaxPerTick += energyMaxPerTickPerPanel;
							energyAvailablePerTick += Mth.floor(energyMaxPerTickPerPanel
									* solar.calculateLocalLightRatio(world, panel, lightRatio));
						}
					}
				}
			}
			energyAvailableThisTick = energyAvailablePerTick;
		}
		
//    	System.out.println(this.toString());
	}

	@Override
	public void extractEnergy(int maxExtract)
	{
		energyAvailableThisTick = Math.max(energyAvailableThisTick - maxExtract, 0);
	}

	@Override
	public int getEnergyAvailableThisTick()
	{
		updateEnergy();
		return energyAvailableThisTick;
	}

	@Override
	public int getEnergyAvailablePerTick()
	{
		updateEnergy();
		return energyAvailablePerTick;
	}

	@Override
	public int getEnergyMaxPerTick()
	{
		updateEnergy();
		return energyMaxPerTick;
	}

	@Override
	public String toString()
	{
		return "SolarPanelNetwork [panels=" + panels.size() + ", valid=" + valid + ", energyMaxPerTick="
				+ energyMaxPerTick + ", energyAvailablePerTick=" + energyAvailablePerTick + ", energyAvailableThisTick="
				+ energyAvailableThisTick + ", lastTick=" + lastTick + ", nextCollectTick=" + nextCollectTick
				+ ", rfMax=" + energyMaxPerTickPerPanel + "]";
	}
}
