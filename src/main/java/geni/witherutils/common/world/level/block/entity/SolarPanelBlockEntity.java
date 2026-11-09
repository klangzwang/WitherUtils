package geni.witherutils.common.world.level.block.entity;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.common.registration.impl.TileEntityTypeRegistryObject;
import geni.withercore.common.util.BlockEntityUtil;
import geni.witherutils.common.config.common.SolarConfig;
import geni.withercore.init.WUTBlockEntities;
import geni.witherutils.common.world.level.block.SolarPanelBlock;
import geni.witherutils.common.world.level.block.solar.ISolarPanelNetwork;
import geni.witherutils.common.world.level.block.solar.NoSolarPanelNetwork;
import geni.witherutils.common.world.level.block.solar.SolarPanelNetwork;
import geni.witherutils.common.world.level.block.solar.SolarType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class SolarPanelBlockEntity extends WitherMachineBlockEntity {

	public static class Basic extends SolarPanelBlockEntity
    {
	    private final EnergyStorage energyStorage = new EnergyStorage(SolarConfig.SOLARBASICINPUTRF.get(), SolarConfig.SOLARBASICOUTPUTRF.get());
		
		public Basic(BlockPos worldPosition, BlockState blockState)
		{
			super(WUTBlockEntities.SOLARBASIC_BE, worldPosition, blockState);
		}
		
	    @Override
	    public IEnergyStorage getEnergyHandler(@Nullable Direction dir)
	    {
	        return dir != Direction.DOWN ? null : energyStorage;
	    }
    }
    public static class Advanced extends SolarPanelBlockEntity
    {
	    private final EnergyStorage energyStorage = new EnergyStorage(SolarConfig.SOLARADVINPUTRF.get(), SolarConfig.SOLARADVOUTPUTRF.get());
		
		public Advanced(BlockPos worldPosition, BlockState blockState)
		{
			super(WUTBlockEntities.SOLARADV_BE, worldPosition, blockState);
		}
		
	    @Override
	    public IEnergyStorage getEnergyHandler(@Nullable Direction dir)
	    {
	        return dir != Direction.DOWN ? null : energyStorage;
	    }
    }
    public static class Ultra extends SolarPanelBlockEntity
    {
	    private final EnergyStorage energyStorage = new EnergyStorage(SolarConfig.SOLARULTRAINPUTRF.get(), SolarConfig.SOLARULTRAOUTPUTRF.get());
		
		public Ultra(BlockPos worldPosition, BlockState blockState)
		{
			super(WUTBlockEntities.SOLARULTRA_BE, worldPosition, blockState);
		}
		
	    @Override
	    public IEnergyStorage getEnergyHandler(@Nullable Direction dir)
	    {
	        return dir != Direction.DOWN ? null : energyStorage;
	    }
    }
    
    public ISolarPanelNetwork network = NoSolarPanelNetwork.INSTANCE;
	private int count = 1;

	public SolarPanelBlockEntity(TileEntityTypeRegistryObject<?> type, BlockPos worldPosition, BlockState blockState)
	{
		super(type, worldPosition, blockState);
	}

    @Override
    public boolean hasEnergyCapability()
    {
        return true;
    }

	private float renderAlpha = 0.0f;

	public float getRenderAlpha() {
		return this.renderAlpha;
	}

	@Override
	public void clientTick() {
		super.clientTick();
		float targetAlpha = calculateLightRatio(level);
		this.renderAlpha = Mth.lerp(0.05f, this.renderAlpha, targetAlpha);
	}

    @Override
    public void serverTick()
    {
        super.serverTick();

//        Vec3 startVector = getBlockPos().getCenter();
//        Vec3 endVector = startVector.add(new Vec3(90, Direction.UP.toYRot(), 0)).scale(1);
//
//        float f = 90 * (float) (Math.PI / 180.0);
//        float f1 = -Direction.UP.toYRot() * (float) (Math.PI / 180.0);
//        float f2 = Mth.cos(f1);
//        float f3 = Mth.sin(f1);
//        float f4 = Mth.cos(f);
//        float f5 = Mth.sin(f);
//        
//        Vec3 viewVector = new Vec3((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
//
//        BlockHitResult hitResult = level.clip(new ClipContext(startVector, endVector, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, CollisionContext.empty()));
//        if(hitResult != null && hitResult.getType() == Type.BLOCK)
//        {
//			BlockPos pos = hitResult.getBlockPos();
//			
//
//        }
//        
//		int theta = ((((int)(level.getSunAngle(1f) * (180.0/Math.PI)))+90) % 360);
//		int e = 2;
//		
//		if(theta > 340)      e = 2;
//		else if(theta <  45) e = 0;
//		else if(theta <  80) e = 1;
//		else if(theta < 100) e = 2;
//		else if(theta < 135) e = 3;
//		else if(theta < 190) e = 4;
//		
//		final double eff = (1.0-((level.getRainLevel(1f)*0.6)+(level.getThunderLevel(1f)*0.3)));
//		final double ll = ((double)(level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(getBlockPos())))/15;
//		final double rf = Math.sin((Math.PI/2) * Math.sqrt(((double)(((theta<0)||(theta>180))?(0):((theta>90)?(180-theta):(theta))))/90));

	    if(!network.isValid())
	    {
	        SolarPanelNetwork.build(this);
	    }

	    IEnergyStorage receptor = getNeighbouringCapability(Capabilities.EnergyStorage.BLOCK, Direction.DOWN);
	    if(receptor != null)
	    {
	    	int canTransmit = network.getEnergyAvailableThisTick();
	    	if(canTransmit > 0)
	    	{
	    		receptor.receiveEnergy(network.getEnergyAvailableThisTick() + 1, false);
	    	}
	    }
	}
    
	public boolean isGenerating()
	{
		return calculateLightRatio(level) > 0;
	}

	public int getGenerationRate()
	{
		return getEnergyPerTick();
	}
	
	/*
	 * 
	 * NETWORK
	 * 
	 */
	public void setNetwork(ISolarPanelNetwork network)
	{
		this.network = network;
	}
	
	int getEnergyPerTick()
	{
		return getEnergyPerTick(level, worldPosition);
	}
	
	public static int getEnergyPerTick(@Nonnull Level world, @Nonnull BlockPos pos)
	{
		final BlockState state = world.getBlockState(pos);
		if(state.getBlock() instanceof SolarPanelBlock solar)
		{
			if(solar.getType() == SolarType.ADVANCED)
				return SolarConfig.SOLARADVINPUTRF.get();
			else if(solar.getType() == SolarType.ULTRA)
				return SolarConfig.SOLARULTRAINPUTRF.get();
			return SolarConfig.SOLARBASICINPUTRF.get();
		}
		else
		{
			return -1;
		}
	}
	
	public boolean isSolarPowered(@Nonnull Level world, BlockPos pos)
	{
		return world.canSeeSkyFromBelowWater(pos);
	}
	
	public float calculateLocalLightRatio(@Nonnull Level level, BlockPos pos, float baseRatio)
	{
		float ratio = isSolarPowered(level, pos) ? baseRatio : 0;
		return ratio;
	}
	
	public float calculateLightRatio(@Nonnull Level level)
	{
		int lightValue = level.getLightEngine().getLayerListener(LightLayer.SKY).getLightValue(getBlockPos());
		float sunAngle = level.getSunAngle(1.0F);
		if(sunAngle < (float) Math.PI)
		{
			sunAngle += (0.0F - sunAngle) * 0.2F;
		}
		else
		{
			sunAngle += (((float) Math.PI * 2F) - sunAngle) * 0.2F;
		}
		lightValue = Math.round(lightValue * Mth.cos(sunAngle));
		lightValue = Mth.clamp(lightValue, 0, 15);

		return lightValue / 15f;
	}

	/**
	 * 
	 * GET/SET COUNT
	 * 
	 */
    @Override
    public void onLoad()
    {
        BlockEntityUtil.sendUpdatePacket(this);
    }
    public void setCount(int count)
    {
        this.count = count;
        BlockEntityUtil.sendUpdatePacket(this);
    }
    public int getCount()
    {
        return this.count;
    }
    public void updateCount()
    {
        Set<SolarPanelBlockEntity> panels = new HashSet<>();
        this.isSolarPanel(panels, this.worldPosition);
        panels.forEach(panel -> panel.setCount(panels.size()));
    }
    private void isSolarPanel(Set<SolarPanelBlockEntity> panels, BlockPos pos)
    {
        if(this.level == null)
            return;

        BlockEntity tileEntity = this.level.getBlockEntity(pos);
        if(tileEntity instanceof SolarPanelBlockEntity)
        {
            if(panels.contains(tileEntity))
                return;

            panels.add((SolarPanelBlockEntity) tileEntity);
            this.isSolarPanel(panels, pos.relative(Direction.NORTH));
            this.isSolarPanel(panels, pos.relative(Direction.EAST));
            this.isSolarPanel(panels, pos.relative(Direction.SOUTH));
            this.isSolarPanel(panels, pos.relative(Direction.WEST));
        }
    }
    
    /**
     * 
     * TAG
     * 
     */
    @Override
    public void loadAdditional(CompoundTag pTag, Provider lookupProvider)
    {
    	super.loadAdditional(pTag, lookupProvider);
        this.readData(pTag);
    }
    @Override
    public void saveAdditional(CompoundTag pTag, Provider lookupProvider)
    {
    	super.saveAdditional(pTag, lookupProvider);
        this.writeData(pTag);
    }
    @Override
    public CompoundTag getUpdateTag(Provider registries)
    {
        return this.saveWithFullMetadata(registries);
    }
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, Provider lookupProvider)
    {
        CompoundTag compound = pkt.getTag();
        this.readData(compound);
    }
    private void readData(CompoundTag compound)
    {
        if(compound.contains("Count", Tag.TAG_INT))
        {
            this.count = compound.getInt("Count");
        }
    }
    private CompoundTag writeData(CompoundTag compound)
    {
        compound.putInt("Count", this.count);
        return compound;
    }
    @Nonnull
    @Override
    public ModelData getModelData()
    {
        return super.getModelData();
    }
}
