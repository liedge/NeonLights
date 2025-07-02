package liedge.neonlights;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

public enum LightColor
{
    WHITE(DyeColor.WHITE),
    ORANGE(DyeColor.ORANGE),
    MAGENTA(DyeColor.MAGENTA),
    LIGHT_BLUE(DyeColor.LIGHT_BLUE),
    ENERGY_BLUE("energy_blue", MapColor.COLOR_LIGHT_BLUE, null),
    YELLOW(DyeColor.YELLOW),
    LIME(DyeColor.LIME),
    LTX_LIME("ltx_lime", MapColor.COLOR_LIGHT_GREEN, null),
    ELECTRIC_CHARTREUSE("electric_chartreuse", MapColor.COLOR_LIGHT_GREEN, null),
    PINK(DyeColor.PINK),
    GRAY(DyeColor.GRAY),
    LIGHT_GRAY(DyeColor.LIGHT_GRAY),
    CYAN(DyeColor.CYAN),
    PURPLE(DyeColor.PURPLE),
    BLUE(DyeColor.BLUE),
    NEURO_BLUE("neuro_blue", MapColor.WATER, null),
    BROWN(DyeColor.BROWN),
    GREEN(DyeColor.GREEN),
    ACID_GREEN("acid_green", MapColor.PLANT, null),
    RED(DyeColor.RED),
    BLACK(DyeColor.BLACK);

    private final String name;
    private final MapColor mapColor;

    @Nullable
    private final DyeColor dyeColor;

    LightColor(String name, MapColor mapColor, @Nullable DyeColor dyeColor)
    {
        this.name = name;
        this.mapColor = mapColor;
        this.dyeColor = dyeColor;
    }

    LightColor(DyeColor dyeColor)
    {
        this(dyeColor.getName(), dyeColor.getMapColor(), dyeColor);
    }

    public String getName()
    {
        return name;
    }

    public MapColor getMapColor()
    {
        return mapColor;
    }

    public @Nullable DyeColor getDyeColor()
    {
        return dyeColor;
    }
}