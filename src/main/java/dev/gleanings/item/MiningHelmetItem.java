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
 * Mining helmet: worn in the helmet slot, and while worn a level-15 light block
 * follows the player (see MiningHelmetLight).
 */
public class MiningHelmetItem extends Item implements Equipable {

    public MiningHelmetItem(Properties properties) {
        super(properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.gleanings.mining_helmet")
                .withStyle(ChatFormatting.GRAY));
    }
}
