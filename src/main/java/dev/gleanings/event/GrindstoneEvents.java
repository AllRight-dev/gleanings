package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Grinding the core into dregs. Vanilla's grindstone is already a spinning stone
 * wheel, so the block exists - it only needed a use. Right-click it while holding
 * a core and the crystal is ground down. No new block, no new GUI.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class GrindstoneEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        ItemStack held = event.getItemStack();

        if (!level.getBlockState(pos).is(Blocks.GRINDSTONE) || !held.is(ModItems.RESONANCE_CORE.get())) {
            return;
        }

        event.setCanceled(true);
        if (level.isClientSide) {
            return;
        }

        Player player = event.getEntity();
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }

        ItemStack dregs = new ItemStack(ModItems.RESONANCE_DREGS.get());
        if (!player.getInventory().add(dregs)) {
            player.drop(dregs, false);
        }

        level.playSound(null, pos, SoundEvents.GRINDSTONE_USE, SoundSource.BLOCKS, 0.8F, 1.5F);
    }

    private GrindstoneEvents() {
    }
}
