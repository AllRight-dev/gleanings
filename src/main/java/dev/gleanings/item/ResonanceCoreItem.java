package dev.gleanings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The core is deliberately a one-step item: it is the only part of the chain a
 * player has to guess at, so it says what it is for on hover. Everything else in
 * the chain is discoverable through JEI or the anvil itself.
 */
public class ResonanceCoreItem extends Item {

    public ResonanceCoreItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.gleanings.resonance_core")
                .withStyle(ChatFormatting.GRAY));
    }
}
