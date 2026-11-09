package geni.witherutils.client.block.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;

import com.mojang.blaze3d.vertex.VertexConsumer;
import geni.withercore.init.WUTBlocks;
import geni.withercore.client.block.render.entity.AbstractBlockEntityRenderer;
import geni.witherutils.client.render.util.SatisfactoryLightRenderer;
import geni.witherutils.common.world.level.block.FloorSensorBlock;
import geni.witherutils.common.world.level.block.entity.FloorSensorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Matrix4f;

public class FloorSensorRenderer extends AbstractBlockEntityRenderer<FloorSensorBlockEntity> {

    public FloorSensorRenderer(BlockEntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(FloorSensorBlockEntity te, float partialTick, PoseStack matrix, MultiBufferSource buffer, Minecraft mc, ClientLevel level, LocalPlayer player, int light, int overlayLight)
    {
        if (te.getLevel() == null)
            return;
        
		matrix.pushPose();
        renderStateCover(te, partialTick, matrix, buffer, mc, light, overlayLight);
        matrix.popPose();

		BlockState state = te.getBlockState();
		if (state.hasProperty(FloorSensorBlock.LIT) && state.getValue(FloorSensorBlock.LIT)) {
			matrix.pushPose();
			renderPedestalEffect(te, partialTick, matrix, buffer);
			matrix.popPose();
		}

		int machineColor = SatisfactoryLightRenderer.resolveSatisfactoryColor(
				state,
				FloorSensorBlock.LIT,
				() -> true,
				te::isPowered
		);

		boolean isBlinking = (machineColor == SatisfactoryLightRenderer.COLOR_POWER_TRIP);
		matrix.pushPose();

		matrix.translate(0.5f, 0.85f, 0.05f);

		SatisfactoryLightRenderer.renderIndicatorLight(
				matrix,
				buffer,
				() -> te.getLevel() != null ? te.getLevel().getGameTime() : 0L,
				partialTick,
				machineColor,
				isBlinking,
				0.0f, 0.0f, 0.0f,
				0.25f, 0.1f
		);
		matrix.popPose();
    }
    
	public static void renderStateCover(FloorSensorBlockEntity tile, float v, PoseStack matrix, MultiBufferSource brenderer, Minecraft mc, int light, int overlayLight)
    {
		if (mc.player == null)
			return;

    	boolean isCrouching = mc.player.isCrouching();
        Vec3 movement = mc.player.getDeltaMovement();
        boolean isStandingStill = (movement.x * movement.x + movement.z * movement.z) < 0.0001;

    	if (tile.cover != null)
    	{
    		matrix.pushPose();

    		BlockRenderDispatcher renderer = Minecraft.getInstance().getBlockRenderer();
    		BakedModel model = renderer.getBlockModel(tile.cover);

    		for (RenderType layer : model.getRenderTypes(tile.cover, RandomSource.create(tile.cover.getSeed(tile.getBlockPos())), ModelData.EMPTY))
    		{
        		VertexConsumer layerfullornot = brenderer.getBuffer(layer);
        		
        		if (isStandingStill && isCrouching)
        		{
        			model = renderer.getBlockModel(WUTBlocks.FLOORSENSOR.get().defaultBlockState());
        			layerfullornot = brenderer.getBuffer(RenderType.solid());
        		}
    			renderer.getModelRenderer().tesselateBlock(tile.getLevel(), model, tile.cover, tile.getBlockPos(), matrix, layerfullornot, true, RandomSource.create(), tile.cover.getSeed(tile.getBlockPos()), overlayLight, ModelData.EMPTY, layer);
    		}
    		
    		matrix.popPose();
    	}
    }

	private void renderPedestalEffect(FloorSensorBlockEntity te, float partialTick, PoseStack matrix, MultiBufferSource bufferSource) {
		VertexConsumer builder = bufferSource.getBuffer(RenderType.lightning());
		long time = te.getLevel().getGameTime();
		float animProgress = ((time + partialTick) % 40.0f) / 40.0f;
		float minY = 0.5f;
		float maxY = 1.25f;
		float totalRange = maxY - minY;

		float spacing = 2.0f / 16.0f;
		float currentHeightOffset = animProgress * totalRange;

		Matrix4f matrix4f = matrix.last().pose();

		for (float offset = 0.0f; offset < 0.6f; offset += spacing) {
			float y = minY + ((currentHeightOffset + offset) % totalRange);
			float relativeProgress = (y - minY) / totalRange;
			float alpha = 1.0f;

			if (relativeProgress < 0.2f) {
				alpha = relativeProgress / 0.2f;
			} else if (relativeProgress > 0.7f) {
				alpha = (1.0f - relativeProgress) / 0.3f;
			}
			alpha = Math.clamp(alpha, 0.0f, 1.0f);

			if (alpha <= 0.02f) continue;

			float minX = 0.15f;
			float maxX = 0.85f;
			float minZ = 0.15f;
			float maxZ = 0.85f;

			int red = 0;
			int green = 255;
			int blue = 255;
			int intAlpha = (int)(alpha * 255);

			builder.addVertex(matrix4f, minX, y, minZ).setColor(red, green, blue, intAlpha);
			builder.addVertex(matrix4f, minX, y, maxZ).setColor(red, green, blue, intAlpha);
			builder.addVertex(matrix4f, maxX, y, maxZ).setColor(red, green, blue, intAlpha);
			builder.addVertex(matrix4f, maxX, y, minZ).setColor(red, green, blue, intAlpha);
		}
	}

    @Override
    public AABB getRenderBoundingBox(FloorSensorBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }
}
