package dev.gleanings.block;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Deepslate that is heavier than it looks: it falls like sand and hits like an
 * anvil. Getting one out is simply a matter of time - it is set to three times
 * obsidian's hardness - and it crumbles into a heavy core when it finally breaks
 * (see the loot table).
 */
public class HeavyDeepslateBlock extends FallingBlock {

    /** Vanilla anvil values: 2 damage per block fallen, capped at 40. */
    private static final float FALL_DAMAGE_PER_BLOCK = 2.0F;
    private static final int FALL_DAMAGE_CAP = 40;

    public HeavyDeepslateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected com.mojang.serialization.MapCodec<? extends FallingBlock> codec() {
        return simpleCodec(HeavyDeepslateBlock::new);
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(FALL_DAMAGE_PER_BLOCK, FALL_DAMAGE_CAP);
    }

    @Override
    public void animateTick(BlockState state, net.minecraft.world.level.Level level,
                            net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        // Nothing: it is stone. Kept explicit so nobody adds particles by accident.
    }
}
