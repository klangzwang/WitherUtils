package geni.witherutils.client.render.type;

import java.awt.Color;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;

import geni.withercore.common.helper.TickHelper;
import geni.witherutils.WitherUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderType extends net.minecraft.client.renderer.RenderType {

	private static final boolean SORT = false;
	private static final boolean CRUMBLING = false;
	private static final int BUFFERSIZE = 256;

    public static final ResourceLocation BLANK_TEXTURE = WitherUtils.loc("textures/render/blank.png");
    public static final ResourceLocation LIN_GLOW_TEXTURE = WitherUtils.loc("textures/render/glow_linear.png");
    public static final ResourceLocation RND_GLOW_TEXTURE = WitherUtils.loc("textures/render/glow_round.png");
    public static final ResourceLocation MUZZLE_FLASH_TEXTURE = WitherUtils.loc("textures/effect/muzzle_flash.png");
    
    private static final net.minecraft.client.renderer.RenderType BULLET_TRAIL = net.minecraft.client.renderer.RenderType.create(WitherUtils.MODID + ":projectile_trail", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, true, net.minecraft.client.renderer.RenderType.CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER).setCullState(NO_CULL).setTransparencyState(TRANSLUCENT_TRANSPARENCY).createCompositeState(false));
    private static final net.minecraft.client.renderer.RenderType MUZZLE_FLASH = net.minecraft.client.renderer.RenderType.create(WitherUtils.MODID + ":muzzle_flash", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false, net.minecraft.client.renderer.RenderType.CompositeState.builder().setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER).setTextureState(new RenderStateShard.TextureStateShard(MUZZLE_FLASH_TEXTURE, false, false)).setTransparencyState(TRANSLUCENT_TRANSPARENCY).setCullState(NO_CULL).createCompositeState(true));

    public static net.minecraft.client.renderer.RenderType getBulletTrail()
    {
        return BULLET_TRAIL;
    }

    public static net.minecraft.client.renderer.RenderType getMuzzleFlash()
    {
        return MUZZLE_FLASH;
    }
    
    private static final Function<ResourceLocation, net.minecraft.client.renderer.RenderType> TEXTURE_RENDER = Util.memoize((rl) -> {
        RenderStateShard.TextureStateShard textureState = new TextureStateShard(rl, false, false);
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setTextureState(textureState)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .createCompositeState(false);
        return create("texture", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                256, false, false, state);
    });
    public static net.minecraft.client.renderer.RenderType getTextureRender(ResourceLocation texture) {
        return TEXTURE_RENDER.apply(texture);
    }

    private static final BiFunction<ResourceLocation, Boolean, net.minecraft.client.renderer.RenderType> TEXTURE_RENDER_COLORED = Util.memoize((rl, disableDepthTest) -> {
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setTextureState(new RenderStateShard.TextureStateShard(rl, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setWriteMaskState(disableDepthTest ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return create("texture_color", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS,
                256, false, false, state);
    });
    public static net.minecraft.client.renderer.RenderType getTextureRenderColored(ResourceLocation texture) {
        return getTextureRenderColored(texture, false);
    }
    public static net.minecraft.client.renderer.RenderType getTextureRenderColored(ResourceLocation texture, boolean disableDepthTest) {
        return TEXTURE_RENDER_COLORED.apply(texture, disableDepthTest);
    }

    public static final net.minecraft.client.renderer.RenderType UNTEXTURED_QUAD_NO_DEPTH = create("untextured_quad_no_depth", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
            false, false,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final net.minecraft.client.renderer.RenderType UNTEXTURED_QUAD = create("untextured_quad", DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.QUADS, 256,
            false, false,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setTextureState(RenderStateShard.NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .setWriteMaskState(COLOR_DEPTH_WRITE)
                    .createCompositeState(false)
    );

    public static final net.minecraft.client.renderer.RenderType BLOCK_FRAME = create("block_frame",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
            false, false,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setTextureState(NO_TEXTURE)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final net.minecraft.client.renderer.RenderType BLOCK_TRACKER = create("block_tracker",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.LINES, 256,
            false, false,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setLineState(LineStateShard.DEFAULT_LINE)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(NO_LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    public static final net.minecraft.client.renderer.RenderType TARGET_CIRCLE = create("target_circle",
            DefaultVertexFormat.POSITION_COLOR_LIGHTMAP, VertexFormat.Mode.TRIANGLE_STRIP, 65536,
            false, false,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_LIGHTMAP_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setTextureState(NO_TEXTURE)
                    .setCullState(NO_CULL)
                    .setLightmapState(LIGHTMAP)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setWriteMaskState(COLOR_WRITE)
                    .createCompositeState(false)
    );

    private final static BiFunction<Boolean,Boolean, net.minecraft.client.renderer.RenderType> BLOCK_HILIGHT_FACE = Util.memoize((disableDepthTest, disableWriteMask) -> {
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setLightmapState(NO_LIGHTMAP)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : LEQUAL_DEPTH_TEST)
                .setWriteMaskState(disableWriteMask ? COLOR_WRITE : COLOR_DEPTH_WRITE)
                .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                .createCompositeState(false);
        return create("nav_path_lines",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
                false, false, state);
    });
    public static net.minecraft.client.renderer.RenderType getBlockHilightFace(boolean disableDepthTest, boolean disableWriteMask) {
        return BLOCK_HILIGHT_FACE.apply(disableDepthTest, disableWriteMask);
    }

    private final static BiFunction<Boolean,Boolean, net.minecraft.client.renderer.RenderType> BLOCK_HILIGHT_LINE = Util.memoize((disableDepthTest, disableWriteMask) -> {
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setTextureState(NO_TEXTURE)
                .setDepthTestState(disableDepthTest ? NO_DEPTH_TEST : RenderStateShard.LEQUAL_DEPTH_TEST)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .setWriteMaskState(disableWriteMask ? COLOR_WRITE : RenderStateShard.COLOR_DEPTH_WRITE)
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .createCompositeState(false);
        return create("block_hilight_line",
                DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.DEBUG_LINES, 256,
                false, false, state);
    });
    public static net.minecraft.client.renderer.RenderType getBlockHilightLine(boolean disableDepthTest, boolean disableWriteMask) {
        return BLOCK_HILIGHT_LINE.apply(disableDepthTest, disableWriteMask);
    }

    private static final Function<ResourceLocation, net.minecraft.client.renderer.RenderType> ARMOR_TRANSLUCENT_NO_CULL = Util.memoize((rl) -> {
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(rl, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);
        return create("armor_translucent_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, false,
                state);
    });
    public static net.minecraft.client.renderer.RenderType getArmorTranslucentNoCull(ResourceLocation rl) {
        return ARMOR_TRANSLUCENT_NO_CULL.apply(rl);
    }
    
    @SuppressWarnings("unused")
	private static final DepthTestStateShard DISABLE_DEPTH = new DepthTestStateShard("none", 519) {
        @Override
        public void setupRenderState() {

            RenderSystem.disableDepthTest();
        }
    };

    public static final net.minecraft.client.renderer.RenderType OVERLAY_BOX = net.minecraft.client.renderer.RenderType.create("cofh:overlay_box",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256, false, true,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .createCompositeState(false)
    );

    public static final net.minecraft.client.renderer.RenderType FLAT_CUTOUT = opaque("cofh_core:opaque", new TextureStateShard(BLANK_TEXTURE, false, false));
    public static final net.minecraft.client.renderer.RenderType FLAT_TRANSLUCENT = translucentNoDepthWrite(BLANK_TEXTURE);
    public static final net.minecraft.client.renderer.RenderType LINEAR_GLOW = translucentNoDepthWrite(LIN_GLOW_TEXTURE);
    public static final net.minecraft.client.renderer.RenderType ROUND_GLOW = translucentNoDepthWrite(RND_GLOW_TEXTURE);

    public static net.minecraft.client.renderer.RenderType opaque(String name, TextureStateShard texture) {

        return net.minecraft.client.renderer.RenderType.create(name, DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                net.minecraft.client.renderer.RenderType.CompositeState.builder()
                        .setTextureState(texture)
                        .setShaderState(RENDERTYPE_ENTITY_SOLID_SHADER)
                        .createCompositeState(false));
    }

    public static net.minecraft.client.renderer.RenderType translucent(ResourceLocation texture) {

        return net.minecraft.client.renderer.RenderType.create("wut:translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                net.minecraft.client.renderer.RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setShaderState(RENDERTYPE_CRUMBLING_SHADER)
                        .setOutputState(MAIN_TARGET)
                        .setWriteMaskState(COLOR_DEPTH_WRITE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false));
    }

    public static net.minecraft.client.renderer.RenderType translucentNoCull(ResourceLocation texture) {

        return net.minecraft.client.renderer.RenderType.create("wut:translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                net.minecraft.client.renderer.RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setShaderState(RENDERTYPE_CRUMBLING_SHADER)
                        .setOutputState(MAIN_TARGET)
                        .setWriteMaskState(COLOR_WRITE)
                        .setCullState(NO_CULL)
                        .setLightmapState(LIGHTMAP)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .createCompositeState(false));
    }

    public static net.minecraft.client.renderer.RenderType translucentNoDepthWrite(ResourceLocation texture) {

        return net.minecraft.client.renderer.RenderType.create("wut:translucent", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true,
                net.minecraft.client.renderer.RenderType.CompositeState.builder()
                        .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                        .setShaderState(RENDERTYPE_CRUMBLING_SHADER)
                        .setOutputState(MAIN_TARGET)
                        .setWriteMaskState(COLOR_WRITE)
                        .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                        .setLightmapState(LIGHTMAP)
                        .createCompositeState(false));
    }

    static ParticleRenderType translucentSheet(Supplier<ShaderInstance> shader) {

        return new ParticleRenderType() {

			@SuppressWarnings("deprecation")
			@Override
			public BufferBuilder begin(Tesselator pTesselator, TextureManager pTextureManager)
			{
                RenderSystem.depthMask(false);
                RenderSystem.enableBlend();
                RenderSystem.setShader(shader);
                RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
                return pTesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
			}
        };
    }
	
    protected static final RenderStateShard.ShaderStateShard BLOCK_SHADER =
            new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeTranslucentMovingBlockShader);

    public static final net.minecraft.client.renderer.RenderType FIELD_RENDER_TYPE = net.minecraft.client.renderer.RenderType.create("projection_field",
            DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 256,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false));

    public static final net.minecraft.client.renderer.RenderType PHANTOM = net.minecraft.client.renderer.RenderType.create("phantom", DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS, 2097152,
            true, false,
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setShaderState(BLOCK_SHADER)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .createCompositeState(true));
    
	/*
	 * 
	 * TESTING
	 * 
	 */
    private static final Function<ResourceLocation, net.minecraft.client.renderer.RenderType> TRANSLUCENT_NO_CULL = Util.memoize((rl) -> {
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(rl, false, false))
                .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                .setCullState(NO_CULL)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setLayeringState(VIEW_OFFSET_Z_LAYERING)
                .createCompositeState(true);
        return create("armor_translucent_no_cull", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS,
                256, true, false,
                state);
    });
    public static net.minecraft.client.renderer.RenderType getTranslucentNoCull(ResourceLocation rl) {
        return TRANSLUCENT_NO_CULL.apply(rl);
    }
	////////
    
    
    protected static final RenderStateShard.TransparencyStateShard WORM_TRANSPARANCY = new RenderStateShard.TransparencyStateShard("translucent_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static final RenderStateShard.ShaderStateShard RENDERTYPE_COLLECTOR_PORTAL_SHADER = new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEndPortalShader);

	private RenderType(String nameIn, VertexFormat formatIn, VertexFormat.Mode drawModeIn, int bufferSizeIn, boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn)
	{
		super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
	}

    private static final TransparencyStateShard GHOST_TRANSPARENCY = new TransparencyStateShard("ghost_transparency",
            () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.CONSTANT_ALPHA, GlStateManager.DestFactor.ONE_MINUS_CONSTANT_ALPHA);
//                long i = Util.getMillis() * 4L;
//                float f1 = (float)(i % 30000L) / 30000.0F;
                GL14.glBlendColor(1.0F, 1.0F, 1.0F, 0.35F);
            },
            () -> {
                GL14.glBlendColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    protected static final RenderStateShard.TransparencyStateShard BLENDED = new RenderStateShard.TransparencyStateShard("blended", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });
    public static net.minecraft.client.renderer.RenderType getTextBlended(ResourceLocation locationIn) {
        CompositeState state = CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(locationIn, false, false))
                .setShaderState(RenderStateShard.RENDERTYPE_TEXT_SHADER)
                .setTransparencyState(BLENDED)
                .setLightmapState(NO_LIGHTMAP).createCompositeState(false);
        return create("text_blended", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, false, true, state);
    }
    
    public static final net.minecraft.client.renderer.RenderType GHOST = create("witherutils:ghost", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 2097152, true, false, net.minecraft.client.renderer.RenderType.CompositeState.builder()
                .setLightmapState(LIGHTMAP)
                .setShaderState(RENDERTYPE_SOLID_SHADER)
                .setTextureState(BLOCK_SHEET)
                .setTransparencyState(GHOST_TRANSPARENCY)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false)
    );
	public static final net.minecraft.client.renderer.RenderType LASER = create(WitherUtils.MODID + ":laserlaser", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, BUFFERSIZE, CRUMBLING, SORT, net.minecraft.client.renderer.RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
			.setLayeringState(VIEW_OFFSET_Z_LAYERING)
			.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
			.setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
			.setCullState(NO_CULL)
			.setLightmapState(NO_LIGHTMAP)
			.setWriteMaskState(COLOR_WRITE)
			.createCompositeState(false));

	public static final net.minecraft.client.renderer.RenderType FLUIDTANKRESIZABLE = create(WitherUtils.MODID + ":resizable_cuboid", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false, net.minecraft.client.renderer.RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_CUTOUT_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
			.setCullState(CULL)
			.setLightmapState(LIGHTMAP)
			.setWriteMaskState(COLOR_WRITE)
			.setLightmapState(LIGHTMAP)
			.createCompositeState(true));

	public static final net.minecraft.client.renderer.RenderType ITEMSTACKRESIZABLE = create(WitherUtils.MODID + ":resizable_cuboid_item", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false, net.minecraft.client.renderer.RenderType.CompositeState.builder()
			.setShaderState(RENDERTYPE_SOLID_SHADER)
			.setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
			.setCullState(NO_CULL)
			.setLightmapState(LIGHTMAP)
			.setWriteMaskState(COLOR_WRITE)
			.setLightmapState(LIGHTMAP)
			.createCompositeState(true));

	public static final net.minecraft.client.renderer.RenderType ITEMSTACKRESIZABLE_SOLID = create(WitherUtils.MODID + ":resizable_cuboid_solid", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false, net.minecraft.client.renderer.RenderType.CompositeState.builder()
			.setLightmapState(LIGHTMAP)
			.setShaderState(RENDERTYPE_SOLID_SHADER)
			.setTextureState(BLOCK_SHEET_MIPPED)
			.createCompositeState(true));

//	public static final RenderType COLLECTOR_PORTAL = create("collector_portal", DefaultVertexFormat.POSITION, VertexFormat.Mode.QUADS, 256, false, false, RenderType.CompositeState.builder()
//			.setShaderState(RENDERTYPE_COLLECTOR_PORTAL_SHADER)
//			.setTextureState(RenderStateShard.MultiTextureStateShard.builder()
//					.add(CollectorRenderer.SKY_LOCATION, false, false)
//					.add(CollectorRenderer.PORTAL_LOCATION, false, false).build())
//			.createCompositeState(false));

    public static net.minecraft.client.renderer.RenderType standard(ResourceLocation resourceLocation) {
        return STANDARD.apply(resourceLocation);
    }

    private static final Function<ResourceLocation, net.minecraft.client.renderer.RenderType> STANDARD = Util.memoize(resourceLocation -> {
        net.minecraft.client.renderer.RenderType.CompositeState state = net.minecraft.client.renderer.RenderType.CompositeState.builder()
              .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_SOLID_SHADER)
              .setTextureState(new RenderStateShard.TextureStateShard(resourceLocation, false, false))
              .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
              .createCompositeState(true);
        return create("wu_standard", DefaultVertexFormat.NEW_ENTITY, Mode.QUADS, 256, true, false, state);
    });

    /*
     * 
     * EMISSIVE
     * 
     */
    public static net.minecraft.client.renderer.RenderType eyes(ResourceLocation p_110489_)
    {
        RenderStateShard.TextureStateShard renderstateshard$texturestateshard = new RenderStateShard.TextureStateShard(p_110489_, false, false);
        
        return create("eyes", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, net.minecraft.client.renderer.RenderType.CompositeState.builder()
        		.setShaderState(RENDERTYPE_EYES_SHADER)
        		.setTextureState(renderstateshard$texturestateshard)
        		.setTransparencyState(ADDITIVE_TRANSPARENCY)
        		.setWriteMaskState(COLOR_WRITE)
        		.createCompositeState(false));
    }
    public static net.minecraft.client.renderer.RenderType getEyesFlickering(ResourceLocation p_228652_0_, float lightLevel)
    {
        RenderStateShard.TextureStateShard lvt_1_1_ = new RenderStateShard.TextureStateShard(p_228652_0_, false, false);
        
        return create("eyes_flickering", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, false, true, net.minecraft.client.renderer.RenderType.CompositeState.builder()
        		.setTextureState(lvt_1_1_)
        		.setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_CULL_SHADER)
        		.setTransparencyState(TRANSLUCENT_TRANSPARENCY)
        		.setCullState(NO_CULL)
        		.setLightmapState(LIGHTMAP)
        		.setOverlayState(OVERLAY)
        		.createCompositeState(false));
    }

	public static float[] speedMap;
	public static Color color;
	
	public static float red;
	public static float green;
	public static float blue;
    
	protected static final RenderStateShard.TransparencyStateShard ANIMATED_EYES_ALPHA = new RenderStateShard.TransparencyStateShard("animated_eyes_alpha", () -> {
		
		Minecraft mc = Minecraft.getInstance();
		float time = mc.level.getLevelData().getGameTime() + TickHelper.getClientTickCount();
		double nature = Math.sin(time * 0.2f) * Mth.floor(time * 0.1f) / 1000;
		float alpha = (float) Mth.clampedMap(nature, -6, 6, speedMap[0], speedMap[1]);
		
		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		RenderSystem.setShaderColor(red, green, blue, alpha);
	}, () -> {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
	});
	
	public static net.minecraft.client.renderer.RenderType eyesAnimatedWithAlpha(ResourceLocation location, float[] spMap, Color clr)
	{
		return makeAlphaAnimEyes(location, spMap, clr);
	}

	protected static net.minecraft.client.renderer.RenderType makeAlphaAnimEyes(ResourceLocation location, float[] spMap, Color clr)
	{
		speedMap = new float[] { spMap[0], spMap[1] };
	    red = clr.getRed();
	    green = clr.getGreen();
	    blue = clr.getBlue();
		
        CompositeState state = CompositeState.builder().setTextureState(new TextureStateShard(location, false, false))
                .setTransparencyState(ANIMATED_EYES_ALPHA)
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setCullState(NO_CULL)
                .setLightmapState(NO_LIGHTMAP)
                .createCompositeState(true);
        
        return create("animated_eyes_alpha", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, true, true, state);
	}

    public static net.minecraft.client.renderer.RenderType getMungusBeam(ResourceLocation guardianBeamTexture)
    {
        net.minecraft.client.renderer.RenderType.CompositeState rendertype$compositestate = net.minecraft.client.renderer.RenderType.CompositeState.builder().setShaderState(RENDERTYPE_EYES_SHADER).setTextureState(new RenderStateShard.TextureStateShard(guardianBeamTexture, false, false)).setTransparencyState(WORM_TRANSPARANCY).setCullState(NO_CULL).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY).createCompositeState(true);
        return create("mungus", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$compositestate);
    }
    
	public static final net.minecraft.client.renderer.RenderType SOLID_COLOUR = create(WitherUtils.MODID + ":solidcolour",
			DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, BUFFERSIZE, CRUMBLING, SORT,
			net.minecraft.client.renderer.RenderType.CompositeState.builder()
					.setShaderState(RenderStateShard.ShaderStateShard.RENDERTYPE_LINES_SHADER)
					.setLayeringState(VIEW_OFFSET_Z_LAYERING).setOutputState(ITEM_ENTITY_TARGET)
					.setTransparencyState(ADDITIVE_TRANSPARENCY).setTextureState(NO_TEXTURE)
					.setDepthTestState(NO_DEPTH_TEST).setCullState(CULL).setLightmapState(NO_LIGHTMAP)
					.setWriteMaskState(COLOR_DEPTH_WRITE).createCompositeState(false));

	  public static final net.minecraft.client.renderer.RenderType RESIZABLE = create(WitherUtils.MODID + ":resizable_cuboid", DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP, VertexFormat.Mode.QUADS, 256, true, false,
		      net.minecraft.client.renderer.RenderType.CompositeState.builder()
		          .setShaderState(RENDERTYPE_CUTOUT_SHADER)
		          .setTextureState(new RenderStateShard.TextureStateShard(InventoryMenu.BLOCK_ATLAS, false, false))
		          .setCullState(CULL)
		          .setLightmapState(LIGHTMAP)
		          .setWriteMaskState(COLOR_WRITE)
		          .setLightmapState(LIGHTMAP)
		          .createCompositeState(true));

	protected static final RenderStateShard.TransparencyStateShard BLENDED_NO_DEPT = new RenderStateShard.TransparencyStateShard("blended_no_dept", () -> {

		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
	}, () -> {
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
	});

	protected static final RenderStateShard.TransparencyStateShard BLENDED_NO_DEPT_ANIM = new RenderStateShard.TransparencyStateShard("blended_no_dept", () -> {
		
		Minecraft mc = Minecraft.getInstance();
		float time = mc.level.getLevelData().getGameTime() + TickHelper.getClientTickCount();
		double nature = Math.sin(time * 0.1f) / 1;
		double end = Mth.clampedMap(nature, -1, 1, 1, 10);
		
		float f = mc.player.getTicksUsingItem() / 20.0F;
		f = f > 1.0F ? 1.0F : f * f;

		RenderSystem.enableBlend();
		RenderSystem.depthMask(false);
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
		if(mc.player.getTicksUsingItem() != 0)
			RenderSystem.setShaderColor((float) f * 255, 0, 0, 0.5f);
		else
			RenderSystem.setShaderColor(255, 255, 255, (float) end);
	}, () -> {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.disableBlend();
		RenderSystem.depthMask(true);
		RenderSystem.defaultBlendFunc();
	});
	
	public static net.minecraft.client.renderer.RenderType entityBlendedNoDept(ResourceLocation location, boolean animated)
	{
		return makeBlendNoDept(location, animated);
	}

	public static net.minecraft.client.renderer.RenderType makeBlendNoDept(ResourceLocation location, boolean animated)
	{
        CompositeState state = CompositeState.builder().setTextureState(new TextureStateShard(location, false, false))
            .setTransparencyState(animated ? BLENDED_NO_DEPT_ANIM : BLENDED_NO_DEPT)
            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
            .setCullState(NO_CULL)
            .setLightmapState(NO_LIGHTMAP).createCompositeState(true);
        return create("blend_bo_dept", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 256, true, true, state);
	}
	
	/*
	 * 
	 * VANISHING
	 * 
	 */
	public static float vanishingTime;
	
	protected static final RenderStateShard.TransparencyStateShard VANSISHING_NO_DEPTH = new RenderStateShard.TransparencyStateShard("vanishing_no_depth", () -> {
		RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value,
        				GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ZERO.value);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, vanishingTime);
	}, () -> {
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
	});
	
	public static final net.minecraft.client.renderer.RenderType makeVansishingNoDepth()
	{
        CompositeState state = CompositeState.builder()
    		.setTextureState(net.minecraft.client.renderer.RenderType.BLOCK_SHEET_MIPPED)
            .setTransparencyState(VANSISHING_NO_DEPTH)
            .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
            .setCullState(net.minecraft.client.renderer.RenderType.NO_CULL)
            .setLightmapState(net.minecraft.client.renderer.RenderType.LIGHTMAP)
    		.setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, vanishingTime >= 1.0f))
            .createCompositeState(true);
        return create("vanish_no_depth", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 786432, true, true, state);
	}
	
