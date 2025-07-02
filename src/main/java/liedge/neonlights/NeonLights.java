package liedge.neonlights;

import com.google.common.collect.ImmutableMap;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.EnumMap;
import java.util.Map;

@Mod(NeonLights.MODID)
public class NeonLights
{
    public static final String MODID = "neonlights";

    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    static final String CREATIVE_TAB_LANG_KEY = "creative_tab." + MODID + ".main";

    public static final Map<LightColor, RegistryObject<LightBlock>> LIGHT_BLOCKS = Util.make(() ->
    {
        Map<LightColor, RegistryObject<LightBlock>> map = new EnumMap<>(LightColor.class);
        for (LightColor color : LightColor.values())
        {
            map.put(color, BLOCKS.register(color.getName() + "_neon_light", () -> new LightBlock(color)));
        }
        return ImmutableMap.copyOf(map);
    });

    public static final Map<LightColor, RegistryObject<BlockItem>> LIGHT_BLOCK_ITEMS = Util.make(() ->
    {
        Map<LightColor, RegistryObject<BlockItem>> map = new EnumMap<>(LightColor.class);
        for (LightColor color : LightColor.values())
        {
            RegistryObject<BlockItem> registration = ITEMS.register(color.getName() + "_neon_light", () -> new BlockItem(getLightBlock(color), new Item.Properties()));
            map.put(color, registration);
        }
        return ImmutableMap.copyOf(map);
    });

    public static LightBlock getLightBlock(LightColor lightColor)
    {
        return LIGHT_BLOCKS.get(lightColor).get();
    }

    public static BlockItem getLightBlockItem(LightColor lightColor)
    {
        return LIGHT_BLOCK_ITEMS.get(lightColor).get();
    }

    public static final RegistryObject<CreativeModeTab> CREATIVE_TAB = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable(CREATIVE_TAB_LANG_KEY))
            .icon(() -> getLightBlockItem(LightColor.LTX_LIME).getDefaultInstance())
            .displayItems((params, output) -> LIGHT_BLOCK_ITEMS.values().forEach(ro -> output.accept(ro.get())))
            .build());

    public static ResourceLocation loc(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

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
            LIGHT_BLOCK_ITEMS.values().forEach(event::accept);
        }
    }
}