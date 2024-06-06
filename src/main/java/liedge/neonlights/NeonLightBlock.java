package liedge.neonlights;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public class NeonLightBlock extends Block
{
    private final DyeColor color;

    public NeonLightBlock(DyeColor color)
    {
        super(Properties.of()
                .mapColor(color)
                .instrument(NoteBlockInstrument.PLING)
                .strength(0.5f)
                .sound(SoundType.GLASS)
                .lightLevel(state -> 15));
        this.color = color;
    }

    public DyeColor getColor()
    {
        return color;
    }

    public static String colorBlockName(DyeColor color)
    {
        return color.getSerializedName() + "_neon_light";
    }
}