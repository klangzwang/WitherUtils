package geni.witherapi.soulbank;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;

import java.util.List;
import java.util.Locale;
import java.util.function.IntFunction;

public enum SoulBankModifier implements StringRepresentable {
	
    ENERGY_CAPACITY(1),
    ENERGY_USE(2),
    FUEL_EFFICIENCY(3),
    BURNING_ENERGY_GENERATION(4),
    FIXED(0);

    public static final Codec<SoulBankModifier> CODEC = StringRepresentable.fromEnum(SoulBankModifier::values);
    public static final IntFunction<SoulBankModifier> BY_ID = ByIdMap.continuous(key -> key.id, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final StreamCodec<ByteBuf, SoulBankModifier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, v -> v.id);

    private final int id;
    public final ResourceLocation modifierId;

    public static final List<SoulBankModifier> SELECTABLE_MODIFIERS = List.of(
        ENERGY_CAPACITY,
        ENERGY_USE,
        FUEL_EFFICIENCY,
        BURNING_ENERGY_GENERATION
    );

    public static SoulBankModifier getRandomModifier(RandomSource randomSource)
    {
        return SoulBankModifier.SELECTABLE_MODIFIERS.get(randomSource.nextInt(SoulBankModifier.SELECTABLE_MODIFIERS.size()));
    }

    SoulBankModifier(int id)
    {
        this.id = id;
        this.modifierId = ResourceLocation.fromNamespaceAndPath("witherutils", "soulbank." + name().toLowerCase(Locale.ROOT));
    }

    @Override
    public String getSerializedName()
    {
        return name().toLowerCase(Locale.ROOT);
    }
}
