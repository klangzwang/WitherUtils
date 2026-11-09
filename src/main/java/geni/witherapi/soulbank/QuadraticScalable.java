package geni.witherapi.soulbank;

import java.util.function.Supplier;

public record QuadraticScalable(SoulBankModifier modifier, Supplier<Integer> base) implements SoulBankScalable {

    @Override
    public Supplier<Float> scaleF(Supplier<SoulBankData> data)
    {
        return () -> scale(base.get(), data.get().getModifier(modifier));
    }

    @Override
    public Supplier<Integer> scaleI(Supplier<SoulBankData> data)
    {
        return () -> Math.round(scale(base.get(), data.get().getModifier(modifier)));
    }

    private static float scale(int base, float level)
    {
        return base * level * level;
    }
}
