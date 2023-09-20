package geni.witherutils.base.common.entity.cursed.zombie;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CursedZombieEyesLayer extends RenderLayer<CursedZombie, CursedZombieModel<CursedZombie>> {

	private static final RenderType ZOMBIE_EYES = RenderType.eyes(new ResourceLocation("witherutils:textures/model/entity/cursed/zombie_eyes.png"));

	public CursedZombieEyesLayer(RenderLayerParent<CursedZombie, CursedZombieModel<CursedZombie>> layer)
	{
		super(layer);
	}
	@Override
	public void render(PoseStack p_117349_, MultiBufferSource p_117350_, int p_117351_, CursedZombie p_117352_, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_)
	{
		VertexConsumer vertexconsumer = p_117350_.getBuffer(this.renderType());
		this.getParentModel().renderToBuffer(p_117349_, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
	}
	public RenderType renderType()
	{
		return ZOMBIE_EYES;
	}
}