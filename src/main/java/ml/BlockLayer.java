package ml;

import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.state.property.Properties;
import net.minecraft.state.StateManager;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.math.Direction;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.WorldView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.state.property.IntProperty;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;

public class BlockLayer extends Block {
    public static final IntProperty LAYERS;
    protected static final VoxelShape[] LAYERS_TO_SHAPE;
    
    protected BlockLayer(final Block block, final boolean breakByHand) {
        super(breakByHand ? FabricBlockSettings.copyOf(block) : FabricBlockSettings.copyOf(block).requiresTool());
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
    
    public BlockState getPlacementState(final ItemPlacementContext ctx) {
        final BlockState current = ctx.getWorld().getBlockState(ctx.getBlockPos());
        final BlockState defaultState = super.getPlacementState(ctx);
        final PlayerEntity player = ctx.getPlayer();
        final World world = ctx.getWorld();
        final boolean server = !world.isClient();
        
        // Adding onto existing BlockLayer
        if (current.getBlock() == this) {
            final int oldHeight = current.get(BlockLayer.LAYERS);
            int newHeight = oldHeight + 1;
            if (player.isSneaking()) newHeight += 3;
            newHeight = Math.min(8, newHeight);

            final boolean survival = !player.getAbilities().creativeMode;

            // This code runs on the client as well, and helps to improve the client-side prediction
            if (survival) newHeight = Math.min(ctx.getStack().getCount() + oldHeight, newHeight);

            BlockState possible = defaultState.with(BlockLayer.LAYERS, newHeight);
            
            // Decrement the extra blocks only if:
            // 1. Code is running on server
            // 2. Player is in survival mode
            // 3. Placement is actually possible
            if (server && survival &&
                world.canPlace(possible, ctx.getBlockPos(), ShapeContext.of(player)) &&
                possible.canPlaceAt(world, ctx.getBlockPos())
            )
                ctx.getStack().decrement(newHeight - oldHeight - 1); // 1 will be deducted by Minecraft code

            return possible;
        }

        // Existing is replaceable or air
        // Place 4 at once when sneaking
        if (player.isSneaking() && (ctx.canReplaceExisting() || world.getBlockState(new BlockPos(ctx.getHitPos())).isAir())) {
            int newHeight = 4;
            
            final boolean survival = !player.getAbilities().creativeMode;

            if (survival) newHeight = Math.min(ctx.getStack().getCount(), newHeight);

            BlockState possible = defaultState.with(BlockLayer.LAYERS, newHeight);

            if (server && survival &&
                world.canPlace(possible, ctx.getBlockPos(), ShapeContext.of(player)) &&
                possible.canPlaceAt(world, ctx.getBlockPos())
            )
                ctx.getStack().decrement(newHeight - 1);

            return possible;
        }

        return defaultState;
    }
    
    protected void appendProperties(final StateManager.Builder<Block, BlockState> stateFactory$Builder_1) {
        stateFactory$Builder_1.add(new Property[] { BlockLayer.LAYERS });
    }
    
    static {
        LAYERS = Properties.LAYERS;
        LAYERS_TO_SHAPE = new VoxelShape[] { VoxelShapes.empty(), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 2.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 4.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 10.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 12.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 14.0, 16.0), Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 16.0, 16.0) };
    }
}
