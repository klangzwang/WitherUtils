package geni.witherutils.base.common.block.generator.solar;

import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;

public interface ISolarPanelNetwork {

	boolean isValid();

	void extractEnergy(int maxExtract);

	int getEnergyAvailableThisTick();

	int getEnergyAvailablePerTick();

	int getEnergyMaxPerTick();

	void destroyNetwork();

	@Nonnull
	Set<BlockPos> getPanels();
}