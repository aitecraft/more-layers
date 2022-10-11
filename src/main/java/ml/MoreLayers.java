package ml;

import java.util.Hashtable;
import java.util.Map;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameMode;

public class MoreLayers implements ModInitializer {
    public static ItemGroup itemGroup;
    
    
    // Concrete Powder Stuff (FALLING)
    public static Block white_concrete_powder_layer;
    public static Block orange_concrete_powder_layer;
    public static Block magenta_concrete_powder_layer;
    public static Block light_blue_concrete_powder_layer;
    public static Block yellow_concrete_powder_layer;
    public static Block lime_concrete_powder_layer;
    public static Block pink_concrete_powder_layer;
    public static Block gray_concrete_powder_layer;
    public static Block light_gray_concrete_powder_layer;
    public static Block cyan_concrete_powder_layer;
    public static Block purple_concrete_powder_layer;
    public static Block blue_concrete_powder_layer;
    public static Block brown_concrete_powder_layer;
    public static Block green_concrete_powder_layer;
    public static Block red_concrete_powder_layer;
    public static Block black_concrete_powder_layer;
    
    // Sand and Gravel (FALLING)
    public static Block gravel_layer;
    public static Block red_sand_layer;
    public static Block sand_layer;
    
    // Leaves
    public static Block acacia_leaves_layer;
    public static Block birch_leaves_layer;
    public static Block dark_oak_leaves_layer;
    public static Block jungle_leaves_layer;
    public static Block oak_leaves_layer;
    public static Block spruce_leaves_layer;
    // 1.17 Leaves
    public static Block azalea_leaves_layer;
    public static Block flowering_azalea_leaves_layer;
    // 1.19 Leaves
    public static Block mangrove_leaves_layer;
    
    // Stone and Variants
    public static Block andesite_layer;
    public static Block cobblestone_layer;
    public static Block diorite_layer;
    public static Block granite_layer;
    public static Block mossy_cobblestone_layer;
    public static Block stone_layer;
    public static Block sandstone_layer;
    public static Block red_sandstone_layer;
    // 1.17 Stuff
    public static Block cobbled_deepslate_layer;
    public static Block deepslate_layer;
    
    // Dirt and Grass Variants
    public static Block coarse_dirt_layer;
    public static Block dirt_layer;
    public static Block grass_block_layer;
    public static Block dirt_path_layer;
    public static Block mycelium_layer;
    public static Block podzol_layer;
    // 1.19 Stuff
    public static Block mud_layer;
    
    // Nether Blocks
    public static Block nether_rack_layer;
    public static Block blackstone_layer;
    public static Block basalt_layer;
    public static Block crimson_nylium_layer;
    public static Block warped_nylium_layer;

    // Misc. Blocks
    public static Block obsidian_layer;
    public static Block crying_obsidian_layer;
    public static Block hay_block_layer;
    public static Block end_stone_layer;

    // Minecraft Block -> Blocklayer Block
    public static Map<Block, Block> blockConversions;

    // BlockLayer Block -> Mining Tool
    public static Map<Block, MiningTool> blockTools;
    
