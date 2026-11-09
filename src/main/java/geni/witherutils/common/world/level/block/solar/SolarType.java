package geni.witherutils.common.world.level.block.solar;

import javax.annotation.Nonnull;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public enum SolarType implements StringRepresentable {

	NONE(0, "none"),
	BASIC(1, "solarbasic"),
	ADVANCED(2, "solaradv"),
	ULTRA(3, "solarultra");

	private int type;
	private final String name;
	public static final SolarType[] BY_ID = values();

    SolarType(int type, String name)
    {
    	this.name = name;
    	this.type = type;
    }

    public SolarType getType()
    {
        return BY_ID[this.type];
    }
	@Override
	public String getSerializedName()
	{
		return this.name;
	}
	public boolean connectTo(@Nonnull SolarType other)
	{
		return this == other;
	}
	public static @Nonnull SolarType getTheType()
	{
		return values()[values().length];
	}
	public static @Nonnull SolarType getType(int type)
	{
		return (values()[type >= 0 && type < values().length ? type : 0]);
	}
	public static @Nonnull SolarType getType(@Nonnull ItemStack stack)
	{
		return getType(stack.getDamageValue());
	}
	public static int getValue(@Nonnull SolarType value)
	{
		return value.ordinal();
	}
	public static EnumProperty<SolarType> getValue(@Nonnull EnumProperty<SolarType> value)
	{
		return value;
	}
	public static int getValueFromType(@Nonnull SolarType solarType)
	{
		return solarType.ordinal();
	}
}
