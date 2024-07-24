package geni.witherutils.core.common.util;

import java.net.SocketAddress;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.crypto.Cipher;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.ServerboundKeepAlivePacket;
import net.minecraft.network.protocol.common.ServerboundPongPacket;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundBlockEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundClientCommandPacket;
import net.minecraft.network.protocol.game.ServerboundCommandSuggestionPacket;
import net.minecraft.network.protocol.game.ServerboundContainerButtonClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundEditBookPacket;
import net.minecraft.network.protocol.game.ServerboundEntityTagQueryPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundJigsawGeneratePacket;
import net.minecraft.network.protocol.game.ServerboundLockDifficultyPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ServerboundPaddleBoatPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookChangeSettingsPacket;
import net.minecraft.network.protocol.game.ServerboundRecipeBookSeenRecipePacket;
import net.minecraft.network.protocol.game.ServerboundRenameItemPacket;
import net.minecraft.network.protocol.game.ServerboundSeenAdvancementsPacket;
import net.minecraft.network.protocol.game.ServerboundSelectTradePacket;
import net.minecraft.network.protocol.game.ServerboundSetBeaconPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetCommandMinecartPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.network.protocol.game.ServerboundSetJigsawBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSetStructureBlockPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundTeleportToEntityPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.RelativeMovement;

public class FakeNetHandler extends ServerGamePacketListenerImpl {
	
	public FakeNetHandler(MinecraftServer pServer, Connection pConnection, ServerPlayer pPlayer, CommonListenerCookie pCookie)
	{
		super(pServer, new FakeManager(PacketFlow.CLIENTBOUND), pPlayer, pCookie);
	}

    private static class FakeManager extends Connection
    {
        public FakeManager(PacketFlow packetDirection)
        {
            super(packetDirection);
        }

        @Override
        public void channelActive(@Nonnull ChannelHandlerContext p_channelActive_1_) throws Exception {}

        @Override
        public void channelInactive(@Nonnull ChannelHandlerContext p_channelInactive_1_) {}

        @Override
        public void exceptionCaught(@Nonnull ChannelHandlerContext p_exceptionCaught_1_, @Nonnull Throwable p_exceptionCaught_2_) {}

        @Override
        protected void channelRead0(@Nonnull ChannelHandlerContext p_channelRead0_1_, @Nonnull Packet<?> p_channelRead0_2_) {}

        @Override
        public void send(@Nonnull Packet<?> packetIn) {}

        @Override
        public void tick() {}

        @Override
        protected void tickSecond() {}

        @Override
        public SocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public void disconnect(@Nonnull Component message) {}

        @Override
        public boolean isConnecting() {
            return true;
        }

        @Override
        public boolean isMemoryConnection() {
            return false;
        }

        @Override
        public void setEncryptionKey(@Nonnull Cipher p_244777_1_, @Nonnull Cipher p_244777_2_) {}

        @Override
        public boolean isConnected() {
            return false;
        }

        @Override
        public void setReadOnly() {}

        @Override
        public void handleDisconnection() {}

        @Override
        public Channel channel() {
            return null;
        }
    }

    @Override
    public void tick() {}

    @Override
    public void resetPosition() {}

    @Override
    public void disconnect(@Nonnull Component textComponent) {}

    @Override
    public void handlePlayerInput(@Nonnull ServerboundPlayerInputPacket packetIn) {}

    @Override
    public void handleMoveVehicle(@Nonnull ServerboundMoveVehiclePacket packetIn) {}

    @Override
    public void handleAcceptTeleportPacket(@Nonnull ServerboundAcceptTeleportationPacket packetIn) {}

    @Override
    public void handleRecipeBookSeenRecipePacket(@Nonnull ServerboundRecipeBookSeenRecipePacket packetIn) {}

    @Override
    public void handleRecipeBookChangeSettingsPacket(@Nonnull ServerboundRecipeBookChangeSettingsPacket p_241831_1_) {}

    @Override
    public void handleSeenAdvancements(@Nonnull ServerboundSeenAdvancementsPacket packetIn) {}

    @Override
    public void handleCustomCommandSuggestions(@Nonnull ServerboundCommandSuggestionPacket packetIn) {}

    @Override
    public void handleSetCommandBlock(@Nonnull ServerboundSetCommandBlockPacket packetIn) {}

    @Override
    public void handleSetCommandMinecart(@Nonnull ServerboundSetCommandMinecartPacket packetIn) {}

