package dev.gleanings.item;

import dev.gleanings.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Earplugs. Worn in the helmet slot, which is the cost: no helmet while the
 * plugs are in.
 *
 * Two effects, both checked by looking at the head slot:
 *   - you can sleep with monsters nearby
 *   - sonic damage is reduced by 40%
 */
public class EarplugsItem extends Item {

    public static final float SONIC_DAMAGE_MULTIPLIER = 0.6F;

    public EarplugsItem(Properties properties) {
        super(properties);
    }

    public static boolean isWorn(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.EARPLUGS.get());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.gleanings.earplugs.sleep")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gleanings.earplugs.sonic")
                .withStyle(ChatFormatting.GRAY));
    }
}
