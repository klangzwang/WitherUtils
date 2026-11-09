package geni.witherapi.soulbank;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public record SoulBankData(float base, Map<SoulBankModifier, Float> modifiers) {

    public static final Codec<Map<SoulBankModifier, Float>> MODIFIER_CODEC = Codec.unboundedMap(SoulBankModifier.CODEC, Codec.FLOAT);

    public static final Codec<SoulBankData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
            Codec.FLOAT.fieldOf("base").forGetter(SoulBankData::base),
            MODIFIER_CODEC.fieldOf("specializations").forGetter(SoulBankData::modifiers)
        ).apply(instance, SoulBankData::new)
    );

    public static final StreamCodec<FriendlyByteBuf, Map<SoulBankModifier, Float>> MODIFIER_STREAM_CODEC
        = ByteBufCodecs.map(HashMap::new, SoulBankModifier.STREAM_CODEC, ByteBufCodecs.FLOAT);

    public static final StreamCodec<FriendlyByteBuf, SoulBankData> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT,
        SoulBankData::base,
        MODIFIER_STREAM_CODEC,
        SoulBankData::modifiers,
        SoulBankData::new
    );

    public static SoulBankData NONE = simple(0.0f);

    public static SoulBankData simple(float base)
    {
        return new SoulBankData(base, Map.of());
    }

    public float getModifier(SoulBankModifier modifier)
    {
        if (modifiers.containsKey(modifier))
        {
            return modifiers.get(modifier);
        }
        return base;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        SoulBankData that = (SoulBankData) o;
        return Float.compare(base, that.base) == 0 && Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode()
    {
        int result = Float.hashCode(base);
        result = 31 * result + Objects.hashCode(modifiers);
        return result;
    }
}
