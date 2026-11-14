package dev.gleanings.event;

import dev.gleanings.Gleanings;
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
 * Vanilla "hold jump" is not a charge - it jumps on the first tick. So the boots
 * also drive JUMP_STRENGTH to zero (see HeavyStoneAttributes), which keeps the
 * wearer planted while the key is held. Only then can a charge accumulate; on
 * release we supply the actual velocity ourselves.
 *
 * A tap therefore produces a small hop instead of nothing, which is what a jump
 * key is expected to do.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class WingedBootsCharge {

    private static final int FULL_CHARGE_TICKS = 10;   // 0.5s
    private static final double MAX_LAUNCH = 0.78;     // ~3 blocks with the boots' feel
    private static final double MIN_LAUNCH = 0.42;     // a plain vanilla hop
    private static final Map<UUID, Integer> CHARGE = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        UUID id = player.getUUID();

        if (!boots.is(ModItems.WINGED_BOOTS.get())) {
            CHARGE.remove(id);
            return;
        }

        if (player.jumping && player.onGround()) {
            CHARGE.merge(id, 1, Integer::sum);
            return;
        }

        int held = CHARGE.getOrDefault(id, 0);
        CHARGE.remove(id);

        if (held <= 0 || !player.onGround()) {
            return;
        }

        float power = Math.min(1.0F, held / (float) FULL_CHARGE_TICKS);
        double launch = MIN_LAUNCH + (MAX_LAUNCH - MIN_LAUNCH) * power;

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
