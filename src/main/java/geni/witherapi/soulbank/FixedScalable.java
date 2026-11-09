package geni.witherapi.soulbank;

import java.util.function.Supplier;

public record FixedScalable(Supplier<Integer> value) implements SoulBankScalable {
	
    public static final FixedScalable ZERO = new FixedScalable(() -> 0);

    @Override
    public Supplier<Float> scaleF(Supplier<SoulBankData> data)
    {
        return () -> (float) value.get();
    }

    @Override
    public Supplier<Integer> scaleI(Supplier<SoulBankData> data)
    {
        return value;
    }
}
