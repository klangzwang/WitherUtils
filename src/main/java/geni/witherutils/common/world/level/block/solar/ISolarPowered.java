package geni.witherutils.common.world.level.block.solar;

public interface ISolarPowered {

	SolarType getType();
	
	int getProduction(SolarType type);
}