//  RenderSystem.depthMask(pWriteDepth);
//  RenderSystem.colorMask(pWriteColor, pWriteColor, pWriteColor, pWriteColor);
	
//	TYPE_TRANSPARENCY = new TransparencyStateShard("typeent_transparency",
//			() -> {
//		        RenderSystem.enableBlend();
//		        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
//		        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.value, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.value,
//		                GlStateManager.SourceFactor.ONE.value, GlStateManager.DestFactor.ZERO.value);
//		        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alphaDriver);
//		    },
//		    () -> {
//		        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//		        RenderSystem.disableBlend();
//		        RenderSystem.defaultBlendFunc();
//		    });
//	RENDER_TYPE = RenderType.create("render_typeent", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 786432, true, true, CompositeState.builder()
//			.setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeTranslucentShader))
//			.setTextureState(RenderType.BLOCK_SHEET_MIPPED)
//			.setLightmapState(RenderType.LIGHTMAP)
//			.setTransparencyState(TYPE_TRANSPARENCY)
//			.setOutputState(RenderType.TRANSLUCENT_TARGET)
//			.setOverlayState(RenderType.OVERLAY)
//			.setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, !te.isPowered())) // new WriteMaskStateShard(true, true))
//			.setDepthTestState(RenderType.LEQUAL_DEPTH_TEST)
//			.setLayeringState(RenderType.VIEW_OFFSET_Z_LAYERING)
//			.createCompositeState(true));

