package geni.witherutils.common.world.level.block.entity;

import geni.withercore.common.world.level.block.entity.WitherMachineBlockEntity;
import geni.withercore.init.WUTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FloorSensorBlockEntity extends WitherMachineBlockEntity {

    public BlockState cover;

    public FloorSensorBlockEntity(BlockPos worldPosition, BlockState blockState)
    {
        super(WUTBlockEntities.FLOORSENSOR, worldPosition, blockState);
    }

    public BlockState getCoverBlock() {
        return cover;
    }

    public boolean hasCover() {
        return cover != null;
    }

    @Override
    public void serverTick()
    {
        super.serverTick();
        setLitProperty(checkEntities(Player.class));
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.loadAdditional(tag, registries);
        this.cover = tag.contains("cover") ?
                NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("cover")) : null;
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), null, null, 0);
            level.getLightEngine().checkBlock(getBlockPos());
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries)
    {
        super.saveAdditional(tag, registries);
        if (this.cover != null)
            tag.put("cover", NbtUtils.writeBlockState(this.cover));
    }

    public void removeCover()
    {
        this.cover = null;
        if (level != null && !level.isClientSide)
        {
            sendBlockEntityToClients(this);
        }
    }

    public static void sendBlockEntityToClients(BlockEntity tile)
    {
        if (tile.getLevel() instanceof ServerLevel world)
        {
            var entities = world.getChunkSource().chunkMap.getPlayers(new ChunkPos(tile.getBlockPos()), false);
            var packet = ClientboundBlockEntityDataPacket.create(tile, BlockEntity::saveWithoutMetadata);
            for (var e : entities)
                e.connection.send(packet);
        }
    }

    private AABB forScanBox()
    {
        return new AABB(worldPosition).inflate(2, 1, 2);
    }

    private boolean checkEntities(Class<? extends Entity> clazz)
    {
        if (level == null) return false;
        List<? extends Entity> entities = level.getEntitiesOfClass(clazz, forScanBox());
        return !entities.isEmpty();
    }

    private boolean checkEntitiesHostile()
    {
        if (level == null) return false;
        List<? extends Entity> entities = level.getEntitiesOfClass(PathfinderMob.class, forScanBox());
        for (Entity entity : entities)
        {
            if (entity instanceof Enemy)
            {
                return true;
            }
        }
        return false;
    }

    private boolean checkEntitiesPassive()
    {
        if (level == null) return false;
        List<? extends Entity> entities = level.getEntitiesOfClass(PathfinderMob.class, forScanBox());
        for (Entity entity : entities)
        {
            if (entity instanceof Mob && !(entity instanceof Enemy))
            {
                return true;
            }
        }
        return false;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        super.handleUpdateTag(tag, lookupProvider);
        loadAdditional(tag, lookupProvider);
        setChanged();
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider provider) {
        if (pkt.getTag() != null) {
            handleUpdateTag(pkt.getTag(), provider);
        }
    }
}
