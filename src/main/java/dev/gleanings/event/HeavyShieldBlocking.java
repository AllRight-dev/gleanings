package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.item.HeavyShieldItem;
import dev.gleanings.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * The heavy shield blocks without being raised.
 *
 * A hit is swallowed when it arrives from roughly the front and the shield is not
 * on cooldown. Five swallowed hits spend the charge; after that the shield is
 * disabled for 120 ticks, slightly longer than vanilla's 100.
 *
 * The charge lives in the stack's DAMAGE component, so the durability bar doubles
 * as a "hits left" readout instead of needing a component of our own.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class HeavyShieldBlocking {

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_SHIELD)) {
            return;
        }

        ItemStack shield = findShield(player);
        if (shield.isEmpty()) {
            return;
        }
        if (player.getCooldowns().isOnCooldown(shield.getItem())) {
            return;
        }
        if (!fromTheFront(player, event)) {
            return;
        }

        event.setAmount(0.0F);
        player.level().playSound(null, player.blockPosition(),
                SoundEvents.SHIELD_BLOCK, SoundSource.PLAYERS, 0.9F, 0.8F);

        int spent = shield.getOrDefault(DataComponents.DAMAGE, 0) + 1;
        if (spent >= HeavyShieldItem.BLOCKS_PER_CHARGE) {
            shield.set(DataComponents.DAMAGE, 0);
            player.getCooldowns().addCooldown(shield.getItem(), HeavyShieldItem.COOLDOWN_TICKS);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.9F, 0.8F);
        } else {
            shield.set(DataComponents.DAMAGE, spent);
        }
    }

    private static ItemStack findShield(Player player) {
        if (player.getOffhandItem().is(ModItems.HEAVY_SHIELD.get())) {
            return player.getOffhandItem();
        }
        if (player.getMainHandItem().is(ModItems.HEAVY_SHIELD.get())) {
            return player.getMainHandItem();
        }
        return ItemStack.EMPTY;
    }

    /** Only hits arriving within roughly 100 degrees of where you are looking count. */
    private static boolean fromTheFront(Player player, LivingIncomingDamageEvent event) {
        var attacker = event.getSource().getDirectEntity();
        if (attacker == null) {
            return false;
        }
        Vec3 toAttacker = attacker.position().subtract(player.position());
        if (toAttacker.lengthSqr() < 1.0E-4) {
            return true;
        }
        return toAttacker.normalize().dot(player.getLookAngle()) > 0.0;
    }

    private HeavyShieldBlocking() {
    }
}
