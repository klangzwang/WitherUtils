package geni.witherutils.api;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public class TimeKeeper {

    private static int serverTick = 0;
    private static int clientTick = 0;

    static {
        NeoForge.EVENT_BUS.register(new TimeKeeper());
    }

    @SubscribeEvent
    protected void clientTick(ClientTickEvent.Post event) {
        clientTick++;
    }

    @SubscribeEvent
    protected void serverTick(ServerTickEvent.Pre event)
    {
        serverTick++;
    }

    public static int getServerTick() {
        return serverTick;
    }

    public static int getClientTick() {
        return clientTick;
    }

    public static int interval(int intervalTicks, int rollover) {
        int tick = EffectiveSide.get().isClient() ? getClientTick() : getServerTick();
        return (tick / intervalTicks) % rollover;
    }

}
