package dev.gleanings.registry;

import dev.gleanings.Gleanings;
import dev.gleanings.block.HeavyDeepslateBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Gleanings.MODID);

    /** A darker, much heavier deepslate. Falls, hurts, refuses to be mined. */
    public static final DeferredBlock<HeavyDeepslateBlock> HEAVY_DEEPSLATE = BLOCKS.register(
            "heavy_deepslate",
            () -> new HeavyDeepslateBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE)
                    .strength(50.0F, 1200.0F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredItem<BlockItem> HEAVY_DEEPSLATE_ITEM =
            ModItems.ITEMS.registerSimpleBlockItem(HEAVY_DEEPSLATE);

    private ModBlocks() {
    }
}
