package ml;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

import net.devtech.arrp.api.RRPCallback;
import net.devtech.arrp.api.RuntimeResourcePack;
import net.devtech.arrp.json.recipe.*;
import net.devtech.arrp.json.blockstate.JBlockModel;
import net.devtech.arrp.json.blockstate.JState;
import net.devtech.arrp.json.blockstate.JVariant;
import net.devtech.arrp.json.loot.*;
import net.devtech.arrp.json.models.JModel;
import net.devtech.arrp.json.models.JTextures;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

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
        return Registry.ITEM.getId(item);
    }

    private static Identifier getBlockId(BlockItem item) {
        return Registry.BLOCK.getId(item.getBlock());
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
            recipe = JRecipe.stonecutting(
                // Take src_item
                JIngredient.ingredient().item(src_item),
                // Give MAX_LAYERS of res_item
                JResult.itemStack(res_item, MAX_LAYERS)
            );
        } else {
            recipe = JRecipe.shaped(
                JPattern.pattern("##"),
                JKeys.keys().key(
                    "#",
                    JIngredient.ingredient().item(src_item)
                ),
                JResult.itemStack(res_item, MAX_LAYERS * 2)
            );
        }

        RESOURCE_PACK.addRecipe(getItemId(res_item), recipe);
    }

    // Loot table
    /**
     * Adds a loot table entry for a BlockLayer
     * @param item BlockLayer Item
     */

    public static void AddLootTable(BlockItem item) {
        JEntry root_entry = JLootTable.entry().type("minecraft:alternatives");

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
                JLootTable.entry()
                .type("minecraft:item")
                .condition(condition)
                .function(function)
                .name(getItemId(item).toString())
            ;

            root_entry.child(child_entry);
        }

        RESOURCE_PACK.addLootTable(
            createId(item, "blocks"),
            JLootTable.loot("minecraft:block")
            .pool(
                JLootTable.pool()
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
        JState state = new JState();
        
        JVariant variant = new JVariant();
        for (int i = 1; i <= MAX_LAYERS; i++) {
            variant.put("layers=" + i, new JBlockModel(createId(item, "block", i * HEIGHT_PER_LAYER)));
        }
        state.add(variant);
        
        RESOURCE_PACK.addBlockState(state, getItemId(item));
    }

    // Lang - Not doing this.

    // MAIN INTERFACE

    public static void GenerateBlockData(BlockItem res_item, Block src_block, boolean stonecutting) {
        BlockItem src_item = (BlockItem) src_block.asItem();
        
        AddRecipe(res_item, src_item, stonecutting);
        AddLootTable(res_item);
        AddBlockStates(res_item);        
        AddModels(res_item, src_item);
    }

    public static void RegisterCallback() {
        RRPCallback.AFTER_VANILLA.register(a -> a.add(RESOURCE_PACK));
    }
}
