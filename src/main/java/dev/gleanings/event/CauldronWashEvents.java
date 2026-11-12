package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Washing rotten flesh in a water cauldron. The cauldron already exists in vanilla,
 * so no new block is introduced: only a new interaction on a block the game already has.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class CauldronWashEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        ItemStack held = event.getItemStack();

        if (!state.is(Blocks.WATER_CAULDRON) || !held.is(Items.ROTTEN_FLESH)) {
            return;
        }

        // Take over the interaction on both sides so vanilla never processes it.
        event.setCanceled(true);
        if (level.isClientSide) {
            return;
        }

        Player player = event.getEntity();
        int waterLevel = state.getValue(LayeredCauldronBlock.LEVEL);

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        ItemStack washed = new ItemStack(ModItems.WASHED_ROTTEN_FLESH.get());
        if (!player.getInventory().add(washed)) {
            player.drop(washed, false);
        }

        if (waterLevel > 1) {
            level.setBlockAndUpdate(pos, state.setValue(LayeredCauldronBlock.LEVEL, waterLevel - 1));
        } else {
            level.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
        }

        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.5F, 1.6F);
    }

    private CauldronWashEvents() {
    }
}
