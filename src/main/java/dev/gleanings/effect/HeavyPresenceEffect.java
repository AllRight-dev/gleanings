package dev.gleanings.effect;

import dev.gleanings.Gleanings;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Movement speed -40% for as long as it lasts.
 *
 * Its own effect rather than vanilla Slow, because Slow only comes in 15% steps
 * and no amplifier lands on exactly 40%.
 */
public class HeavyPresenceEffect extends MobEffect {

    public HeavyPresenceEffect() {
        super(MobEffectCategory.HARMFUL, 0x3A2A4A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(Gleanings.MODID, "heavy_presence_speed"),
                -0.4,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
}
