package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModBlocks;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;

/**
 * Carrying a heavy stone in your main hand: 99% knockback resistance in exchange
 * for 90% of your movement speed. Expressed as item attribute modifiers so the
 * numbers show up in the tooltip and stack correctly with potions and armour.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class HeavyStoneAttributes {

    private static final ResourceLocation KNOCKBACK_ID = id("heavy_stone_knockback");
    private static final ResourceLocation SPEED_ID = id("heavy_stone_speed");

    @SubscribeEvent
    public static void onAttributeModifiers(ItemAttributeModifierEvent event) {
        if (!event.getItemStack().is(ModBlocks.HEAVY_DEEPSLATE_ITEM.get())) {
            return;
        }

        event.addModifier(Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(KNOCKBACK_ID, 0.99, AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.MAINHAND);

        event.addModifier(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(SPEED_ID, -0.9, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                EquipmentSlotGroup.MAINHAND);
    }

    /** The heavy shield slows whoever carries it, in either hand. */
    @SubscribeEvent
    public static void onShieldAttributes(ItemAttributeModifierEvent event) {
        if (!event.getItemStack().is(dev.gleanings.registry.ModItems.HEAVY_SHIELD.get())) {
            return;
        }
        event.addModifier(Attributes.MOVEMENT_SPEED,
                new AttributeModifier(id("heavy_shield_speed"), -0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL),
                EquipmentSlotGroup.HAND);
    }

    /**
     * Winged boots: one armour point (iron boots give two) and step height 1.0,
     * which is what "walks up a full block without slowing down" actually is.
     */
    @SubscribeEvent
    public static void onBootsAttributes(ItemAttributeModifierEvent event) {
        if (!event.getItemStack().is(dev.gleanings.registry.ModItems.WINGED_BOOTS.get())) {
            return;
        }
        event.addModifier(Attributes.ARMOR,
                new AttributeModifier(id("winged_boots_armor"),
                        dev.gleanings.item.WingedBootsItem.ARMOR_POINTS,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.FEET);

        event.addModifier(Attributes.STEP_HEIGHT,
                new AttributeModifier(id("winged_boots_step"),
                        dev.gleanings.item.WingedBootsItem.STEP_BONUS,
                        AttributeModifier.Operation.ADD_VALUE),
                EquipmentSlotGroup.FEET);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Gleanings.MODID, path);
    }

    private HeavyStoneAttributes() {
    }
}
