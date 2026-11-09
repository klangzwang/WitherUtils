package geni.witherdata.generator;

import com.google.gson.JsonObject;
import geni.withercore.common.registration.impl.BlockRegistryObject;
import geni.withercore.common.world.level.block.state.MultiBlockState;
import geni.withercore.init.WUTBlocks;
import geni.witherutils.WitherUtils;
import geni.witherutils.common.world.level.block.AnvilBlock;
import geni.witherutils.common.world.level.block.SteelBeamBlock;
import geni.witherutils.common.world.level.block.SteelPoleBlock;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.Map;

public class WitherUtilsBlockStates extends BlockStateProvider {

    public WitherUtilsBlockStates(PackOutput output, ExistingFileHelper fileHelper)
    {
        super(output, WitherUtils.MODID, fileHelper);
    }

    /*
     *
     * FLUIDS
     *
     */
    private void registerFluids()
    {
        simpleBlock(WUTBlocks.COLDSLUSH.get(), models().getBuilder("block/fluid/coldslush")
                .texture("particle", modLoc("block/fluid/coldslush_still"))
                .texture("particle2", modLoc("block/fluid/coldslush_flow"))
                .renderType("translucent"));
    }

    /*
     *
     * STEELS
     *
     */
    private void registerSteels()
    {
        // BEAM
        ModelFile steelBeam0 = models().getExistingFile(modLoc("block/pole/steel_beam_0"));
        ModelFile steelBeam1 = models().getExistingFile(modLoc("block/pole/steel_beam_1"));
        ModelFile steelBeam2 = models().getExistingFile(modLoc("block/pole/steel_beam_2"));
        ModelFile steelWall = models().getExistingFile(modLoc("block/pole/steel_wall"));
        ModelFile steelConn = models().getExistingFile(modLoc("block/pole/steel_conn"));
        ModelFile steelConnB = models().getExistingFile(modLoc("block/pole/steel_connb"));

        MultiPartBlockStateBuilder beamBuilder = getMultipartBuilder(WUTBlocks.STEEL_BEAM.get());

        ModelFile[] steelBeams = { steelBeam0, steelBeam1, steelBeam2 };

        for (int v = 0; v < 3; v++)
        {
            ModelFile currentBeamModel = steelBeams[v];

            for (Direction dir : Direction.Plane.HORIZONTAL)
            {
                beamBuilder.part()
                        .modelFile(currentBeamModel)
                        .rotationY(dir == Direction.SOUTH ? 180 : dir == Direction.WEST ? 270 : dir == Direction.EAST ? 90 : 0)
                        .addModel()
                        .condition(SteelBeamBlock.FACING, dir)
                        .condition(SteelBeamBlock.VARIANT, v)
                        .end();

                beamBuilder.part()
                        .modelFile(steelWall)
                        .rotationY(dir == Direction.SOUTH ? 180 : dir == Direction.WEST ? 270 : dir == Direction.EAST ? 90 : 0)
                        .addModel()
                        .condition(SteelBeamBlock.FACING, dir)
                        .condition(SteelBeamBlock.WALL, true)
                        .condition(SteelBeamBlock.VARIANT, v)
                        .end();
            }

            beamBuilder.part()
                    .modelFile(steelConnB)
                    .rotationY(0)
                    .addModel()
                    .condition(SteelBeamBlock.NORTHCONN, true)
                    .condition(SteelBeamBlock.VARIANT, v)
                    .end();

            beamBuilder.part()
                    .modelFile(steelConnB)
                    .rotationY(90)
                    .addModel()
                    .condition(SteelBeamBlock.EASTCONN, true)
                    .condition(SteelBeamBlock.VARIANT, v)
                    .end();

            beamBuilder.part()
                    .modelFile(steelConnB)
                    .rotationY(180)
                    .addModel()
                    .condition(SteelBeamBlock.SOUTHCONN, true)
                    .condition(SteelBeamBlock.VARIANT, v)
                    .end();

            beamBuilder.part()
                    .modelFile(steelConnB)
                    .rotationY(270)
                    .addModel()
                    .condition(SteelBeamBlock.WESTCONN, true)
                    .condition(SteelBeamBlock.VARIANT, v)
                    .end();
        }

        /*for (Direction dir : Direction.Plane.HORIZONTAL)
        {
            beamBuilder.part()
                    .modelFile(steelBeam0)
                    .rotationY(dir == Direction.SOUTH ? 180 : dir == Direction.WEST ? 270 : dir == Direction.EAST ? 90 : 0)
                    .addModel()
                    .condition(SteelBeamBlock.FACING, dir)
                    .end();

            beamBuilder.part()
                    .modelFile(steelWall)
                    .rotationY(dir == Direction.SOUTH ? 180 : dir == Direction.WEST ? 270 : dir == Direction.EAST ? 90 : 0)
                    .addModel()
                    .condition(SteelBeamBlock.FACING, dir)
                    .condition(SteelBeamBlock.WALL, true)
                    .end();
        }

        beamBuilder.part()
                .modelFile(steelConnB)
                .rotationY(0)
                .addModel()
                .condition(SteelBeamBlock.NORTHCONN, true)
                .end();

        beamBuilder.part()
                .modelFile(steelConnB)
                .rotationY(90)
                .addModel()
                .condition(SteelBeamBlock.EASTCONN, true)
                .end();

        beamBuilder.part()
                .modelFile(steelConnB)
                .rotationY(180)
                .addModel()
                .condition(SteelBeamBlock.SOUTHCONN, true)
                .end();

        beamBuilder.part()
                .modelFile(steelConnB)
                .rotationY(270)
                .addModel()
                .condition(SteelBeamBlock.WESTCONN, true)
                .end();*/

        simpleBlockItem(WUTBlocks.STEEL_BEAM.get(), steelBeam0);

        // FOOT
        ModelFile steelPole = models().getExistingFile(modLoc("block/pole/steel_pole"));
        ModelFile steelFoot = models().getExistingFile(modLoc("block/pole/steel_foot"));

        MultiPartBlockStateBuilder poleBuilder = getMultipartBuilder(WUTBlocks.STEEL_POLE.get());

        for (Direction dir : Direction.values())
        {
            poleBuilder.part()
                    .modelFile(steelPole)
                    .addModel()
                    .condition(SteelPoleBlock.FACINGDOWN, true)
                    .end();
            poleBuilder.part()
                    .modelFile(steelPole)
                    .addModel()
                    .condition(SteelPoleBlock.FACINGDOWN, false)
                    .end();

            poleBuilder.part()
                    .modelFile(steelFoot)
                    .rotationX(180)
                    .addModel()
                    .condition(SteelPoleBlock.FOOT, true)
                    .condition(SteelPoleBlock.FACINGDOWN, true)
                    .end();
            poleBuilder.part()
                    .modelFile(steelFoot)
                    .addModel()
                    .condition(SteelPoleBlock.FOOT, true)
                    .condition(SteelPoleBlock.FACINGDOWN, false)
                    .end();

            poleBuilder.part()
                    .modelFile(steelConn)
                    .rotationY(0)
                    .addModel()
                    .condition(SteelPoleBlock.NORTHCONN, true)
                    .end();

            poleBuilder.part()
                    .modelFile(steelConn)
                    .rotationY(90)
                    .addModel()
                    .condition(SteelPoleBlock.EASTCONN, true)
                    .end();

            poleBuilder.part()
                    .modelFile(steelConn)
                    .rotationY(180)
                    .addModel()
                    .condition(SteelPoleBlock.SOUTHCONN, true)
                    .end();

            poleBuilder.part()
                    .modelFile(steelConn)
                    .rotationY(270)
                    .addModel()
                    .condition(SteelPoleBlock.WESTCONN, true)
                    .end();
        }
    }

