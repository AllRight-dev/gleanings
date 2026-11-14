package dev.gleanings.item;

import dev.gleanings.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * A bow that fires a small warden sonic boom instead of an arrow.
 *
 * It does not spawn WardenSonicBoom: that entity is hardcoded to fire once and
 * die on impact, which cannot pierce and cannot be steered. Instead the shot is
 * a raycast - it passes through everything on the line by construction, the range
 * and damage are ours to pick, and the visuals come from the vanilla sonic boom
 * particle plus the vanilla warden sound.
 *
 * No arrows are consumed; the bow pays with durability.
 */
public class ResonanceBowItem extends Item {

    private static final int CHARGE_TICKS_FULL = 20;
    private static final double BASE_RANGE = 8.0;
    private static final double POWER_RANGE = 14.0;
    private static final float BASE_DAMAGE = 6.0F;
    private static final float POWER_DAMAGE = 6.0F;
    private static final int COOLDOWN_TICKS = 10;

    public ResonanceBowItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (level.isClientSide) {
            level.playSound(player, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 0.5F, 1.6F);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) {
            return;
        }

        int charge = this.getUseDuration(stack, entity) - remainingUseDuration;
        float progress = Math.min(1.0F, charge / (float) CHARGE_TICKS_FULL);
        if (progress < 0.12F) {
            return;
        }

        // A ball of dark blue energy condensing in front of the bow, tightening as
        // the draw completes. Client only - it is decoration, not state.
        Vec3 look = entity.getLookAngle();
        Vec3 hand = entity.getEyePosition().add(look.scale(0.5)).add(0.0, -0.18, 0.0);

        int count = 1 + (int) (progress * 3.0F);
        double radius = 0.18 * (1.05 - progress);

        for (int i = 0; i < count; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 2.0 * radius;
            double oy = (level.random.nextDouble() - 0.5) * 2.0 * radius;
            double oz = (level.random.nextDouble() - 0.5) * 2.0 * radius;
            level.addParticle(ParticleTypes.SCULK_SOUL,
                    hand.x + ox, hand.y + oy, hand.z + oz, 0.0, 0.0, 0.0);
        }

        if (progress > 0.97F && charge % 5 == 0) {
            level.addParticle(ParticleTypes.SONIC_BOOM, hand.x, hand.y, hand.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (!(entity instanceof Player player) || level.isClientSide) {
            return;
        }

        int charge = this.getUseDuration(stack, entity) - timeLeft;
        float power = Math.min(1.0F, charge / (float) CHARGE_TICKS_FULL);
        if (power < 0.15F) {
            return;
        }

        fireSonar(level, player, power);

        player.getCooldowns().addCooldown(this, COOLDOWN_TICKS);
        stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
    }

    private void fireSonar(Level level, Player player, float power) {
        Vec3 eye = player.getEyePosition();
        Vec3 dir = player.getLookAngle().normalize();
        double range = BASE_RANGE + POWER_RANGE * power;
        float damage = BASE_DAMAGE + POWER_DAMAGE * power;

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS, 1.0F, 1.35F);

        Set<UUID> alreadyHit = new HashSet<>();

        // Step along the line. Anything whose box the step point falls inside is hit
        // once - which makes the shot pierce by construction, not by exception.
        for (double travelled = 0.5; travelled <= range; travelled += 0.5) {
            Vec3 point = eye.add(dir.scale(travelled));

            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.SONIC_BOOM, point.x, point.y, point.z, 1, 0.0, 0.0, 0.0, 0.0);
            }

            AABB probe = new AABB(point.x - 0.7, point.y - 0.7, point.z - 0.7,
                    point.x + 0.7, point.y + 0.7, point.z + 0.7);

            for (Entity candidate : level.getEntities(player, probe)) {
                if (!(candidate instanceof LivingEntity target) || !target.isAlive()) {
                    continue;
                }
                if (target == player || !alreadyHit.add(target.getUUID())) {
                    continue;
                }
                target.hurt(level.damageSources().sonicBoom(player), damage);
                target.push(dir.x * 0.45, 0.18, dir.z * 0.45);
                target.hurtMarked = true;
            }
        }
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(ModItems.RESONANCE_STRING.get()) || super.isValidRepairItem(stack, repair);
    }
}
