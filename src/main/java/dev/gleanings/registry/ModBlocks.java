package dev.gleanings.registry;

import dev.gleanings.Gleanings;
import dev.gleanings.block.HeavyDeepslateBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Gleanings.MODID);

    /** A darker, much heavier deepslate. Falls, hurts, and takes forever to mine. */
    public static final DeferredBlock<HeavyDeepslateBlock> HEAVY_DEEPSLATE = BLOCKS.register(
            "heavy_deepslate",
            () -> new HeavyDeepslateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(150.0F, 3600.0F)   // obsidian is 50 / 1200
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> HEAVY_DEEPSLATE_ITEM =
            ModItems.ITEMS.registerSimpleBlockItem(HEAVY_DEEPSLATE);

    /**
     * The heavy core is a block, not just a trinket: it has to be placeable on top
     * of a beacon to do anything.
     */
    public static final DeferredBlock<Block> HEAVY_CORE = BLOCKS.register("heavy_core",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(8.0F, 1200.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> HEAVY_CORE_ITEM =
            ModItems.ITEMS.registerSimpleBlockItem(HEAVY_CORE);

    private ModBlocks() {
    }
}
