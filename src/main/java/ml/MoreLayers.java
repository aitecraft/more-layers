package ml;

import java.util.ArrayList;
import java.util.Hashtable;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;
import net.minecraft.tag.Tag;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.PickaxeItem;
import net.minecraft.block.SnowBlock;
import net.minecraft.world.GameMode;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.registry.Registry;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Blocks;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;
import net.fabricmc.api.ModInitializer;

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
    
    // Stone and Variants
    public static Block andesite_layer;
    public static Block cobblestone_layer;
    public static Block diorite_layer;
    public static Block granite_layer;
    public static Block mossy_cobblestone_layer;
    public static Block stone_layer;
    public static Block sandstone_layer;
    public static Block red_sandstone_layer;
    
    // Dirt and Grass Variants
    public static Block coarse_dirt_layer;
    public static Block dirt_layer;
    public static Block grass_block_layer;
    public static Block grass_path_layer;
    public static Block mycelium_layer;
    public static Block podzol_layer;
    
    // Nether Blocks
    public static Block nether_rack_layer;
    public static Block blackstone_layer;
    public static Block basalt_layer;
    public static Block crimson_nylium_layer;
    public static Block warped_nylium_layer;
    
    // End Blocks
    public static Block end_stone_layer;
    
    // Misc. Blocks
    public static Block obsidian_layer;
    public static Block crying_obsidian_layer;
    public static Block hay_block_layer;


    public static Map<Block, Block> blockConversions;
    public static List<Block> pickaxeBlocks;
    
    public void onInitialize() {
        MoreLayers.itemGroup = FabricItemGroupBuilder.create(new Identifier("ml", "layers")).appendItems(itemStacks -> Registry.ITEM.forEach(item -> {
            if (Registry.ITEM.getId(item).getNamespace().equals("ml") || Registry.ITEM.getId(item).getPath().equals("snow")) {
                itemStacks.add(new ItemStack((ItemConvertible)item));
            }
        })).icon(() -> new ItemStack((ItemConvertible)MoreLayers.grass_block_layer)).build();
        this.registerBlocks();
        
        UseBlockCallback.EVENT.register(((player, world, hand, hitResult) -> {
            if (world.isClient || !player.isSneaking()) {
                return ActionResult.PASS;
            }
            if (((ServerPlayerEntity)player).interactionManager.getGameMode() != GameMode.SURVIVAL && ((ServerPlayerEntity)player).interactionManager.getGameMode() != GameMode.CREATIVE) {
                return ActionResult.PASS;
            }
            final Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();
            if (block instanceof SnowBlock || block instanceof BlockLayer || block instanceof BlockConcretePowderLayer) {
                if ((MoreLayers.pickaxeBlocks.contains(block) && player.getStackInHand(hand).getItem() instanceof PickaxeItem) || (!MoreLayers.pickaxeBlocks.contains(block) && player.getStackInHand(hand).getItem() instanceof ShovelItem)) {
                    final int newHeight = (int)world.getBlockState(hitResult.getBlockPos()).get((IntProperty)BlockLayer.LAYERS) - 1;
                    if (newHeight == 0) {
                        world.breakBlock(hitResult.getBlockPos(), true);
                    }
                    else {
                        world.setBlockState(
                            hitResult.getBlockPos(),
                            (BlockState)world.getBlockState(hitResult.getBlockPos()).with(BlockLayer.LAYERS, newHeight)
                        );
                        Block.dropStack(world, hitResult.getBlockPos(), new ItemStack((ItemConvertible)block.asItem()));
                    }
                    player.getStackInHand(hand).damage(1, player, null);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            }
            else {
                if (MoreLayers.blockConversions.get((Object)block) == null) {
                    return ActionResult.PASS;
                }
                final Block layerBlock = (Block)MoreLayers.blockConversions.get((Object)block);
                if ((MoreLayers.pickaxeBlocks.contains((Object)layerBlock) && player.getStackInHand(hand).getItem() instanceof PickaxeItem) || (!MoreLayers.pickaxeBlocks.contains((Object)layerBlock) && player.getStackInHand(hand).getItem() instanceof ShovelItem)) {
                    world.setBlockState(hitResult.getBlockPos(), (BlockState)layerBlock.getDefaultState().with(BlockLayer.LAYERS, 8));
                    player.getStackInHand(hand).damage(1, player, null);
                    return ActionResult.SUCCESS;
                }
                return ActionResult.PASS;
            }
        }));
    }
    
    private void registerBlocks() {
        MoreLayers.dirt_layer = this.registerShovelBlock(Blocks.DIRT);
        MoreLayers.coarse_dirt_layer = this.registerShovelBlock(Blocks.COARSE_DIRT);
        MoreLayers.podzol_layer = this.registerShovelBlock(Blocks.PODZOL);
        MoreLayers.mycelium_layer = this.registerShovelBlock(Blocks.MYCELIUM);
        MoreLayers.grass_block_layer = this.registerShovelBlock(Blocks.GRASS_BLOCK);
        MoreLayers.grass_path_layer = this.registerShovelBlock(Blocks.GRASS_PATH);
        MoreLayers.sandstone_layer = this.registerPickaxeBlock(Blocks.SANDSTONE);
        MoreLayers.red_sandstone_layer = this.registerPickaxeBlock(Blocks.RED_SANDSTONE);
        MoreLayers.hay_block_layer = this.registerHoeBlock(Blocks.HAY_BLOCK);
        MoreLayers.cobblestone_layer = this.registerPickaxeBlock(Blocks.COBBLESTONE);
        MoreLayers.mossy_cobblestone_layer = this.registerPickaxeBlock(Blocks.MOSSY_COBBLESTONE);
        MoreLayers.stone_layer = this.registerPickaxeBlock(Blocks.STONE);
        MoreLayers.granite_layer = this.registerPickaxeBlock(Blocks.GRANITE);
        MoreLayers.diorite_layer = this.registerPickaxeBlock(Blocks.DIORITE);
        MoreLayers.andesite_layer = this.registerPickaxeBlock(Blocks.ANDESITE);
        MoreLayers.obsidian_layer = this.registerPickaxeBlock(Blocks.OBSIDIAN);
        MoreLayers.nether_rack_layer = this.registerPickaxeBlock(Blocks.NETHERRACK);
        MoreLayers.end_stone_layer = this.registerPickaxeBlock(Blocks.END_STONE);
        MoreLayers.blackstone_layer = this.registerPickaxeBlock(Blocks.BLACKSTONE);
        MoreLayers.basalt_layer = this.registerPickaxeBlock(Blocks.BASALT);
        MoreLayers.crying_obsidian_layer = this.registerPickaxeBlock(Blocks.CRYING_OBSIDIAN);
        MoreLayers.warped_nylium_layer = this.registerPickaxeBlock(Blocks.WARPED_NYLIUM);
        MoreLayers.crimson_nylium_layer = this.registerPickaxeBlock(Blocks.CRIMSON_NYLIUM);
        MoreLayers.oak_leaves_layer = this.registerHoeBlock(Blocks.OAK_LEAVES);
        MoreLayers.birch_leaves_layer = this.registerHoeBlock(Blocks.BIRCH_LEAVES);
        MoreLayers.jungle_leaves_layer = this.registerHoeBlock(Blocks.JUNGLE_LEAVES);
        MoreLayers.spruce_leaves_layer = this.registerHoeBlock(Blocks.SPRUCE_LEAVES);
        MoreLayers.dark_oak_leaves_layer = this.registerHoeBlock(Blocks.DARK_OAK_LEAVES);
        MoreLayers.acacia_leaves_layer = this.registerHoeBlock(Blocks.ACACIA_LEAVES);
        
        MoreLayers.white_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.WHITE_CONCRETE_POWDER);
        MoreLayers.orange_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.ORANGE_CONCRETE_POWDER);
        MoreLayers.magenta_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.MAGENTA_CONCRETE_POWDER);
        MoreLayers.light_blue_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.LIGHT_BLUE_CONCRETE_POWDER);
        MoreLayers.yellow_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.YELLOW_CONCRETE_POWDER);
        MoreLayers.lime_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.LIME_CONCRETE_POWDER);
        MoreLayers.pink_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.PINK_CONCRETE_POWDER);
        MoreLayers.gray_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.GRAY_CONCRETE_POWDER);
        MoreLayers.light_gray_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.LIGHT_GRAY_CONCRETE_POWDER);
        MoreLayers.cyan_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.CYAN_CONCRETE_POWDER);
        MoreLayers.purple_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.PURPLE_CONCRETE_POWDER);
        MoreLayers.blue_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.BLUE_CONCRETE_POWDER);
        MoreLayers.brown_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.BROWN_CONCRETE_POWDER);
        MoreLayers.green_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.GREEN_CONCRETE_POWDER);
        MoreLayers.red_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.RED_CONCRETE_POWDER);
        MoreLayers.black_concrete_powder_layer = this.registerConcretePowderBlock(Blocks.BLACK_CONCRETE_POWDER);
        MoreLayers.sand_layer = this.registerConcretePowderBlock(Blocks.SAND);
        MoreLayers.red_sand_layer = this.registerConcretePowderBlock(Blocks.RED_SAND);
        MoreLayers.gravel_layer = this.registerConcretePowderBlock(Blocks.GRAVEL);
    }
    
    private Block registerBlock(final Block block, final Tag<Item> tool, final boolean breakByHand) {
        final Block block2 = new BlockLayer(block, tool, breakByHand);
        registerCommon(block, block2);
        return block2;
    }

    private Block registerShovelBlock(final Block block) {
        return registerBlock(block, FabricToolTags.SHOVELS, true);
    }

    private Block registerPickaxeBlock(final Block block, final boolean breakByHand) {
        Block block2 = registerBlock(block, FabricToolTags.PICKAXES, breakByHand);
        pickaxeBlocks.add(block2);
        return block2;
    }

    private Block registerPickaxeBlock(final Block block) {
        return registerPickaxeBlock(block, false);
    }
    
    private Block registerHoeBlock(final Block block) {
        // Just point to shovel for now...
        return registerShovelBlock(block);
    }


    private Block registerConcretePowderBlock(final Block block) {
        final Block block2 = new BlockConcretePowderLayer(block);
        registerCommon(block, block2);
        return block2;
    }

    private void registerCommon(final Block block, final Block block2) {
        Registry.register(Registry.BLOCK, new Identifier("ml", block.getTranslationKey().replace("block.minecraft.", "") + "_layer"), block2);
        final BlockItem blockItem = new BlockItem(block2, new Item.Settings().group(MoreLayers.itemGroup));
        Registry.register(Registry.ITEM, new Identifier("ml", block.getTranslationKey().replace("block.minecraft.", "") + "_layer"), blockItem);
        blockConversions.put(block, block2);
    }
    
    static {
        MoreLayers.blockConversions = new Hashtable<Block, Block>();
        MoreLayers.pickaxeBlocks = new ArrayList<Block>();
    }
}
