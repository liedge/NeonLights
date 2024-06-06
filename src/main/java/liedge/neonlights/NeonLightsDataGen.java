package liedge.neonlights;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = NeonLights.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NeonLightsDataGen
{
    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event)
    {
        boolean runClient = event.includeClient();
        boolean runServer = event.includeServer();

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        generator.addProvider(runServer, new Recipes(output));
        generator.addProvider(runServer, new LootTables(output));
        generator.addProvider(runServer, new BlockTagsGen(output, lookupProvider, helper));

        generator.addProvider(runClient, new BlockStates(output, helper));
        generator.addProvider(runClient, new Language(output));
    }

    private static class BlockTagsGen extends BlockTagsProvider
    {
        public BlockTagsGen(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
        {
            super(output, lookupProvider, NeonLights.MODID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider provider)
        {
            IntrinsicTagAppender<Block> impermeable = tag(BlockTags.IMPERMEABLE);
            NeonLights.LIGHT_BLOCKS.values().forEach(pair -> impermeable.add(pair.getA().get()));
        }
    }

    private static class Recipes extends RecipeProvider
    {
        public Recipes(PackOutput output)
        {
            super(output);
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> consumer)
        {
            for (DyeColor color : DyeColor.values())
            {
                RegistryObject<NeonLightBlock> registryObject = NeonLights.LIGHT_BLOCKS.get(color).getA();

                TagKey<Item> dyeKey = switch (registryObject.get().getColor())
                {
                    case WHITE -> Tags.Items.DYES_WHITE;
                    case ORANGE -> Tags.Items.DYES_ORANGE;
                    case MAGENTA -> Tags.Items.DYES_MAGENTA;
                    case LIGHT_BLUE -> Tags.Items.DYES_LIGHT_BLUE;
                    case YELLOW -> Tags.Items.DYES_YELLOW;
                    case LIME -> Tags.Items.DYES_LIME;
                    case PINK -> Tags.Items.DYES_PINK;
                    case GRAY -> Tags.Items.DYES_GRAY;
                    case LIGHT_GRAY -> Tags.Items.DYES_LIGHT_GRAY;
                    case CYAN -> Tags.Items.DYES_CYAN;
                    case PURPLE -> Tags.Items.DYES_PURPLE;
                    case BLUE -> Tags.Items.DYES_BLUE;
                    case BROWN -> Tags.Items.DYES_BROWN;
                    case GREEN -> Tags.Items.DYES_GREEN;
                    case RED -> Tags.Items.DYES_RED;
                    case BLACK -> Tags.Items.DYES_BLACK;
                };

                ShapedRecipeBuilder
                        .shaped(RecipeCategory.BUILDING_BLOCKS, registryObject.get(), 16)
                        .define('g', Blocks.GLOWSTONE)
                        .define('d', dyeKey)
                        .pattern(" g ").pattern("gdg").pattern(" g ")
                        .unlockedBy("get_glowstone", has(Blocks.GLOWSTONE))
                        .group("neon_lights")
                        .save(consumer);
            }
        }
    }

    private static class LootTables extends LootTableProvider
    {
        public LootTables(PackOutput output)
        {
            super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationcontext) {}
    }

    private static class BlockLoot extends BlockLootSubProvider
    {
        public BlockLoot()
        {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), new Object2ObjectOpenHashMap<>());
        }

        @Override
        protected void generate()
        {
            NeonLights.LIGHT_BLOCKS.values().forEach(pair -> dropSelf(pair.getA().get()));
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return NeonLights.LIGHT_BLOCKS.values().stream().map(pair -> pair.getA().get()).collect(Collectors.toList());
        }
    }

    private static class BlockStates extends BlockStateProvider
    {
        private final ExistingFileHelper helper;

        public BlockStates(PackOutput output, ExistingFileHelper helper)
        {
            super(output, NeonLights.MODID, helper);
            this.helper = helper;
        }

        @Override
        protected void registerStatesAndModels()
        {
            ModelFile lightBlockBase = new ModelFile.ExistingModelFile(blockFolderLocation("light_block"), helper);
            for (DyeColor color : DyeColor.values())
            {
                RegistryObject<NeonLightBlock> registryObject = NeonLights.LIGHT_BLOCKS.get(color).getA();
                NeonLightBlock block = registryObject.get();
                simpleBlockWithItem(block, models().getBuilder(registryObject.getId().getPath()).parent(lightBlockBase).texture("all", blockFolderLocation(block.getColor().getSerializedName())));
            }
        }

        private ResourceLocation blockFolderLocation(String path)
        {
            return new ResourceLocation(NeonLights.MODID, "block/" + path);
        }
    }

    private static class Language extends LanguageProvider
    {
        public Language(PackOutput output)
        {
            super(output, NeonLights.MODID, "en_us");
        }

        @Override
        protected void addTranslations()
        {
            add("creative_tab.neonlights.main", "Neon Lights");

            for (DyeColor color : DyeColor.values())
            {
                RegistryObject<NeonLightBlock> registryObject = NeonLights.LIGHT_BLOCKS.get(color).getA();
                String name = Arrays.stream(registryObject.getId().getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
                addBlock(registryObject, name);
            }
        }
    }
}