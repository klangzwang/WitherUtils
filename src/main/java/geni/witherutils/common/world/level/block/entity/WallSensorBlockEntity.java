package geni.witherutils.common.world.level.block.entity;

import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.init.WUTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class WallSensorBlockEntity extends WitherMachineBlockEntity {

	public WallSensorBlockEntity(BlockPos pos, BlockState state)
	{
		super(WUTBlockEntities.WALLSENSOR, pos, state);
	}
}
