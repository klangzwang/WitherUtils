package geni.witherutils.base.client.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;

import geni.witherutils.base.client.ClientSetup;
import geni.witherutils.core.common.math.Vector3;
import geni.witherutils.core.common.util.MathUtil;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import org.joml.Quaternionf;
import org.joml.Vector3f;

@OnlyIn(Dist.CLIENT)
public class GuardianChargeParticle extends SingleQuadParticle {

    private final Vector3 startPos;
    private final Vector3 endPos;
    private final double angularPos;
//    private PhaseManager phaseManager;
    public TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(ClientSetup.HALO);
    
    public GuardianChargeParticle(ClientLevel world, Vector3 startPos, Vector3 endPos, double angularPos, int life)
    {//, PhaseManager phaseManager) {
        super(world, startPos.x, startPos.y, startPos.z);
        this.startPos = startPos;
        this.endPos = endPos;
        this.angularPos = angularPos;
//        this.phaseManager = phaseManager;
        lifetime = life * 4;
        setColor(0.75F, 0F, 0F);
        scale(5);
    }

    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
//        if (!(phaseManager.getCurrentPhase() instanceof ChargeUpPhase))
//        {
//            alpha -= 0.1;
//        }
        if (this.age++ >= this.lifetime || alpha <= 0)
        {
            this.remove();
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks)
    {
        if (age + partialTicks > lifetime) return;
        Vec3 vector3d = camera.getPosition();
        float anim = (age + partialTicks) / lifetime;
        Vector3 pos = MathUtil.interpolateVec3(startPos, endPos, anim);
        float radius = (anim * 2) + (Mth.sin(anim * (float) Math.PI) * 5);
        float x = (float) (pos.x - vector3d.x()) + (Mth.sin((float) (angularPos * Math.PI * 2) + anim) * radius);
        float y = (float) (pos.y - vector3d.y());
        float z = (float) (pos.z - vector3d.z()) + (Mth.cos((float) (angularPos * Math.PI * 2) + anim) * radius);
        Quaternionf quaternion;
        if (this.roll == 0.0F) {
            quaternion = camera.rotation();
        } else {
            quaternion = new Quaternionf(camera.rotation());
            float f3 = Mth.lerp(partialTicks, this.oRoll, this.roll);
            quaternion.mul(Axis.ZP.rotation(f3));
        }

        Vector3f vector3f1 = new Vector3f(-1.0F, -1.0F, 0.0F);
        vector3f1.rotate(quaternion);
        Vector3f[] avector3f = new Vector3f[]{new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F), new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)};
        float f4 = this.getQuadSize(partialTicks);

        for (int i = 0; i < 4; ++i) {
            Vector3f vector3f = avector3f[i];
            vector3f.rotate(quaternion);
            vector3f.mul(f4);
            vector3f.add(x, y, z);
        }

        float uMin = this.getU0();
        float uMax = this.getU1();
        float vMin = this.getV0();
        float vMax = this.getV1();
        int j = 240;
        buffer.addVertex(avector3f[0].x(), avector3f[0].y(), avector3f[0].z()).setUv(uMax, vMax).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setColor(j);
        buffer.addVertex(avector3f[1].x(), avector3f[1].y(), avector3f[1].z()).setUv(uMax, vMin).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setColor(j);
        buffer.addVertex(avector3f[2].x(), avector3f[2].y(), avector3f[2].z()).setUv(uMin, vMin).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setColor(j);
        buffer.addVertex(avector3f[3].x(), avector3f[3].y(), avector3f[3].z()).setUv(uMin, vMax).setColor(this.rCol, this.gCol, this.bCol, this.alpha).setColor(j);
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    protected float getU0() {
        return sprite.getU0();
    }

    @Override
    protected float getU1() {
        return sprite.getU1();
    }

    @Override
    protected float getV0() {
        return sprite.getV0();
    }

    @Override
    protected float getV1() {
        return sprite.getV1();
    }
    
    public static ParticleRenderType PARTICLE_SHEET_TRANSLUCENT = new ParticleRenderType()
    {
		@SuppressWarnings("deprecation")
		@Override
		public BufferBuilder begin(Tesselator pTesselator, TextureManager pTextureManager)
		{
			pTesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
            RenderSystem.depthMask(false);
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES);
			return null;
		}
        public String toString()
        {
            return "TERRAIN_SHEET_TRANSLUCENT";
        }
    };
}