    @Override
    public void handlePickItem(@Nonnull ServerboundPickItemPacket packetIn) {}

    @Override
    public void handleRenameItem(ServerboundRenameItemPacket packetIn) {}

    @Override
    public void handleSetBeaconPacket(ServerboundSetBeaconPacket packetIn) {}

    @Override
    public void handleSetStructureBlock(ServerboundSetStructureBlockPacket packetIn) {}

    @Override
    public void handleSetJigsawBlock(ServerboundSetJigsawBlockPacket p_217262_1_) {}

    @Override
    public void handleJigsawGenerate(ServerboundJigsawGeneratePacket p_230549_1_) {}

    @Override
    public void handleSelectTrade(ServerboundSelectTradePacket packetIn) {}

    @Override
    public void handleEditBook(ServerboundEditBookPacket packetIn) {}

    @Override
    public void handleEntityTagQuery(ServerboundEntityTagQueryPacket packetIn) {}

    @Override
    public void handleBlockEntityTagQuery(ServerboundBlockEntityTagQueryPacket packetIn) {}

    @Override
    public void handleMovePlayer(ServerboundMovePlayerPacket packetIn) {}

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch) {}

    @Override
    public void teleport(double x, double y, double z, float yaw, float pitch, Set<RelativeMovement> pRelativeSet) {}
    
    @Override
    public void handlePlayerAction(ServerboundPlayerActionPacket packetIn) {}

    @Override
    public void handleUseItemOn(ServerboundUseItemOnPacket packetIn) {}

    @Override
    public void handleUseItem(ServerboundUseItemPacket packetIn) {}

    @Override
    public void handleTeleportToEntityPacket(ServerboundTeleportToEntityPacket packetIn) {}

    @Override
    public void handleResourcePackResponse(ServerboundResourcePackPacket packetIn) {}

    @Override
    public void handlePaddleBoat(ServerboundPaddleBoatPacket packetIn) {}

    @Override
    public void onDisconnect(DisconnectionDetails reason) {}

    @Override
    public void send(Packet<?> packetIn) {}

    @Override
    public void handleSetCarriedItem(ServerboundSetCarriedItemPacket packetIn) {}

    @Override
    public void handleChat(ServerboundChatPacket packetIn) {}

    @Override
    public void handleAnimate(ServerboundSwingPacket packetIn) {}

    @Override
    public void handlePlayerCommand(ServerboundPlayerCommandPacket packetIn) {}

    @Override
    public void handleInteract(ServerboundInteractPacket packetIn) {}

    @Override
    public void handleClientCommand(ServerboundClientCommandPacket packetIn) {}

    @Override
    public void handleContainerClose(ServerboundContainerClosePacket packetIn) {}

    @Override
    public void handleContainerClick(ServerboundContainerClickPacket packetIn) {}

    @Override
    public void handlePlaceRecipe(ServerboundPlaceRecipePacket packetIn) {}

    @Override
    public void handleContainerButtonClick(ServerboundContainerButtonClickPacket packetIn) {}

    @Override
    public void handleSetCreativeModeSlot(ServerboundSetCreativeModeSlotPacket packetIn) {}

    @Override
    public void handleSignUpdate(ServerboundSignUpdatePacket packetIn) {}

    @Override
    public void handleKeepAlive(ServerboundKeepAlivePacket packetIn) {}

    @Override
    public void handlePlayerAbilities(ServerboundPlayerAbilitiesPacket packetIn) {}

    @Override
    public void handleClientInformation(ServerboundClientInformationPacket packetIn) {}

    @Override
    public void handleCustomPayload(ServerboundCustomPayloadPacket packetIn) {}

    @Override
    public void handleChangeDifficulty(ServerboundChangeDifficultyPacket p_217263_1_) {}

    @Override
    public void handleLockDifficulty(ServerboundLockDifficultyPacket p_217261_1_) {}

//    @Override
//    public void dismount(double p_143612_, double p_143613_, double p_143614_, float p_143615_, float p_143616_) {
//    }
//
//    @Override
//    public void teleport(double p_143618_, double p_143619_, double p_143620_, float p_143621_, float p_143622_, Set<ClientboundPlayerPositionPacket.RelativeArgument> p_143623_, boolean p_143624_) {
//    }

    @Override
    public void handlePong(ServerboundPongPacket p_143652_) {
    }
}