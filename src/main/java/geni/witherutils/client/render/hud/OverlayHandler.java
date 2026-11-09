package geni.witherutils.client.render.hud;

import codechicken.lib.math.MathHelper;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import geni.witherapi.client.IHudOverlay;
import geni.withercore.init.WUTEffects;
import geni.withercore.math.Vector2i;
import geni.witherutils.WitherUtils;
import geni.witherutils.client.gui.console.DevConsoleOverlay;
import geni.witherutils.client.render.type.RenderType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import java.util.List;
import java.util.function.Consumer;

public class OverlayHandler {

	public static void registerOverlays(RegisterGuiLayersEvent event)
	{
		event.registerAboveAll(WitherUtils.loc("blindhud"), (graphics, partialTick) -> {
			Minecraft minecraft = Minecraft.getInstance();
			Window window = minecraft.getWindow();
			LocalPlayer player = minecraft.player;

			if (player != null)
			{
				MobEffectInstance effect = player.getEffect(WUTEffects.BLIND);
				if (effect != null)
				{
					RenderSystem.disableDepthTest();
					RenderSystem.depthMask(false);
					RenderSystem.enableBlend();
					float percent = Math.min((effect.getDuration() / (float) 40), 1);
					graphics.fill(net.minecraft.client.renderer.RenderType.guiOverlay(), 0, 0, window.getScreenWidth(), window.getScreenHeight(), ((int) (percent * 255 + 0.5) << 24) | 16777215);
					RenderSystem.disableBlend();
					RenderSystem.depthMask(true);
					RenderSystem.enableDepthTest();
				}
			}
		});

		event.registerAboveAll(WitherUtils.loc("dev_console_overlay"), (graphics, partialTick) -> {
			DevConsoleOverlay.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
		});

		event.registerAboveAll(WitherUtils.loc("player_look_overlay"), (graphics, partialTick) -> {
			renderHUD(graphics, partialTick.getGameTimeDeltaTicks());
		});

		/*event.registerAboveAll(WitherUtils.loc("shield_combat_overlay"), (graphics, partialTick) -> {
			ShieldCombatOverlay.render(graphics, partialTick.getGameTimeDeltaPartialTick(true));
		});*/
	}

	public static void renderHUD(GuiGraphics graphics, float partialTicks)
	{
		Minecraft minecraft = Minecraft.getInstance();
		Window window = minecraft.getWindow();
		LocalPlayer player = minecraft.player;

		if (player != null)
		{
			double dist = 6.0;

			PoseStack matrixStack = graphics.pose();

			Vec3 start  = player.getEyePosition(partialTicks);
			Vec3 vec31 = player.getViewVector(partialTicks);
			Vec3 end = start.add(vec31.x * dist, vec31.y * dist, vec31.z * dist);
			ClipContext context = new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
			HitResult mouseOver = player.getCommandSenderWorld().clip(context);

			matrixStack.pushPose();
			if (mouseOver.getType() == HitResult.Type.BLOCK)
			{
				renderHUDBlock(graphics, matrixStack, player, mouseOver, partialTicks);
			}
			matrixStack.popPose();
		}
	}