//	GL11C.glDepthRange(0, 1);
//	GL11C.glDepthMask(false);
//	GlStateManager._depthFunc(1);
	
    /*public static VertexConsumer getEsiBuffer(MultiBufferSource bufferSource, RenderType renderType, PoseStack.Pose pose)
    {
        return VertexMultiConsumer.create(new SheetedDecalTextureGenerator(bufferSource.getBuffer(ESI_TRANSLUCENT),
        		pose, 0.0078125F), bufferSource.getBuffer(renderType)
        );
    }

    public static final RenderType ESI_TRANSLUCENT = create(
        "esi_translucent",
        DefaultVertexFormat.POSITION_TEX,
        VertexFormat.Mode.QUADS,
        1536,
        RenderType.CompositeState.builder()
            .setShaderState(RENDERTYPE_GLINT_TRANSLUCENT_SHADER)
            .setTextureState(new RenderStateShard.TextureStateShard(RenderModularPickaxe.ESI_ITEM, true, false))
            .setWriteMaskState(COLOR_WRITE)
            .setCullState(NO_CULL)
            .setDepthTestState(EQUAL_DEPTH_TEST)
            .setTransparencyState(GLINT_TRANSPARENCY)
            .setTexturingState(GLINT_TEXTURING)
            .setOutputState(ITEM_ENTITY_TARGET)
            .createCompositeState(false)
    );*/

    public static final net.minecraft.client.renderer.RenderType FORCEFIELD = net.minecraft.client.renderer.RenderType.create(
            "witherutils_forcefield",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true, // isComposite
            net.minecraft.client.renderer.RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_ENTITY_TRANSLUCENT_EMISSIVE_SHADER)
                    .setTextureState(new TextureStateShard(TextureAtlas.LOCATION_BLOCKS, false, false))
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setCullState(NO_CULL)               // Beidseitig rendern
                    .setLightmapState(LIGHTMAP)
                    .setOverlayState(OVERLAY)
                    .setWriteMaskState(COLOR_WRITE)      // WICHTIG: Kein Depth Write!
                    .createCompositeState(false)
    );
}
