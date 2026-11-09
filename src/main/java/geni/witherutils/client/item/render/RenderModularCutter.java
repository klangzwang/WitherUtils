package geni.witherutils.client.item.render;

import codechicken.lib.render.CCModel;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.model.OBJParser;
import codechicken.lib.vec.Matrix4;
import com.mojang.blaze3d.vertex.PoseStack;
import geni.withercore.client.item.render.AbstractItemRenderer;
import geni.witherutils.WitherUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public class RenderModularCutter extends AbstractItemRenderer {

    private static RenderType handleType = RenderType.entitySolid(WitherUtils.loc("textures/item/steel/handle.png"));
    private static RenderType headType = RenderType.entitySolid(WitherUtils.loc("textures/item/steel/head.png"));

    private final CCModel handle;
    private final CCModel head;

    public RenderModularCutter() {
        super();
        Map<String, CCModel> map = new OBJParser(WitherUtils.loc("models/wither/steel/cutter.obj")).quads().ignoreMtl()
                .parse();
        handle = map.get("handle").backfacedCopy().computeNormals();
        head = map.get("head").backfacedCopy().computeNormals();
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

        ccrs.bind(headType, buffer);
        head.render(ccrs, mat);
    }
}
