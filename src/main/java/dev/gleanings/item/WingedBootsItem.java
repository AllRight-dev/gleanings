package dev.gleanings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Winged spring boots. Worn in the feet slot.
 *
 * They are not an ArmorItem: armour would drag in an ArmorMaterial and a layer
 * texture for the player model. Instead they are Equippable onto FEET and hand
 * their armour point and step height over as attribute modifiers, which is also
 * what makes "steps up one block" work - that is just step height 1.0.
 */
public class WingedBootsItem extends Item implements Equipable {

    /** Iron boots give 2; these give 1. */
    public static final double ARMOR_POINTS = 1.0;

    /** Vanilla default is 0.6, so +0.4 lets you walk straight up a full block. */
    public static final double STEP_BONUS = 0.4;

    public WingedBootsItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.FEET;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repair) {
        return repair.is(net.minecraft.world.item.Items.PHANTOM_MEMBRANE) || super.isValidRepairItem(stack, repair);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.gleanings.winged_boots.step")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gleanings.winged_boots.jump")
                .withStyle(ChatFormatting.GRAY));
    }
}
