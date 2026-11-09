package geni.withercore.mixin;

import geni.withercore.init.WUTFluids;
import geni.witherutils.client.block.render.fluid.LiquidsBlockRenderer;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LiquidBlockRenderer.class)
public class LiquidBlockRendererMixin {

    @Inject(
            method = "tesselate",
            at = @At("HEAD"),
            cancellable = true
    )
    private void interceptFluidRendering(
            BlockAndTintGetter level,
            BlockPos pos,
            VertexConsumer buffer,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci
    ) {
        if (fluidState.is(WUTFluids.COLDSLUSH.get())) {
            LiquidsBlockRenderer.tesselate(level, pos, buffer, blockState, fluidState);
            ci.cancel();
        }
    }
}