    public void onInitialize() {
        MoreLayers.itemGroup = FabricItemGroupBuilder.create(new Identifier("ml", "layers")).appendItems(itemStacks -> Registry.ITEM.forEach(item -> {
            if (Registry.ITEM.getId(item).getNamespace().equals("ml") || Registry.ITEM.getId(item).getPath().equals("snow")) {
                itemStacks.add(new ItemStack((ItemConvertible)item));
            }
        })).icon(() -> new ItemStack((ItemConvertible)MoreLayers.grass_block_layer)).build();
        this.registerBlocks();
        ResourcesManager.PushTags();
                
        ResourcesManager.RegisterCallback();

        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            
            // If code is running client-side or if the player isn't sneaking, skip the conversion.
            if (world.isClient || !player.isSneaking()) {
                return ActionResult.PASS;
            }
            
            // If player isn't in Survival or Creative gamemodes, skip the conversion.
            GameMode gameMode = ((ServerPlayerEntity)player).interactionManager.getGameMode();
            if (
                gameMode != GameMode.SURVIVAL &&
                gameMode != GameMode.CREATIVE
            ) {
                return ActionResult.PASS;
            }

            // If player has a block in offhand, skip the conversion.
            if (player.getOffHandStack().getItem() instanceof BlockItem)
            {
                return ActionResult.PASS;
            }

            final Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
            
            // If targetted Block is a "layer" block...
            if (block instanceof SnowBlock || block instanceof BlockLayer || block instanceof BlockConcretePowderLayer) {
                if (isCorrectTool(block, player.getStackInHand(hand).getItem())) {
                    final int newHeight = world.getBlockState(hitResult.getBlockPos()).get(BlockLayer.LAYERS) - 1;
                    
                    if (newHeight == 0) {
                        world.breakBlock(hitResult.getBlockPos(), true);
                    }
                    else {    
                        world.setBlockState(
                            hitResult.getBlockPos(),
                            world.getBlockState(hitResult.getBlockPos()).with(BlockLayer.LAYERS, newHeight)
                        );

                        Block.dropStack(world, hitResult.getBlockPos(), new ItemStack(block.asItem()));
                    }

                    player.getStackInHand(hand).damage(1, player, null);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            }
            
            // If not a layer block...
            else {
                // If targetted block doesn't exist in layered form, skip the conversion.
                if (MoreLayers.blockConversions.get(block) == null) {
                    return ActionResult.PASS;
                }
                
                final Block layerBlock = MoreLayers.blockConversions.get(block);


                if (isCorrectTool(layerBlock, player.getStackInHand(hand).getItem())) {
                    world.setBlockState(hitResult.getBlockPos(), layerBlock.getDefaultState().with(BlockLayer.LAYERS, 8));
                    player.getStackInHand(hand).damage(1, player, null);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            }
        }));
    }

    /**
     * ONLY CALL FOR MORELAYERS BLOCKS
     * @param block : A MoreLayers Block
     * @param item : The item used by the player
     * @return <code>true</code>, if <code>item</code> is the correct tool to mine <code>block</code>
     */
    private boolean isCorrectTool(Block block, Item item) {
        MiningTool correctTool = blockTools.get(block);

        if (correctTool == null)
            return false;

        boolean isCorrect = false;

        switch(correctTool) {
            case PICKAXE:
                isCorrect = item instanceof PickaxeItem;
                break;
            case HOE:
                isCorrect = item instanceof HoeItem;
                break;
            case SHOVEL:
                isCorrect = item instanceof ShovelItem;
                break;
        }

        return isCorrect;
    }
    
    // Todo disable layer conversion for very high hardness blocks, like obsidian


    /* 
    // WIP Data Fixer Code,
    // Just gave up at this point
    // and decided to keep grass path layer's name as-is
    // waiting until a library or something gets published to help with this
    // Keeping code here for future reference

    private void fixData() {
        final int dataVersion = 1;
        
        DataFixerBuilder builder = new DataFixerBuilder(dataVersion);
        
        //builder.addSchema(0, ModDataFixes.MOD_SCHEMA);
        Schema latest_vanilla = Schemas.getFixer().getSchema(SharedConstants.getGameVersion().getWorldVersion());
        Schema mod_schema = new Schema(0, latest_vanilla);

        builder.addSchema(0, (_a, _b) -> mod_schema);

        Schema schemaV1 = builder.addSchema(1, IdentifierNormalizingSchema::new);
        
        builder.addFixer(ItemNameFix.create(schemaV1, "Renamed Grass Path Layer item to Dirt Path Layer", replacing("ml:grass_path_layer", "ml:dirt_path_layer")));
        builder.addFixer(JigsawBlockNameFix.create(schemaV1, "Renamed Grass Path Layer block to Dirt Path Layer", replacing("ml:grass_path_layer", "ml:dirt_path_layer")));
        
        DataFixer fixer = builder.build(Util.getMainWorkerExecutor());


        
    }

    private static UnaryOperator<String> replacing(String oldId, String newId) {
        return (inputName)
            -> Objects.equals(IdentifierNormalizingSchema.normalize(inputName), oldId) ? newId : inputName;
    }

    */

    private void registerBlocks() {
        dirt_layer = registerShovelBlock(Blocks.DIRT);
        coarse_dirt_layer = registerShovelBlock(Blocks.COARSE_DIRT);
        podzol_layer = registerShovelBlock(Blocks.PODZOL);
        mycelium_layer = registerShovelBlock(Blocks.MYCELIUM);
        grass_block_layer = registerShovelBlock(Blocks.GRASS_BLOCK);
        dirt_path_layer = registerShovelBlock(Blocks.DIRT_PATH, "grass_path");
        mud_layer = registerShovelBlock(Blocks.MUD);
    
        hay_block_layer = registerHoeBlock(Blocks.HAY_BLOCK);
        oak_leaves_layer = registerHoeBlock(Blocks.OAK_LEAVES);
        birch_leaves_layer = registerHoeBlock(Blocks.BIRCH_LEAVES);
        jungle_leaves_layer = registerHoeBlock(Blocks.JUNGLE_LEAVES);
        spruce_leaves_layer = registerHoeBlock(Blocks.SPRUCE_LEAVES);
        dark_oak_leaves_layer = registerHoeBlock(Blocks.DARK_OAK_LEAVES);
        acacia_leaves_layer = registerHoeBlock(Blocks.ACACIA_LEAVES);
        azalea_leaves_layer = registerHoeBlock(Blocks.AZALEA_LEAVES);
        flowering_azalea_leaves_layer = registerHoeBlock(Blocks.FLOWERING_AZALEA_LEAVES);
        mangrove_leaves_layer = registerHoeBlock(Blocks.MANGROVE_LEAVES);
    
        sandstone_layer = registerPickaxeBlock(Blocks.SANDSTONE);
        red_sandstone_layer = registerPickaxeBlock(Blocks.RED_SANDSTONE);
        cobblestone_layer = registerPickaxeBlock(Blocks.COBBLESTONE);
        mossy_cobblestone_layer = registerPickaxeBlock(Blocks.MOSSY_COBBLESTONE);
        stone_layer = registerPickaxeBlock(Blocks.STONE);
        granite_layer = registerPickaxeBlock(Blocks.GRANITE);
        diorite_layer = registerPickaxeBlock(Blocks.DIORITE);
        andesite_layer = registerPickaxeBlock(Blocks.ANDESITE);
        obsidian_layer = registerPickaxeBlock(Blocks.OBSIDIAN);
        nether_rack_layer = registerPickaxeBlock(Blocks.NETHERRACK);
        end_stone_layer = registerPickaxeBlock(Blocks.END_STONE);
        blackstone_layer = registerPickaxeBlock(Blocks.BLACKSTONE);
        basalt_layer = registerPickaxeBlock(Blocks.BASALT);
        crying_obsidian_layer = registerPickaxeBlock(Blocks.CRYING_OBSIDIAN);
        warped_nylium_layer = registerPickaxeBlock(Blocks.WARPED_NYLIUM);
        crimson_nylium_layer = registerPickaxeBlock(Blocks.CRIMSON_NYLIUM);
        cobbled_deepslate_layer = registerPickaxeBlock(Blocks.COBBLED_DEEPSLATE);
        deepslate_layer = registerPickaxeBlock(Blocks.DEEPSLATE);

        white_concrete_powder_layer = registerConcretePowderBlock(Blocks.WHITE_CONCRETE_POWDER);
        orange_concrete_powder_layer = registerConcretePowderBlock(Blocks.ORANGE_CONCRETE_POWDER);
        magenta_concrete_powder_layer = registerConcretePowderBlock(Blocks.MAGENTA_CONCRETE_POWDER);
        light_blue_concrete_powder_layer = registerConcretePowderBlock(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
        yellow_concrete_powder_layer = registerConcretePowderBlock(Blocks.YELLOW_CONCRETE_POWDER);
        lime_concrete_powder_layer = registerConcretePowderBlock(Blocks.LIME_CONCRETE_POWDER);
        pink_concrete_powder_layer = registerConcretePowderBlock(Blocks.PINK_CONCRETE_POWDER);
        gray_concrete_powder_layer = registerConcretePowderBlock(Blocks.GRAY_CONCRETE_POWDER);
        light_gray_concrete_powder_layer = registerConcretePowderBlock(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
        cyan_concrete_powder_layer = registerConcretePowderBlock(Blocks.CYAN_CONCRETE_POWDER);
        purple_concrete_powder_layer = registerConcretePowderBlock(Blocks.PURPLE_CONCRETE_POWDER);
        blue_concrete_powder_layer = registerConcretePowderBlock(Blocks.BLUE_CONCRETE_POWDER);
        brown_concrete_powder_layer = registerConcretePowderBlock(Blocks.BROWN_CONCRETE_POWDER);
        green_concrete_powder_layer = registerConcretePowderBlock(Blocks.GREEN_CONCRETE_POWDER);
        red_concrete_powder_layer = registerConcretePowderBlock(Blocks.RED_CONCRETE_POWDER);
        black_concrete_powder_layer = registerConcretePowderBlock(Blocks.BLACK_CONCRETE_POWDER);
        sand_layer = registerConcretePowderBlock(Blocks.SAND);
        red_sand_layer = registerConcretePowderBlock(Blocks.RED_SAND);
        gravel_layer = registerConcretePowderBlock(Blocks.GRAVEL);
    }

    private Block registerShovelBlock(final Block block) {
        return registerShovelBlock(block, "");
    }

    private Block registerShovelBlock(final Block block, final String name) {
        return registerBlock(block, MiningTool.SHOVEL, true, name);
    }

    private Block registerPickaxeBlock(final Block block, final boolean breakByHand) {
        return registerBlock(block, MiningTool.PICKAXE, false);
    }

    private Block registerPickaxeBlock(final Block block) {
        return registerBlock(block, MiningTool.PICKAXE, false);
    }
    
    private Block registerHoeBlock(final Block block) {
        return registerBlock(block, MiningTool.HOE, true);
    }

    private Block registerConcretePowderBlock(final Block block) {
        final Block block2 = new BlockConcretePowderLayer(block);
        registerCommon(block, block2, MiningTool.SHOVEL);
        return block2;
    }

    private Block registerBlock(final Block block, final MiningTool tool, final boolean breakByHand) {
        return registerBlock(block, tool, breakByHand, "");
    }
    
    private Block registerBlock(final Block block, final MiningTool tool, final boolean breakByHand, final String name) {
        final Block res = new BlockLayer(block, breakByHand);
        
        if (name.isEmpty())
            registerCommon(block, res, tool);
        else
            registerCommon(block, res, name, tool);
        
        return res;
    }
    
    private void registerCommon(final Block src_block, final Block res_block, MiningTool tool) {
        final String name = src_block.getTranslationKey().replace("block.minecraft.", "");
        registerCommon(src_block, res_block, name, tool);
    }

    private void registerCommon(final Block src_block, final Block res_block, String res_block_name, MiningTool tool) {
        res_block_name +=  "_layer";

        // Register the block
        Registry.register(Registry.BLOCK, new Identifier("ml", res_block_name), res_block);
        
        // Create and register BlockItem
        final BlockItem blockItem = new BlockItem(res_block, new Item.Settings().group(MoreLayers.itemGroup));
        Registry.register(Registry.ITEM, new Identifier("ml", res_block_name), blockItem);
        
        // Add blocks to conversion list
        blockConversions.put(src_block, res_block);

        // Fill up blockTools hashmap
        blockTools.put(res_block, tool);

        // Generate Block Data
        ResourcesManager.GenerateBlockData(blockItem, src_block, tool);
    }
    
    static {
        MoreLayers.blockConversions = new Hashtable<Block, Block>();
        
        MoreLayers.blockTools = new Hashtable<Block, MiningTool>();
        blockTools.put(Blocks.SNOW_BLOCK, MiningTool.SHOVEL);
    }
}
