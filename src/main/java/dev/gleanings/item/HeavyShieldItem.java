package dev.gleanings.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * A shield that does not need to be raised.
 *
 * Vanilla blocking is a state of "using" the item, so it cannot be switched on
 * passively. This shield instead swallows frontal hits in
 * HeavyShieldBlocking, and pays for it with a five-hit charge and a 20% walk
 * speed penalty just for carrying it.
 */
public class HeavyShieldItem extends ShieldItem {

    /** Hits absorbed before the shield needs to recover. */
    public static final int BLOCKS_PER_CHARGE = 5;

    /** 120 ticks, a little longer than the 100 a vanilla shield needs. */
    public static final int COOLDOWN_TICKS = 120;

    public HeavyShieldItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.gleanings.heavy_shield.blocks")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gleanings.heavy_shield.slow")
                .withStyle(ChatFormatting.GRAY));
    }
}
