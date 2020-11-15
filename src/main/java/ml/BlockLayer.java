package ml;

import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.state.property.Properties;
import net.minecraft.state.StateManager;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldAccess;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.Direction;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.world.WorldView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.state.property.IntProperty;
import net.minecraft.block.Block;

public class BlockLayer extends Block {
    public static final IntProperty LAYERS;
    protected static final VoxelShape[] LAYERS_TO_SHAPE;
    
    protected BlockLayer(final Block block) {
        super(Block.Settings.copy(block));
        this.setDefaultState(this.getDefaultState().with(BlockLayer.LAYERS, 1));
    }
    
    public boolean canPathfindThrough(final BlockState blockState_1, final BlockView blockView_1, final BlockPos blockPos_1, final NavigationType blockPlacementEnvironment_1) {
        switch (blockPlacementEnvironment_1) {
            case LAND: {
                return blockState_1.get(BlockLayer.LAYERS) < 5;
            }
            case WATER: {
                return false;
            }
            case AIR: {
                return false;
            }
            default: {
                return false;
            }
        }
    }
    
    public VoxelShape getOutlineShape(final BlockState blockState_1, final BlockView blockView_1, final BlockPos blockPos_1, final ShapeContext entityContext_1) {
        return BlockLayer.LAYERS_TO_SHAPE[blockState_1.get(BlockLayer.LAYERS)];
    }
    
    public VoxelShape getCollisionShape(final BlockState blockState_1, final BlockView blockView_1, final BlockPos blockPos_1, final ShapeContext entityContext_1) {
        return BlockLayer.LAYERS_TO_SHAPE[blockState_1.get(BlockLayer.LAYERS)];
    }
    
    public boolean hasSidedTransparency(final BlockState blockState_1) {
        return true;
    }
    
    public boolean canPlaceAt(final BlockState blockState_1, final WorldView worldView_1, final BlockPos blockPos_1) {
        final BlockState blockState_2 = worldView_1.getBlockState(blockPos_1.down());
        final Block block_1 = blockState_2.getBlock();
        return block_1 != Blocks.ICE && block_1 != Blocks.PACKED_ICE && block_1 != Blocks.BARRIER && (block_1 == Blocks.HONEY_BLOCK || block_1 == Blocks.SOUL_SAND || Block.isFaceFullSquare(blockState_2.getCollisionShape(worldView_1, blockPos_1.down()), Direction.UP) || ((block_1 instanceof BlockLayer || block_1 instanceof BlockConcretePowderLayer || block_1 == Blocks.SNOW_BLOCK) && blockState_2.get(BlockLayer.LAYERS) == 8));
    }
    
    public BlockState getStateForNeighborUpdate(final BlockState blockState_1, final Direction direction_1, final BlockState blockState_2, final WorldAccess iWorld_1, final BlockPos blockPos_1, final BlockPos blockPos_2) {
        return blockState_1.canPlaceAt(iWorld_1, blockPos_1) ? super.getStateForNeighborUpdate(blockState_1, direction_1, blockState_2, iWorld_1, blockPos_1, blockPos_2) : Blocks.AIR.getDefaultState();
    }
    
    public boolean canReplace(final BlockState blockState_1, final ItemPlacementContext itemPlacementContext_1) {
        final int int_1 = blockState_1.get(BlockLayer.LAYERS);
        return itemPlacementContext_1.getStack().getItem() == this.asItem() && int_1 < 8 && (!itemPlacementContext_1.canReplaceExisting() || itemPlacementContext_1.getSide() == Direction.UP);
    }
    
    public BlockState getPlacementState(final ItemPlacementContext itemPlacementContext_1) {
        final BlockState blockState_1 = itemPlacementContext_1.getWorld().getBlockState(itemPlacementContext_1.getBlockPos());
        if (blockState_1.getBlock() == this) {
            final int oldHeight = blockState_1.get(BlockLayer.LAYERS);
            int newHeight = oldHeight + 1;
            if (itemPlacementContext_1.getPlayer().isSneaking()) {
                newHeight += 3;
            }
            newHeight = Math.min(8, newHeight);
            if (!itemPlacementContext_1.getWorld().isClient()  && ((ServerPlayerEntity)itemPlacementContext_1.getPlayer()).interactionManager.getGameMode() == GameMode.SURVIVAL) {
                newHeight = Math.min(itemPlacementContext_1.getStack().getCount() + oldHeight, newHeight);
                itemPlacementContext_1.getStack().decrement(newHeight - oldHeight - 1);
            }
            return blockState_1.with(BlockLayer.LAYERS, newHeight);
        }
        if (itemPlacementContext_1.getPlayer().isSneaking() && (itemPlacementContext_1.canReplaceExisting() || itemPlacementContext_1.getWorld().getBlockState(new BlockPos(itemPlacementContext_1.getHitPos())).isAir())) {
            int newHeight2 = 4;
            if (!itemPlacementContext_1.getWorld().isClient() && ((ServerPlayerEntity)itemPlacementContext_1.getPlayer()).interactionManager.getGameMode() == GameMode.SURVIVAL) {
                newHeight2 = Math.min(itemPlacementContext_1.getStack().getCount(), newHeight2);
                itemPlacementContext_1.getStack().decrement(newHeight2 - 1);
            }
            return super.getPlacementState(itemPlacementContext_1).with(BlockLayer.LAYERS, newHeight2);
        }
        return super.getPlacementState(itemPlacementContext_1);
    }
    
    protected void appendProperties(final StateManager.Builder<Block, BlockState> stateFactory$Builder_1) {
        stateFactory$Builder_1.add(new Property[] { BlockLayer.LAYERS });
    }
    
    static {
        LAYERS = Properties.LAYERS;
        LAYERS_TO_SHAPE = new VoxelShape[] { VoxelShapes.empty(), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0) };
    }
}