    /*
     *
     * RACK
     *
     */
    public ModelFile getMBStateModel(MultiBlockState mbstate)
    {
        if(mbstate == MultiBlockState.NONE)
        {
            return models().getExistingFile(modLoc("block/rack/casing/case_none"));
        }
        else if(mbstate == MultiBlockState.XMAX_ZMAX_YEDGE ||
                mbstate == MultiBlockState.XMAX_ZMIN_YEDGE ||
                mbstate == MultiBlockState.XMIN_ZMAX_YEDGE ||
                mbstate == MultiBlockState.XMIN_ZMIN_YEDGE)
        {
            return models().getExistingFile(modLoc("block/rack/casing/case_corner"));
        }
        else
        {
            return models().getExistingFile(modLoc("block/rack/casing/case_null"));
        }
    }

    public int getMBStateYRotation(MultiBlockState mbstate)
    {
        if(mbstate == MultiBlockState.XMIN_ZMAX_YEDGE)
        {
            return -90;
        }
        else if(mbstate == MultiBlockState.XMAX_ZMIN_YEDGE)
        {
            return 90;
        }
        else if(mbstate == MultiBlockState.XMAX_ZMAX_YEDGE)
        {
            return 180;
        }
        else
        {
            return 0;
        }
    }

    protected void registerRack()
    {
        getVariantBuilder(WUTBlocks.RACKENERGY_CONTROLLER.get()).forAllStates(state -> {

            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            boolean lit = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);

            return ConfiguredModel.builder()
                    .modelFile(lit ? models().getExistingFile(modLoc("block/rack/controller/controller_on")) :
                            models().getExistingFile(modLoc("block/rack/controller/controller")))
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        getVariantBuilder(WUTBlocks.RACKITEM_CONTROLLER.get()).forAllStates(state -> {

            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            boolean lit = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);

            return ConfiguredModel.builder()
                    .modelFile(lit ? models().getExistingFile(modLoc("block/rack/controller/controller_on")) :
                            models().getExistingFile(modLoc("block/rack/controller/controller")))
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        getVariantBuilder(WUTBlocks.RACKFLUID_CONTROLLER.get()).forAllStates(state -> {

            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            boolean lit = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);

            return ConfiguredModel.builder()
                    .modelFile(lit ? models().getExistingFile(modLoc("block/rack/controller/controller_on")) :
                            models().getExistingFile(modLoc("block/rack/controller/controller")))
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        getVariantBuilder(WUTBlocks.RACK_TERMINAL.get()).forAllStates(state -> {

            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING);
            boolean formed = state.getValue(geni.withercore.common.world.level.block.state.BlockStateProperties.FORMED);

            return ConfiguredModel.builder()
                    .modelFile(formed ? models().getExistingFile(modLoc("block/rack/terminal/terminal_formed")) :
                            models().getExistingFile(modLoc("block/rack/terminal/terminal")))
                    .rotationX(dir == Direction.DOWN ? 270 : dir == Direction.UP ? 90 : 0)
                    .rotationY(dir == Direction.SOUTH ? 180 : dir == Direction.EAST ? 90 : dir == Direction.WEST ? 270 : 0)
                    .build();
        });

        getVariantBuilder(WUTBlocks.RACK_CASE.get()).forAllStates(state -> {

            MultiBlockState mbstate = state.getValue(geni.withercore.common.world.level.block.state.BlockStateProperties.MBSTATE);

            return ConfiguredModel.builder()
                    .modelFile(getMBStateModel(mbstate))
                    .rotationY(getMBStateYRotation(mbstate))
                    .build();
        });
    }

    private void registerSensors()
    {
        getVariantBuilder(WUTBlocks.FLOORSENSOR.get()).forAllStates(state -> {
            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            return ConfiguredModel.builder()
                    .modelFile(models().getExistingFile(modLoc("block/sensor/floor/floorsensor")))
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });

        getVariantBuilder(WUTBlocks.WALL_SENSOR.get()).forAllStates(state -> {
            Direction dir = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
            return ConfiguredModel.builder()
                    .modelFile(models().getExistingFile(modLoc("block/sensor/wall/wallsensor")))
                    .rotationY(dir.getAxis().isVertical() ? 0 : (((int) dir.toYRot()) + 180) % 360)
                    .build();
        });
    }

    @Override
    protected void registerStatesAndModels()
    {
        registerCtmMap(WUTBlocks.CTM_CONCRETE);
		registerCtmMap(WUTBlocks.CTM_DIRT);
        registerCtmMap(WUTBlocks.CTM_GLASS);
        registerCtmMap(WUTBlocks.CTM_METAL);
        registerCtmMap(WUTBlocks.CTM_STONE);
        registerCtmMap(WUTBlocks.CTM_WOOD);

        registerModelBlock(WUTBlocks.ANGEL, "angel");
        registerCubeAll(WUTBlocks.WITHERSTEEL_BLOCK, "block/shared/full19");
        registerCubeAllComplete(WUTBlocks.BRICKSDARK, "block/bricks", "block/bricks/bricks_dark");

        registerRack();
        registerSensors();
        registerSteels();
        registerFluids();

        getVariantBuilder(WUTBlocks.ANVIL.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(models().getExistingFile(modLoc("block/anvil/anvil")))
                .rotationY((((int) state.getValue(AnvilBlock.FACING).toYRot()) + 180) % 360)
                .build());

        BlockModelBuilder offModel = models().cubeAll("block/bricks/bricks_lava_off", modLoc("block/bricks/bricks_dark"));
        BlockModelBuilder onModel = models().cubeAll("block/bricks/bricks_lava_on", modLoc("block/bricks/bricks_lava"));
        getVariantBuilder(WUTBlocks.BRICKSLAVA.get()).forAllStates(state -> {
            boolean isPowered = state.getValue(BlockStateProperties.LIT);
            return ConfiguredModel.builder()
                    .modelFile(isPowered ? onModel : offModel)
                    .build();
        });
        simpleBlockItem(WUTBlocks.BRICKSLAVA.get(), onModel);

        getVariantBuilder(WUTBlocks.CREATIVE_GENERATOR.get()).forAllStates(state -> {
            boolean lit = state.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.LIT);
            return ConfiguredModel.builder()
                    .modelFile(lit ? models().getExistingFile(modLoc("block/generator/creative/creative_generator_on")) :
                            models().getExistingFile(modLoc("block/generator/creative/creative_generator")))
                    .build();
        });
    }

    private void registerCtmMap(Map<String, ? extends BlockRegistryObject<? extends Block, ?>> map) {
        for (BlockRegistryObject<? extends Block, ?> blockObj : map.values()) {
            Block block = blockObj.get();
            ctmBlock(block);
        }
    }

    public void ctmBlock(Block block) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        String fullName = blockId.getPath();
        String modId = blockId.getNamespace();

        String[] parts = fullName.split("_");
        String material = parts[0]; // Enthält "glass", "dirt", "stone", etc.
        String textureName = parts[1];

        String modelRelativePath = "block/ctm/" + fullName;
        String fullModelLocation = modId + ":" + modelRelativePath;
        String textureBase = modId + ":block/ctm/" + material + "/" + textureName + "/";

        // 1. Basis-Modell erzeugen
        BlockModelBuilder model = models().withExistingParent(modelRelativePath, mcLoc("block/cube_all"))
                .texture("all", textureBase + "particle");

        // HIER: RenderType nur für Glas-Blöcke setzen
        if (material.equals("glass")) {
            model.renderType("translucent"); // Verwende "cutout", falls du Pixelkanten statt weicher Transparenz möchtest
        }

        // 2. Item-Modell erzeugen
        itemModels().withExistingParent(fullName, modLoc(modelRelativePath));

        // 3. BlockState JSON mit Athena CTM Loader
        registeredBlocks.put(block, () -> {
            JsonObject root = new JsonObject();

            JsonObject variants = new JsonObject();
            JsonObject variantObj = new JsonObject();
            variantObj.addProperty("model", fullModelLocation);
            variants.add("", variantObj);
            root.add("variants", variants);

            root.addProperty("athena:loader", "athena:ctm");

            JsonObject ctmTextures = new JsonObject();
            ctmTextures.addProperty("center", textureBase + "center");
            ctmTextures.addProperty("empty", textureBase + "empty");
            ctmTextures.addProperty("horizontal", textureBase + "horizontal");
            ctmTextures.addProperty("vertical", textureBase + "vertical");
            ctmTextures.addProperty("particle", textureBase + "particle");

            root.add("ctm_textures", ctmTextures);

            return root;
        });
    }

