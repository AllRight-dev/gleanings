package dev.gleanings.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Deepslate that is heavier than it looks: it falls like sand, hits like an anvil,
 * and will not let itself be mined - it slips away instead (see
 * playerWillDestroy). The only reliable way to take one is to let it fall onto a
 * torch, the same trick that works on a dragon egg.
 */
public class HeavyDeepslateBlock extends FallingBlock {

    /** Vanilla anvil values: 2 damage per block fallen, capped at 40. */
    private static final float FALL_DAMAGE_PER_BLOCK = 2.0F;
    private static final int FALL_DAMAGE_CAP = 40;

    public HeavyDeepslateBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(FALL_DAMAGE_PER_BLOCK, FALL_DAMAGE_CAP);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // Nothing: it is stone. Kept explicit so nobody adds particles by accident.
    }

    /**
     * Breaking it moves it rather than yielding it, exactly like a dragon egg.
     * Returning false tells vanilla the block survived, so no drop is produced.
     */
    @Override
    protected boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos,
                                         net.minecraft.world.entity.player.Player player,
                                         boolean willHarvest, net.minecraft.world.level.material.FluidState fluid) {
        if (level.isClientSide) {
            return false;
        }

        BlockState moving = level.getBlockState(pos);
        for (int attempt = 0; attempt < 1000; attempt++) {
            BlockPos target = pos.offset(
                    RandomSource.create().nextInt(16) - RandomSource.create().nextInt(16),
                    RandomSource.create().nextInt(8) - RandomSource.create().nextInt(8),
                    RandomSource.create().nextInt(16) - RandomSource.create().nextInt(16));

            if (!level.isEmptyBlock(target)) {
                continue;
            }
            if (level instanceof ServerLevel server && !server.isPositionEntityTicking(target)) {
                continue;
            }

            level.setBlockAndUpdate(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(target, moving);
            level.levelEvent(2001, pos, net.minecraft.world.level.block.Block.getId(state));
            return false;
        }

        // Nowhere to slip to: let it break normally rather than eat the click.
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }
}
