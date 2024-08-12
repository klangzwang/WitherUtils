package geni.witherutils.core.common.network;

import geni.witherutils.WitherUtils;
import geni.witherutils.api.WitherUtilsRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.commons.lang3.tuple.Pair;


public record PacketServerToClient(CompoundTag data, IDataHandler handler) implements CustomPacketPayload {
	
    public static final StreamCodec<FriendlyByteBuf, PacketServerToClient> CODEC = CustomPacketPayload.codec(
            PacketServerToClient::write,
            PacketServerToClient::new);
    
    public static final Type<PacketServerToClient> ID = new Type<>(WitherUtilsRegistry.loc("server_to_client"));

    public PacketServerToClient(Pair<CompoundTag, IDataHandler> data)
    {
        this(data.getLeft(), data.getRight());
    }

    public PacketServerToClient(final FriendlyByteBuf buffer)
    {
        this(fromBytes(buffer));
    }

    public static Pair<CompoundTag, IDataHandler> fromBytes(FriendlyByteBuf buffer)
    {
        try
        {
            CompoundTag data = buffer.readNbt();

            int handlerId = buffer.readInt();
            if (handlerId >= 0 && handlerId < CoreNetwork.DATA_HANDLERS.size())
            {
                return Pair.of(data, CoreNetwork.DATA_HANDLERS.get(handlerId));
            }
        }
        catch (Exception e)
        {
            WitherUtils.LOGGER.error("Something went wrong trying to receive a client packet!", e);
        }
        return Pair.of(null, null);
    }

    public void write(FriendlyByteBuf buf)
    {
        buf.writeNbt(data);
        buf.writeInt(CoreNetwork.DATA_HANDLERS.indexOf(handler));
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return ID;
    }

    public static void handle(final PacketServerToClient message, final IPayloadContext context)
    {
        context.enqueueWork(
            () -> {
                if (message.data != null && message.handler != null)
                {
                    message.handler.handleData(message.data, context);
                }
            });
    }
}
