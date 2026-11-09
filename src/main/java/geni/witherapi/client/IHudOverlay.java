package geni.witherapi.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

public interface IHudOverlay {

    List<Component> getOverlayText(Player player, BlockHitResult hitResult);

    default Component getOverlayTitle(Player player, BlockHitResult hitResult) {
        return Component.literal("Information");
    }

    default boolean shouldRenderPlayerModel(Player player, BlockHitResult hitResult) {
        return false;
    }

    default BakedModel getOverlayModel(BlockState state) {
        if (state == null) return null;
        // Holt das BakedModel direkt aus dem BlockModelShaper von Minecraft
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(state);
    }
}