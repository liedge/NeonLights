package liedge.neonlights;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class LightBlock extends Block
{
    private final LightColor lightColor;

    public LightBlock(LightColor lightColor)
    {
        super(Properties.of()
                .mapColor(lightColor.getMapColor())
                .instrument(NoteBlockInstrument.PLING)
                .strength(0.5f)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15));
        this.lightColor = lightColor;
    }

    public LightColor getLightColor()
    {
        return lightColor;
    }

    public static String colorBlockName(DyeColor color)
    {
        return color.getSerializedName() + "_neon_light";
    }
}