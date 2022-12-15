package ml;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.recipe.*;
import net.devtech.arrp.json.tags.JTag;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JBlockStates;
import net.devtech.arrp.json.blockstate.JVariants;
import net.devtech.arrp.json.loot.*;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

public abstract class ResourcesManager {

    public static final int MAX_LAYERS = 8;
    public static final int HEIGHT_PER_LAYER = 2;
    // 8 * 2 = 16 pixels

    public static final String MOD_ID = "ml";

    // Special Cases handling
    static {
        special_cases = new HashMap<>();
        specialTop(Blocks.SANDSTONE);
        specialTop(Blocks.RED_SANDSTONE);
        specialTop(Blocks.GRASS_BLOCK);
        specialTop(Blocks.DIRT_PATH);
        specialTop(Blocks.MYCELIUM);
        specialTop(Blocks.PODZOL);
        specialTop(Blocks.BLACKSTONE);
        specialTop(Blocks.BASALT);
        specialTop(Blocks.HAY_BLOCK);
        specialTop(Blocks.DEEPSLATE);
    }
    
    public static final RuntimeResourcePack RESOURCE_PACK = RuntimeResourcePack.create(MOD_ID + ":dynamic_data");
    
    private static Identifier getItemId(BlockItem item) {
        return Registries.ITEM.getId(item);
    }

    private static Identifier getBlockId(BlockItem item) {
        return Registries.BLOCK.getId(item.getBlock());
    }

    /**
     * ONLY USE WITH ITEMS FROM THIS MOD
     */
    
    private static Identifier createId(BlockItem item, String prefix_dir, String suffix_name) {
        if (!prefix_dir.isEmpty()) prefix_dir += "/";

        if (!suffix_name.isEmpty()) suffix_name = "_" + suffix_name;

        return new Identifier(MOD_ID, prefix_dir + getItemId(item).getPath() + suffix_name);
    }


    /**
     * ONLY USE WITH ITEMS FROM THIS MOD
     */

    private static Identifier createId(BlockItem item, String prefix_dir, int suffix_num) {
        return createId(item, prefix_dir, String.valueOf(suffix_num));
    }

    /**
     * ONLY USE WITH ITEMS FROM THIS MOD
     */

    private static Identifier createId(BlockItem item, String prefix_dir) {
        return createId(item, prefix_dir, "");
    }

    // DATA

    // Recipe
    //  - Crafting table blocks / Shovel blocks
    //  - Stonecutter blocks / Pickaxe blocks

    /**
     * Adds a new recipe to the data pack
     * 
     * @param res_item Resulting Item from the crafting recipe
     * @param src_item Item to be used in the crafting recipe
     * @param stonecutting Set to <code>true</code> for stonecutting recipe, <code>false</code> for crafting table recipe
     */

    public static void AddRecipe(BlockItem res_item, BlockItem src_item, boolean stonecutting) {
        JRecipe recipe;

        if (stonecutting) {
            recipe = new JStonecuttingRecipe(src_item, res_item, MAX_LAYERS);
        } else {
            recipe = 
                new JShapedRecipe(res_item)
                .resultCount(MAX_LAYERS * 2)
                .pattern(new JPattern("##"))
                .addKey("#", src_item)
            ;
        }

        RESOURCE_PACK.addRecipe(getItemId(res_item), recipe);
    }

    // Loot table
    /**
     * Adds a loot table entry for a BlockLayer
     * @param item BlockLayer Item
     */

