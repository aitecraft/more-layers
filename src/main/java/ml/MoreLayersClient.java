package ml;

import net.minecraft.client.color.block.BlockColorProvider;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.item.ItemConvertible;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.client.render.RenderLayer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.block.Blocks;
import net.fabricmc.api.ClientModInitializer;

public class MoreLayersClient implements ClientModInitializer {
    public void onInitializeClient() {
        this.registerBlockColour(MoreLayers.grass_block_layer, Blocks.GRASS_BLOCK);
        this.registerBlockColour(MoreLayers.oak_leaves_layer, Blocks.OAK_LEAVES);
        this.registerBlockColour(MoreLayers.birch_leaves_layer, Blocks.BIRCH_LEAVES);
        this.registerBlockColour(MoreLayers.spruce_leaves_layer, Blocks.SPRUCE_LEAVES);
        this.registerBlockColour(MoreLayers.dark_oak_leaves_layer, Blocks.DARK_OAK_LEAVES);
        this.registerBlockColour(MoreLayers.acacia_leaves_layer, Blocks.ACACIA_LEAVES);
        this.registerBlockColour(MoreLayers.jungle_leaves_layer, Blocks.JUNGLE_LEAVES);
        this.registerBlockColour(MoreLayers.azalea_leaves_layer, Blocks.AZALEA);
        this.registerBlockColour(MoreLayers.flowering_azalea_leaves_layer, Blocks.FLOWERING_AZALEA);

        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.oak_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.birch_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.spruce_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.jungle_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.dark_oak_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.acacia_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.azalea_leaves_layer, RenderLayer.getCutout());
        BlockRenderLayerMap.INSTANCE.putBlock(MoreLayers.flowering_azalea_leaves_layer, RenderLayer.getCutout());

    }
    
    public void registerBlockColour(final Block block, final Block biomeColouringBlock) {
        ColorProviderRegistry.BLOCK.register(
            ((block3, pos, world, layer) -> {
                final BlockColorProvider provider = (BlockColorProvider)ColorProviderRegistry.BLOCK.get(biomeColouringBlock);
                return (provider == null) ? -1 : provider.getColor(block3, pos, world, layer);
            }), new Block[] { block }
        );
        ColorProviderRegistry.ITEM.register(((item, layer) -> {
            if (biomeColouringBlock == Blocks.OAK_LEAVES) {
                return FoliageColors.getDefaultColor();
            }
            return GrassColors.getColor(0.5, 1.0);
        }), new ItemConvertible[] { (ItemConvertible)block.asItem() });
    }
}
