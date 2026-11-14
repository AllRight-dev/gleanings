package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.item.EarplugsItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Worn earplugs take 40% off sonic damage - warden booms, and our own bow.
 * Applied here rather than as an attribute so other mods' sonic sources count too.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class EarplugsEvents {

    private static final ResourceKey<DamageType> SONIC_BOOM =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("minecraft", "sonic_boom"));

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || !EarplugsItem.isWorn(player)) {
            return;
        }
        if (!event.getSource().is(SONIC_BOOM)) {
            return;
        }
        event.setAmount(event.getAmount() * EarplugsItem.SONIC_DAMAGE_MULTIPLIER);
    }

    private EarplugsEvents() {
    }
}
