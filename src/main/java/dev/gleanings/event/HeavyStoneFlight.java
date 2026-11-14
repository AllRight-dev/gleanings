package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModBlocks;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Hold the heavy stone and the elytra quits on you.
 *
 * Checked before the tick so a glide cannot be started while carrying it, and
 * after the tick so one already in progress is cut immediately - the point is
 * that it fails suddenly, mid-air, with the wearer still holding it.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class HeavyStoneFlight {

    private static boolean carrying(Player player) {
        return player.getMainHandItem().is(ModBlocks.HEAVY_DEEPSLATE_ITEM.get());
    }

    @SubscribeEvent
    public static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        cut(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        cut(event.getEntity());
    }

    private static void cut(Player player) {
        if (!(player instanceof ServerPlayer) || !player.isFallFlying() || !carrying(player)) {
            return;
        }

        player.stopFallFlying();
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.8F, 0.6F);

        // No damage is applied on purpose: the fall that follows is the punishment.
        player.hurtMarked = true;
    }

    private HeavyStoneFlight() {
    }
}
