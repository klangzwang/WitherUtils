package geni.withercore.mixin;

import geni.witherapi.block.ICustomBlockProperties;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BlockBehaviour.Properties.class)
public class BlockBehaviourPropertiesMixin implements ICustomBlockProperties {

    @Unique
    private boolean myMod$checkNeighbour = false;
    @Unique
    private boolean myMod$checkArea = false;

    @Override
    public BlockBehaviour.Properties checkNeighbour(boolean enable) {
        this.myMod$checkNeighbour = enable;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public BlockBehaviour.Properties checkArea(boolean enable) {
        this.myMod$checkArea = enable;
        return (BlockBehaviour.Properties) (Object) this;
    }

    @Override
    public boolean isCheckingNeighbour() {
        return this.myMod$checkNeighbour;
    }

    @Override
    public boolean isCheckingArea() {
        return this.myMod$checkArea;
    }
}