	private static void renderHUDBlock(GuiGraphics graphics, PoseStack matrix, LocalPlayer player, HitResult mouseOver, float partialTicks)
	{
		if (!(mouseOver instanceof BlockHitResult blockHit))
		{
			return;
		}

		BlockPos blockPos = blockHit.getBlockPos();
		Level world = player.getCommandSenderWorld();

		if (world.isEmptyBlock(blockPos))
		{
			return;
		}

		BlockEntity be = world.getBlockEntity(blockPos);
		if (be instanceof IHudOverlay textProvider)
		{
			Component title = textProvider.getOverlayTitle(player, blockHit);
			List<Component> lines = textProvider.getOverlayText(player, blockHit);

			if (lines.isEmpty() && title == null)
			{
				return;
			}

			float sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
			float sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			int x = (int) sw / 2;
			int y = (int) sh;

			Vector2i pos = new Vector2i(x - 75, y - 100);
			Vector2i pos2 = pos.add(new Vector2i(150, 50));

			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);
			drawThickBeveledBox(graphics, pos.x(), pos.y(), pos2.x(), pos2.y(), 1, 0xFF118B8B, 0xFF118B8B, 0xFF373737);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			int modelX = pos.x() + 20;
			int modelY = pos.y() + 40;

			if (textProvider.shouldRenderPlayerModel(player, blockHit))
			{
				renderPlayerModel(graphics, player, modelX, modelY, 20);
			}
			/*BakedModel model = textProvider.getOverlayModel(be.getBlockState());
			if (model != null)
			{
				renderNextPlacement(matrix, player, model, pos.x(), pos.y(), partialTicks);
			}*/

			matrix.pushPose();
			RenderSystem.enableBlend();
			Font font = Minecraft.getInstance().font;

			if (title != null)
			{
				drawTextWithScale(graphics, title, x + 10 - ((float) font.width(title) / 2), y - 90, 0xFFFFFFFF, 1.1f);
			}

			int startY = y - 80;
			for (int i = 0; i < lines.size(); i++)
			{
				Component line = lines.get(i);
				drawTextWithScale(graphics, line, x + 10 - ((float) font.width(line) / 2), startY + (i * 10), 0xFFFFFFFF, 0.812f);
			}

			RenderSystem.disableBlend();
			matrix.popPose();
		}
	}

	public static void drawThickBeveledBox(GuiGraphics graphics, int x1, int y1, int x2, int y2, int thickness, int topleftcolor, int botrightcolor, int fillcolor)
	{
		if (fillcolor != -1)
		{
			graphics.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, fillcolor);
		}
		graphics.fill(x1, y1, x2 - 1, y1 + thickness, topleftcolor);
		graphics.fill(x1, y1, x1 + thickness, y2 - 1, topleftcolor);
		graphics.fill(x2 - thickness, y1, x2, y2 - 1, botrightcolor);
		graphics.fill(x1, y2 - thickness, x2, y2, botrightcolor);
	}

	/*public static ItemStack requestBlockInfo(HitResult mouseOver, BlockPos blockPos, Player player)
	{
		Level world = player.getCommandSenderWorld();
		BlockState blockState = world.getBlockState(blockPos);
		Block block = blockState.getBlock();
		return block.getCloneItemStack(blockState, mouseOver, world, blockPos, player);
	}*/

	public static void renderNextPlacement(PoseStack matrix, LocalPlayer player, BakedModel model, int startX, int startY, float partialTicks)
	{
		float rotation = player.tickCount + partialTicks;

		matrix.pushPose();
		matrix.translate(startX, startY + 25, 100);
		matrix.scale(30F, -30F, 30F);
		matrix.mulPose(Axis.XP.rotationDegrees(5F));
		matrix.mulPose(Axis.YP.rotationDegrees(rotation * (float) MathHelper.torad * 40));
		matrix.mulPose(Axis.ZP.rotationDegrees(rotation * (float) MathHelper.torad * 40 / 2));

		MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();

		VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.solid());

		Minecraft.getInstance().getItemRenderer().renderModelLists(
				model,
				ItemStack.EMPTY,
				15728880,
				OverlayTexture.NO_OVERLAY,
				matrix,
				vertexBuilder
		);

		buffer.endBatch();
		matrix.popPose();

		/*matrix.pushPose();
		matrix.translate(startX, startY + 25, 100);
		matrix.scale(32F, -32F, 32F);
		matrix.mulPose(Axis.XP.rotationDegrees(5F));
		matrix.mulPose(Axis.YN.rotationDegrees(rotation * 0.5F * (float) MathHelper.torad * 60));
		matrix.mulPose(Axis.ZN.rotationDegrees(rotation * 0.5F * (float) MathHelper.torad * 60 / 2));

		buffer = Minecraft.getInstance().renderBuffers().bufferSource();
		vertexBuilder = buffer.getBuffer(WUTRenderType.translucent());

		Minecraft.getInstance().getItemRenderer().renderModelLists(
				BasicModels.LOGO_HAIR.getModel(),
				ItemStack.EMPTY,
				255,
				OverlayTexture.NO_OVERLAY,
				matrix,
				vertexBuilder
		);
		buffer.endBatch();
		matrix.popPose();*/
	}

	public static int drawString(GuiGraphics gg, Component component, int x, int y, int color)
	{
		return gg.drawString(Minecraft.getInstance().font, component, x, y, color);
	}
	public static void drawTextWithScale(GuiGraphics gg, Component text, float x, float y, int color, float scale)
	{
		prepTextScale(gg, m -> drawString(m, text, 0, 0, color), x, y, scale);
	}
	public static void prepTextScale(GuiGraphics gg, Consumer<GuiGraphics> runnable, float x, float y, float scale)
	{
		float yAdd = 4 - (scale * 8) / 2F;
		gg.pose().pushPose();
		gg.pose().translate(x, y + yAdd, 0);
		gg.pose().scale(scale, scale, scale);
		runnable.accept(gg);
		gg.pose().popPose();
		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	public static void renderPlayerModel(GuiGraphics graphics, LivingEntity entity, int x, int y, int scale) {
		InventoryScreen.renderEntityInInventoryFollowsAngle(
				graphics,
				x,           // X-Position
				y,           // Y-Position
				scale,       // Skalierung (z. B. 25)
				0,           // mouseX (0 = fixiert)
				0,           // mouseY (0 = fixiert)
				0.0F,        // Körper-Rotation (0 = nach vorne gerichtet)
				0.0F,        // Kopf-Pitch (angleXComponent)
				0.0F,        // Kopf-Yaw (angleYComponent)
				entity       // Spieler-Entity
		);
	}
}
