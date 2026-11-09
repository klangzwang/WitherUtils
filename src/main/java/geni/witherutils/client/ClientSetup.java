package geni.witherutils.client;

import geni.withercore.common.helper.AtlasTextureHelper;
import geni.withercore.init.*;
import geni.witherutils.WitherUtils;
import geni.witherutils.client.block.render.entity.*;
import geni.witherutils.client.block.render.entity.rack.CaseRenderer;
import geni.witherutils.client.block.render.entity.rack.ControllerEnergyRenderer;
import geni.witherutils.client.block.render.entity.rack.ControllerFluidRenderer;
import geni.witherutils.client.block.render.entity.rack.ControllerItemRenderer;
import geni.witherutils.client.block.render.fluid.LiquidRendererPlus;
import geni.witherutils.client.entity.render.CursedLightningBoltRenderer;
import geni.witherutils.client.events.KeybindMappingEvents;
import geni.witherutils.client.gui.screen.CutterScreen;
import geni.witherutils.client.render.bar.BarDecorator;
import geni.witherutils.client.render.hud.OverlayHandler;
import geni.witherutils.common.world.fluid.FluidColdSlush;
import geni.witherutils.common.world.level.block.CutterBlock;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.NeoForge;

/*@EventBusSubscriber(modid = WitherUtils.MODID, value = Dist.CLIENT)*/
public class ClientSetup {

