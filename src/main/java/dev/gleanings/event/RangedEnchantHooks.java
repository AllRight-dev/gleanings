package dev.gleanings.event;

import dev.gleanings.Gleanings;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/**
 * Two ranged-weapon enchantments that work on any bow or crossbow, applied the
 * moment the arrow enters the world. No mixin: an arrow that already exists can
 * still have its pierce level and its heading rewritten before its first tick.
 *
 *   gleanings:piercing  - the arrow keeps going through targets (vanilla's own
 *                         pierce mechanism, which bows simply never use)
 *   gleanings:guidance  - the arrow leaves already aimed where a moving target
 *                         will be in half a second, instead of turning in flight
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class RangedEnchantHooks {

    private static final ResourceKey<Enchantment> PIERCING = key("piercing");
    private static final ResourceKey<Enchantment> GUIDANCE = key("guidance");

    private static final double GUIDANCE_RANGE = 24.0;
    private static final double LEAD_SECONDS = 0.5;
    private static final int TICKS_PER_SECOND = 20;

    @SubscribeEvent
    public static void onArrowSpawn(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof AbstractArrow arrow)) {
            return;
        }
        if (!(arrow.getOwner() instanceof Player player)) {
            return;
        }

        ItemStack weapon = player.getMainHandItem();

        int pierce = levelOf(weapon, PIERCING);
        if (pierce > 0) {
            // 7 is the largest value vanilla's hit tracking tolerates.
            arrow.setPierceLevel((byte) Math.min(7, pierce));
        }

        int guidance = levelOf(weapon, GUIDANCE);
        if (guidance > 0) {
            lead(arrow, player, guidance);
        }
    }

    /** Aim at where the target will be, once, at launch. */
    private static void lead(AbstractArrow arrow, Player shooter, int level) {
        Vec3 velocity = arrow.getDeltaMovement();
        if (velocity.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 heading = velocity.normalize();

        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;

        for (LivingEntity candidate : arrow.level().getEntitiesOfClass(LivingEntity.class,
                arrow.getBoundingBox().inflate(GUIDANCE_RANGE),
                e -> e != shooter && e.isAlive() && !(e instanceof Player))) {

            Vec3 toTarget = candidate.position().subtract(arrow.position());
            double distance = toTarget.length();
            if (distance < 0.5 || toTarget.normalize().dot(heading) < 0.35) {
                continue;
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                best = candidate;
            }
        }

        if (best == null) {
            return;
        }

        // Lead the shot by the target's own velocity.
        Vec3 aim = best.position()
                .add(0.0, best.getBbHeight() * 0.5, 0.0)
                .add(best.getDeltaMovement().scale(LEAD_SECONDS * TICKS_PER_SECOND))
                .subtract(arrow.position());

        if (aim.lengthSqr() < 1.0E-4) {
            return;
        }

        double blend = Math.min(1.0, 0.2 + 0.2 * level);
        Vec3 desired = aim.normalize().scale(velocity.length());
        arrow.setDeltaMovement(velocity.scale(1.0 - blend).add(desired.scale(blend)));
        arrow.hurtMarked = true;
    }

    private static int levelOf(ItemStack stack, ResourceKey<Enchantment> key) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        for (var entry : enchantments.entrySet()) {
            var holderKey = entry.getKey().unwrapKey();
            if (holderKey.isPresent() && holderKey.get().equals(key)) {
                return entry.getValue();
            }
        }
        return 0;
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT,
                ResourceLocation.fromNamespaceAndPath(Gleanings.MODID, path));
    }

    private RangedEnchantHooks() {
    }
}
