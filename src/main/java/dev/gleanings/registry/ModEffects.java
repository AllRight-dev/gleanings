package dev.gleanings.registry;

import dev.gleanings.Gleanings;
import dev.gleanings.effect.HeavyPresenceEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Gleanings.MODID);

    /**
     * Hostile mobs standing in a beacon's field while a heavy core sits on top of
     * that beacon move 40% slower.
     *
     * An attribute modifier rather than vanilla Slow: Slow comes in 15% steps, so
     * there is no amplifier that lands on exactly 40%.
     */
    public static final Supplier<Holder<MobEffect>> HEAVY_PRESENCE = MOB_EFFECTS.register("heavy_presence",
            HeavyPresenceEffect::new);

    private ModEffects() {
    }
}
