package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.item.WingedBootsItem;
import dev.gleanings.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Charge-jump on the winged boots.
 *
 * Holding the jump key while standing charges for up to half a second; letting go
 * launches up to three blocks. The key state is already synced to the server
 * (Player#jumping), so no packets of our own are needed.
 *
 * A jump costs one durability, which is what gives the phantom-membrane repair
 * something to repair.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class WingedBootsCharge {

    private static final int FULL_CHARGE_TICKS = 10;      // 0.5s
    private static final double MAX_LAUNCH = 0.68;        // ~3 blocks
    private static final Map<UUID, Integer> CHARGE = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        boolean wearing = boots.is(ModItems.WINGED_BOOTS.get());
        UUID id = player.getUUID();

        if (!wearing) {
            CHARGE.remove(id);
            return;
        }

        boolean charging = player.jumping && player.onGround();

        if (charging) {
            CHARGE.merge(id, 1, Integer::sum);
            return;
        }

        int held = CHARGE.getOrDefault(id, 0);
        CHARGE.remove(id);
        if (held <= 0 || !player.onGround()) {
            return;
        }

        float power = Math.min(1.0F, held / (float) FULL_CHARGE_TICKS);
        if (power < 0.15F) {
            return;
        }

        double launch = MAX_LAUNCH * power;
        var velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, launch, velocity.z);
        player.hurtMarked = true;
        player.hasImpulse = true;

        player.level().playSound(null, player.blockPosition(),
                SoundEvents.WOOL_STEP, SoundSource.PLAYERS, 1.0F, 0.6F + 0.5F * power);

        if (!player.getAbilities().instabuild) {
            boots.hurtAndBreak(1, player, EquipmentSlot.FEET);
        }
    }

    private WingedBootsCharge() {
    }
}
