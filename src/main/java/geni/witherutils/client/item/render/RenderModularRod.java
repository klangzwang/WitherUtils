package geni.witherutils.client.item.render;

import java.util.Map;

import geni.withercore.client.item.render.AbstractItemRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;

import com.mojang.blaze3d.vertex.PoseStack;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import geni.witherutils.WitherUtils;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class RenderModularRod extends AbstractItemRenderer {

    private static RenderType handleType = RenderType.entitySolid(WitherUtils.loc("textures/item/steel/handle.png"));

    private final CCModel handle;

    public RenderModularRod() {
        super();
        Map<String, CCModel> map = new OBJParser(WitherUtils.loc("models/wither/steel/rod.obj")).quads().ignoreMtl()
                .parse();
        handle = map.get("handle").backfacedCopy().computeNormals();
    }

    @Override
    public void renderItem(ItemStack stack, BakedModel model, float partialTick, PoseStack matrix,
            MultiBufferSource buffer, Minecraft mc, ClientLevel level, LocalPlayer player, int packedLight,
            int packedOverlay, ItemDisplayContext context, float swingProgress, InteractionHand hand, boolean isUsing,
            float useProgressRatio, boolean isSprinting) {
        Matrix4 mat = new Matrix4(matrix);
        CCRenderState ccrs = CCRenderState.instance();

        ccrs.reset();
        ccrs.brightness = packedLight;
        ccrs.overlay = packedOverlay;

        ccrs.bind(handleType, buffer);
        handle.render(ccrs, mat);
    }
}
