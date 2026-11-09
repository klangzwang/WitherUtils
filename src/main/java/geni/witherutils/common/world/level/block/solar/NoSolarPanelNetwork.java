package geni.witherutils.common.world.level.block.solar;

import java.util.Collections;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;

public class NoSolarPanelNetwork implements ISolarPanelNetwork {

	public static final @Nonnull ISolarPanelNetwork INSTANCE = new NoSolarPanelNetwork();

	private NoSolarPanelNetwork()
	{
	}

	@Override
	public boolean isValid()
	{
		return false;
	}

	@Override
	public void extractEnergy(int maxExtract)
	{
	}

	@Override
	public int getEnergyAvailableThisTick()
	{
		return 0;
	}

	@Override
	public int getEnergyAvailablePerTick()
	{
		return 0;
	}

	@Override
	public int getEnergyMaxPerTick()
	{
		return 0;
	}

	@Override
	public void destroyNetwork()
	{
	}

	@Override
	public @Nonnull Set<BlockPos> getPanels()
	{
		return Collections.emptySet();
	}
}