    public static void onModConstruction(IEventBus modBus)
    {
        modBus.addListener(ClientSetup::onClientSetup);
        modBus.addListener(ClientSetup::onRegisterClientExtensions);
        modBus.addListener(ClientSetup::registerRenderers);
        modBus.addListener(ClientSetup::registerScreens);
        modBus.addListener(ClientSetup::registerExtensions);
        modBus.addListener(ClientSetup::registerItemDecorations);
        modBus.addListener(OverlayHandler::registerOverlays);

		AtlasTextureHelper.init(modBus);
    }

    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(FluidColdSlush.RENDER_PROPS, WUTFluids.COLDSLUSH_FLUID_TYPE.get());
    }

    public static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(WUTMenus.CUTTER.get(), CutterScreen::new);
    }

    public static void onClientSetup(FMLClientSetupEvent event)
	{
        NeoForge.EVENT_BUS.register(new KeybindMappingEvents());

        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.ANVIL.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.STEEL_CATWALK.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.STEEL_RAILING.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.ANGEL.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.GREENHOUSE.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.FLOORSENSOR.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.WALL_SENSOR.get(), RenderType.cutout());
    	ItemBlockRenderTypes.setRenderLayer(WUTBlocks.SOULFIRE.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.SOLARBASIC.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.SOLARADV.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.SOLARULTRA.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.CREATIVE_GENERATOR.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.RACK_CASE.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.RACK_TERMINAL.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.RACKITEM_CONTROLLER.get(), RenderType.cutout());
		ItemBlockRenderTypes.setRenderLayer(WUTBlocks.RACKFLUID_CONTROLLER.get(), RenderType.cutout());
        ItemBlockRenderTypes.setRenderLayer(WUTBlocks.RACKENERGY_CONTROLLER.get(), RenderType.cutout());

        for (var entry : WUTBlocks.CTM_GLASS.entrySet()) {
            ItemBlockRenderTypes.setRenderLayer(entry.getValue().get(), RenderType.translucent());
        }

        LiquidRendererPlus.onClientSetup(event);

        /*event.enqueueWork(() -> {
            ClientRegistrationUtil.setPropertyOverride(WUTItems.CUTTER, WitherUtils.loc("pull"),
                    (stack, world, entity, seed) -> entity != null && entity.getUseItem() == stack ? (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / (float) SharedConstants.TICKS_PER_SECOND : 0);
            ClientRegistrationUtil.setPropertyOverride(WUTItems.CUTTER, WitherUtils.loc("pulling"),
                    (stack, world, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
            ClientRegistrationUtil.setPropertyOverride(WUTItems.CUTTER, WitherUtils.loc("nixnix"), (stack, world, entity, seed) -> {
                if (entity instanceof Player) {
                    return 1;
                }
                return 0;
            });
        });*/
    }

	@OnlyIn(Dist.CLIENT)
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
	{
	    event.registerBlockEntityRenderer(WUTBlockEntities.ANVIL.get(), AnvilRenderer::new);
        event.registerBlockEntityRenderer(WUTBlockEntities.FLOORSENSOR.get(), FloorSensorRenderer::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.WALLSENSOR.get(), WallSensorRenderer::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.SOLARBASIC_BE.get(), SolarPanelRenderer.Basic::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.SOLARADV_BE.get(), SolarPanelRenderer.Advanced::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.SOLARULTRA_BE.get(), SolarPanelRenderer.Ultra::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.CREATIVE_GENERATOR.get(), CreativeGeneratorRenderer::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.RACK_CASE.get(), CaseRenderer::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.RACKITEM_CONTROLLER.get(), ControllerItemRenderer::new);
	    event.registerBlockEntityRenderer(WUTBlockEntities.RACKFLUID_CONTROLLER.get(), ControllerFluidRenderer::new);
        event.registerBlockEntityRenderer(WUTBlockEntities.RACKENERGY_CONTROLLER.get(), ControllerEnergyRenderer::new);

        event.registerEntityRenderer(WUTEntities.CURSEDBOLT.get(), CursedLightningBoltRenderer::new);
    }

    /*public static final IClientBlockExtensions PARTICLE_HANDLER = new IClientBlockExtensions()
    {
        @Override
        public boolean addHitEffects(BlockState state, Level world, HitResult target, ParticleEngine manager) {

            System.out.println("addHitEffects");

            if (target.getType() == HitResult.Type.BLOCK && target instanceof BlockHitResult blockTarget) {
                BlockPos pos = blockTarget.getBlockPos();
                BlockPos mainPos = Objects.requireNonNull(BlockEntityUtil.getTileEntity(world, pos)).getBlockPos();

                System.out.println("Type.BLOCK");

                if (mainPos != null) {
                    BlockState mainState = world.getBlockState(mainPos);
                    if (!mainState.isAir()) {
                        //Copy of ParticleManager#addBlockHitEffects except using the block state for the main position
                        AABB axisalignedbb = state.getShape(world, pos).bounds();
                        double x = pos.getX() + world.random.nextDouble() * (axisalignedbb.maxX - axisalignedbb.minX - 0.2) + 0.1 + axisalignedbb.minX;
                        double y = pos.getY() + world.random.nextDouble() * (axisalignedbb.maxY - axisalignedbb.minY - 0.2) + 0.1 + axisalignedbb.minY;
                        double z = pos.getZ() + world.random.nextDouble() * (axisalignedbb.maxZ - axisalignedbb.minZ - 0.2) + 0.1 + axisalignedbb.minZ;
                        Direction side = blockTarget.getDirection();
                        switch (side) {
                            case DOWN -> y = pos.getY() + axisalignedbb.minY - 0.1;
                            case UP -> y = pos.getY() + axisalignedbb.maxY + 0.1;
                            case NORTH -> z = pos.getZ() + axisalignedbb.minZ - 0.1;
                            case SOUTH -> z = pos.getZ() + axisalignedbb.maxZ + 0.1;
                            case WEST -> x = pos.getX() + axisalignedbb.minX - 0.1;
                            case EAST -> x = pos.getX() + axisalignedbb.maxX + 0.1;
                        }
                        manager.add(new TerrainParticle((ClientLevel) world, x, y, z, 0, 0, 0, mainState)
                                .updateSprite(mainState, mainPos).setPower(0.2F).scale(0.6F));
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager)
        {
            state.getShape(Level, pos).forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> {
                double xDif = Math.min(1, maxX - minX);
                double yDif = Math.min(1, maxY - minY);
                double zDif = Math.min(1, maxZ - minZ);
                int xCount = Mth.ceil(xDif / 0.25);
                int yCount = Mth.ceil(yDif / 0.25);
                int zCount = Mth.ceil(zDif / 0.25);
                if (xCount > 0 && yCount > 0 && zCount > 0)
                {
                    for (int x = 0; x < xCount; x++) {
                        for (int y = 0; y < yCount; y++) {
                            for (int z = 0; z < zCount; z++) {
                                double d4 = (x + 0.5) / xCount;
                                double d5 = (y + 0.5) / yCount;
                                double d6 = (z + 0.5) / zCount;
                                double d7 = d4 * xDif + minX;
                                double d8 = d5 * yDif + minY;
                                double d9 = d6 * zDif + minZ;
                                manager.add(new TerrainParticle((ClientLevel) Level, pos.getX() + d7, pos.getY() + d8,
                                        pos.getZ() + d9, d4 - 0.5, d5 - 0.5, d6 - 0.5, state).updateSprite(state, pos));
                            }
                        }
                    }
                }
            });
            return true;
        }
    };*/

    public static void registerExtensions(RegisterClientExtensionsEvent event)
    {
        /*for(var blocks : BuiltInRegistries.BLOCK.entrySet())
        {
            var block = blocks.getValue();

            if(block instanceof WitherBlock blk)
            {
                event.registerBlock(PARTICLE_HANDLER, blk);
            }
        }*/
    }

    public static void registerItemDecorations(RegisterItemDecorationsEvent event) {

        WUTBlocks.CTM_CONCRETE.values().forEach(block -> event.register(block.get(), BarDecorator.INSTANCE));
        WUTBlocks.CTM_DIRT.values().forEach(block -> event.register(block.get(), BarDecorator.INSTANCE));
        WUTBlocks.CTM_GLASS.values().forEach(block -> event.register(block.get(), BarDecorator.INSTANCE));
        WUTBlocks.CTM_METAL.values().forEach(block -> event.register(block.get(), BarDecorator.INSTANCE));
        WUTBlocks.CTM_STONE.values().forEach(block -> event.register(block.get(), BarDecorator.INSTANCE));
        WUTBlocks.CTM_WOOD.values().forEach(block -> event.register(block.get(), BarDecorator.INSTANCE));
    }
}