    /*private void registerBlock(BlockRegistryObject<? extends Block, ? extends BlockItem> block)
    {
        registerBlock(block, "", "solid");
    }

    private void registerBlock(BlockRegistryObject<? extends Block, ? extends BlockItem> block, String dir, String renderType)
    {
        String location = "block/";
        if(!dir.isEmpty()) {
            location = location + dir + "/";
        }
        location = location + BuiltInRegistries.BLOCK.getKey(block.get()).getPath();

        ModelFile model = models().getBuilder(location)
                .parent(models().getExistingFile(mcLoc("block/cube_all")))
                .texture("all", modLoc(location))
                .renderType(renderType);
        super.simpleBlock(block.get(), model);
    }*/

    private void registerCubeAll(BlockRegistryObject<? extends Block, ? extends BlockItem> block, String textureDirAndName)
    {
        ModelFile model = models()
                .getBuilder("block/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath())
                .parent(models().getExistingFile(mcLoc("block/cube_all")))
                .texture("all", modLoc(textureDirAndName));
        super.simpleBlockWithItem(block.get(), model);
    }

    private void registerCubeAllComplete(BlockRegistryObject<? extends Block, ? extends BlockItem> block, String blockDir, String textureDirAndName)
    {
        ModelFile model = models()
                .getBuilder(blockDir + "/" + BuiltInRegistries.BLOCK.getKey(block.get()).getPath())
                .parent(models().getExistingFile(mcLoc("block/cube_all")))
                .texture("all", modLoc(textureDirAndName));
        super.simpleBlockWithItem(block.get(), model);
    }

    private void registerModelBlock(BlockRegistryObject<? extends Block, ? extends BlockItem> block, String dir)
    {
        String location = "block/";
        if(!dir.isEmpty()) {
            location = location + dir + "/";
        }
        location = location + BuiltInRegistries.BLOCK.getKey(block.get()).getPath();

        ModelFile model = models().getExistingFile(modLoc(location));
        super.simpleBlockWithItem(block.get(), model);
    }

    @Override
    public String getName()
    {
        return "WitherUtils Blockstates";
    }
}
