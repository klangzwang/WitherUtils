package geni.witherapi.soulbank;

import net.minecraft.core.Registry;

import javax.annotation.Nonnull;

public interface ISoulBankModifier extends Registry<ISoulBankModifier> {

	default int get(float level)
	{
		return (int) getFloat(level);
	}

	default int get(@Nonnull SoulBankData data)
	{
		return (int) getFloat(data);
	}

	default int getDefault()
	{
		return (int) getDefaultFloat();
	}

	float getFloat(float level);

	default float getFloat(@Nonnull SoulBankData data)
	{
		return getFloat(data.getModifier(getValueType()));
	}

	default float getDefaultFloat()
	{
		return getFloat(1);
	}

	int getBaseValue();

	@Nonnull
	SoulBankModifier getValueType();

	void setBaseValue(int baseValue);

	void validate() throws UnconfiguredCapKeyException;

	class UnconfiguredCapKeyException extends RuntimeException {

		private static final long serialVersionUID = 307095860368579216L;

		public UnconfiguredCapKeyException(String message, Throwable cause)
		{
			super(message, cause);
		}

		public UnconfiguredCapKeyException(String message)
		{
			super(message);
		}
	}
}