    public static void AddLootTable(BlockItem item) {
        JEntry root_entry = new JEntry().type("minecraft:alternatives");

        for (int i = 1; i <= MAX_LAYERS; i++) {
            
            /*
            {
                "layers": "<i>"
            }
            */
            JsonObject properties = new JsonObject();
            properties.addProperty("layers", String.valueOf(i));

            /*
            {
                "condition": "minecraft:block_state_property",
                "block": "<block/item ID>",
                "properties": {
                    <properties>
                }
            }
            */
            JCondition condition = 
                new JCondition("minecraft:block_state_property")
                .parameter("block", getBlockId(item))
                .parameter("properties", properties)
            ;

            /*
            {
                "function": "minecraft:set_count",
                "count": <i>
            }
            */
            JFunction function = 
                new JFunction("minecraft:set_count")
                .parameter("count", i)
            ;

            /*
            {
              "type": "minecraft:item",
              "conditions": [
                <condition>
              ],
              "functions": [
                <function>
              ],
              "name": "<item ID>"
            }
            */
            JEntry child_entry = 
                new JEntry()
                .type("minecraft:item")
                .condition(condition)
                .function(function)
                .name(getItemId(item).toString())
            ;

            root_entry.child(child_entry);
        }

        RESOURCE_PACK.addLootTable(
            createId(item, "blocks"),
            new JLootTable("minecraft:block")
            .pool(
                new JPool()
                .rolls(1)
                .entry(root_entry)
                .condition(
                    new JCondition("minecraft:survives_explosion")
                )
            )
        );
    }

    // ASSETS
    
    // Models
    // - Simple item model
    // - A bit more complicated block models (8 for each block...)
    // - Skip creating main "reference" model through code

    public static void AddModels(BlockItem res_item, BlockItem src_item) {
        addItemModel(res_item);
        addBlockModels(res_item, src_item);
    }

    private static void addItemModel(BlockItem item) {
        RESOURCE_PACK.addModel(new JModel().parent(createId(item, "block", "2").toString()), createId(item, "item"));
    }

    private static Map<Block, String> special_cases;

    private static void specialTop(Block block) {
        special_cases.put(block, "_top");
    }

    private static void addBlockModels(BlockItem res_item, BlockItem src_item) {
        for (int i = 1; i <= MAX_LAYERS; i++) {
            int pixel = i * HEIGHT_PER_LAYER;
            
            String texture_name = "block" + "/" + getBlockId(src_item).getPath();

            if (special_cases.containsKey(src_item.getBlock())) {
                texture_name += special_cases.get(src_item.getBlock());
            }

            JModel model = 
                new JModel()
                .parent(MOD_ID + ":block/layer_" + pixel)
                .textures(new JTextures().var("all", texture_name))
            ;
            
            RESOURCE_PACK.addModel(model, createId(res_item, "block", pixel));
        }
    }

    // Blockstates - to link with block models
    public static void AddBlockStates(BlockItem item) {
        JVariants variants = new JVariants();
        for (int i = 1; i <= MAX_LAYERS; i++) {	
            variants.addVariant("layers=" + i, new JBlockModel(createId(item, "block", i * HEIGHT_PER_LAYER)));
        }
        JBlockStates state = JBlockStates.ofVariants(variants);
        
        RESOURCE_PACK.addBlockState(state, getItemId(item));
    }

    public static Map<MiningTool, Pair<JTag, String>> tagMap;
    // Tool Tags
    static {
        tagMap = new HashMap<>();

        tagMap.put(MiningTool.PICKAXE, new Pair<>(new JTag(), "pickaxe"));
        tagMap.put(MiningTool.SHOVEL, new Pair<>(new JTag(), "shovel"));
        tagMap.put(MiningTool.HOE, new Pair<>(new JTag(), "hoe"));
    }

    // Tag - put block id in correct tag to mine blocks
    public static void AddToTag(BlockItem item, MiningTool tool) {
        tagMap.get(tool).getLeft().add(getBlockId(item));
    }

    public static void PushTags() {
        tagMap.forEach((k, v) -> {
            RESOURCE_PACK.addTag(new Identifier("minecraft", "blocks/mineable/" + v.getRight()), v.getLeft());
        });
    }

    // Lang - Not doing this.

    // MAIN INTERFACE

    public static void GenerateBlockData(BlockItem res_item, Block src_block, MiningTool tool) {
        BlockItem src_item = (BlockItem) src_block.asItem();
        
        // Create stonecutting recipies for pickaxe blocks
        boolean stonecutting = tool == MiningTool.PICKAXE;

        AddRecipe(res_item, src_item, stonecutting);
        AddLootTable(res_item);
        AddBlockStates(res_item);        
        AddModels(res_item, src_item);
        AddToTag(res_item, tool);
    }

    public static void RegisterCallback() {
        RRPCallback.BEFORE_VANILLA.register(a -> a.add(RESOURCE_PACK));
    }
}
