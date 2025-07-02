package liedge.neonlights;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.*;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static liedge.neonlights.NeonLights.*;

@Mod.EventBusSubscriber(modid = NeonLights.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NeonLightsDataGen
{
    private NeonLightsDataGen() {}

    public static final TagKey<Block> LIGHT_BLOCK_TAG = BlockTags.create(loc("light_blocks"));
    public static final TagKey<Item> LIGHT_ITEM_TAG = ItemTags.create(loc("light_blocks"));

    @SubscribeEvent
    public static void onGatherData(final GatherDataEvent event)
    {
        boolean runClient = event.includeClient();
        boolean runServer = event.includeServer();

        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> registries = event.getLookupProvider();
        ExistingFileHelper helper = event.getExistingFileHelper();

        BlockTagsGen blockTags = new BlockTagsGen(output, registries, helper);

        generator.addProvider(runServer, new RecipesGen(output));
        generator.addProvider(runServer, new LootTablesGen(output));
        generator.addProvider(runServer, blockTags);
        generator.addProvider(runServer, new ItemTagsGen(output, registries, blockTags.contentsGetter(), helper));

        generator.addProvider(runClient, new BlockStatesGen(output, helper));
        generator.addProvider(runClient, new LanguageGen(output));
    }

    private static class BlockTagsGen extends BlockTagsProvider
    {
        BlockTagsGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, @Nullable ExistingFileHelper helper)
        {
            super(output, registries, MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries)
        {
            IntrinsicTagAppender<Block> lightBlocks = tag(LIGHT_BLOCK_TAG);
            for (LightColor lightColor : LightColor.values())
            {
                lightBlocks.add(NeonLights.getLightBlock(lightColor));
            }

            tag(BlockTags.IMPERMEABLE).addTag(LIGHT_BLOCK_TAG);
            tag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(LIGHT_BLOCK_TAG);
        }
    }

    private static class ItemTagsGen extends ItemTagsProvider
    {
        ItemTagsGen(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper helper)
        {
            super(output, registries, blockTags, MODID, helper);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries)
        {
            copy(LIGHT_BLOCK_TAG, LIGHT_ITEM_TAG);
        }
    }

    private static class RecipesGen extends RecipeProvider
    {
        RecipesGen(PackOutput output)
        {
            super(output);
        }

        private void standardRecipe(ItemLike lightBlockItem, DyeColor dyeColor, Consumer<FinishedRecipe> output)
        {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, lightBlockItem, 16)
                    .define('m', Items.GLOWSTONE)
                    .define('d', dyeColor.getTag())
                    .unlockedBy("has_glowstone", has(Items.GLOWSTONE))
                    .pattern(" m ").pattern("mdm").pattern(" m ")
                    .save(output);
        }

        private ResourceLocation stonecuttingRecipeName(LightColor first, LightColor second)
        {
            return loc(String.format("%s_to_%s_neon_light", first.getName(), second.getName()));
        }

        private void stonecuttingRecipe(LightColor ingredientColor, LightColor resultColor, Consumer<FinishedRecipe> output)
        {
            ItemLike ingredientBlock = NeonLights.getLightBlock(ingredientColor);
            ItemLike resultBlock = NeonLights.getLightBlock(resultColor);

            // Forward conversion
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(ingredientBlock), RecipeCategory.BUILDING_BLOCKS, resultBlock)
                    .unlockedBy("has_light", has(ingredientBlock))
                    .save(output, stonecuttingRecipeName(ingredientColor, resultColor));

            // Backward conversion
            SingleItemRecipeBuilder.stonecutting(Ingredient.of(resultBlock), RecipeCategory.BUILDING_BLOCKS, ingredientBlock)
                    .unlockedBy("has_light", has(resultBlock))
                    .save(output, stonecuttingRecipeName(resultColor, ingredientColor));
        }

        @Override
        protected void buildRecipes(Consumer<FinishedRecipe> output)
        {
            // Standard colors
            NeonLights.LIGHT_BLOCKS.forEach((color, registration) ->
            {
                DyeColor dyeColor = color.getDyeColor();
                if (dyeColor != null)
                {
                    // Main crafting recipe
                    standardRecipe(registration.get(), dyeColor, output);
                }
            });

            // Non-standard colors
            stonecuttingRecipe(LightColor.LIGHT_BLUE, LightColor.ENERGY_BLUE, output);
            stonecuttingRecipe(LightColor.LIME, LightColor.LTX_LIME, output);
            stonecuttingRecipe(LightColor.LIME, LightColor.ELECTRIC_CHARTREUSE, output);
            stonecuttingRecipe(LightColor.BLUE, LightColor.NEURO_BLUE, output);
            stonecuttingRecipe(LightColor.GREEN, LightColor.ACID_GREEN, output);
        }
    }

    private static class LootTablesGen extends LootTableProvider
    {
        LootTablesGen(PackOutput output)
        {
            super(output, Set.of(), List.of(new SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)));
        }

        @Override
        protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationcontext) {}
    }

    private static class BlockLoot extends BlockLootSubProvider
    {
        BlockLoot()
        {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate()
        {
            NeonLights.LIGHT_BLOCKS.values().stream().map(RegistryObject::get).forEach(this::dropSelf);
        }

        @Override
        protected Iterable<Block> getKnownBlocks()
        {
            return NeonLights.LIGHT_BLOCKS.values().stream().map(RegistryObject::get).collect(Collectors.toList());
        }
    }

    private static class BlockStatesGen extends BlockStateProvider
    {
        private final ExistingFileHelper helper;

        BlockStatesGen(PackOutput output, ExistingFileHelper helper)
        {
            super(output, MODID, helper);
            this.helper = helper;
        }

        @Override
        protected void registerStatesAndModels()
        {
            ModelFile blockModel = new ModelFile.ExistingModelFile(blockFolderLocation("light_block"), helper);
            NeonLights.LIGHT_BLOCKS.forEach((color, registration) ->
            {
                String name = registration.getId().getPath();
                simpleBlockWithItem(registration.get(), models().getBuilder(name).parent(blockModel).texture("all", blockFolderLocation(name)));
            });
        }

        private ResourceLocation blockFolderLocation(String path)
        {
            return loc("block/" + path);
        }
    }

    private static class LanguageGen extends LanguageProvider
    {
        LanguageGen(PackOutput output)
        {
            super(output, MODID, "en_us");
        }

        @Override
        protected void addTranslations()
        {
            add(NeonLights.CREATIVE_TAB_LANG_KEY, "Neon Lights");

            NeonLights.LIGHT_BLOCKS.forEach((color, registration) ->
            {
                if (color != LightColor.LTX_LIME)
                {
                    String name = Arrays.stream(registration.getId().getPath().split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
                    addBlock(registration, name);
                }
            });
            add(NeonLights.getLightBlock(LightColor.LTX_LIME), "LTX Lime Neon Light");
        }
    }
}