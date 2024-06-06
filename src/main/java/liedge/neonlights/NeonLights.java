package liedge.neonlights;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import oshi.util.tuples.Pair;

import java.util.EnumMap;
import java.util.Map;

import static liedge.neonlights.NeonLightBlock.colorBlockName;

@Mod(NeonLights.MODID)
public class NeonLights
{
    public static final String MODID = "neonlights";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Map<DyeColor, Pair<RegistryObject<NeonLightBlock>, RegistryObject<Item>>> LIGHT_BLOCKS = Util.make(() -> {
        Map<DyeColor, Pair<RegistryObject<NeonLightBlock>, RegistryObject<Item>>> map = new EnumMap<>(DyeColor.class);

        for (DyeColor color : DyeColor.values())
        {
            RegistryObject<NeonLightBlock> blockRegistryObject = BLOCKS.register(colorBlockName(color), () -> new NeonLightBlock(color));
            RegistryObject<Item> itemRegistryObject = ITEMS.register(colorBlockName(color), () -> new BlockItem(blockRegistryObject.get(), new Item.Properties()));
            map.put(color, new Pair<>(blockRegistryObject, itemRegistryObject));
        }

        return map;
    });

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("creative_tab.neonlights.main"))
            .icon(() -> LIGHT_BLOCKS.get(DyeColor.LIME).getB().get().getDefaultInstance())
            .displayItems((params, output) -> ITEMS.getEntries().forEach(ro -> output.accept(ro.get()))).build());

    public NeonLights()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CREATIVE_MODE_TABS.register(modBus);

        modBus.addListener(this::addToTabs);
    }

    private void addToTabs(final BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey().equals(CreativeModeTabs.COLORED_BLOCKS))
        {
            LIGHT_BLOCKS.values().forEach(pair -> event.accept(pair.getB()));
        }
    }
}