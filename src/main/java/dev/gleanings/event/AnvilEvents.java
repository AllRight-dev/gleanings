package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

/**
 * The anvil is the only place vanilla lets you spend an enchanted book, so it is
 * also the only honest place to make one reusable. Two cases:
 *
 *   core + enchanted book  -> a tome carrying that enchantment (core is consumed)
 *   tome + any item        -> the item gets the enchantment; the tome keeps its life
 *
 * The second case needs the tome not to be swallowed by the anvil's take step,
 * which vanilla does unconditionally. See AnvilMenuMixin.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class AnvilEvents {

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        // Vanilla bow + resonance string -> sonic bow. The anvil keeps the bow's
        // existing enchantments and wear, which a crafting recipe would throw away.
        if (left.is(Items.BOW) && right.is(ModItems.RESONANCE_STRING.get())) {
            ItemStack out = new ItemStack(ModItems.RESONANCE_BOW.get());
            ItemEnchantments carried = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (!carried.isEmpty()) {
                out.set(DataComponents.ENCHANTMENTS, carried);
            }
            int wear = left.getOrDefault(DataComponents.DAMAGE, 0);
            if (wear > 0) {
                out.set(DataComponents.DAMAGE, wear);
            }
            event.setOutput(out);
            event.setCost(3);
            event.setMaterialCost(1);
            return;
        }

        if (left.is(Items.ENCHANTED_BOOK) && right.is(ModItems.RESONANCE_DREGS.get())) {
            ItemEnchantments carried = left.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (carried.isEmpty()) {
                return;
            }
            ItemStack out = new ItemStack(ModItems.RESONANCE_TOME.get());
            out.set(DataComponents.ENCHANTMENTS, carried);
            event.setOutput(out);
            event.setCost(1);
            event.setMaterialCost(1);
            return;
        }

        if (right.is(ModItems.RESONANCE_TOME.get())) {
            ItemEnchantments carried = right.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (carried.isEmpty() || left.isEmpty() || left.getCount() != 1) {
                return;
            }
            ItemEnchantments existing = left.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(existing);
            for (var entry : carried.entrySet()) {
                merged.set(entry.getKey(), Math.max(merged.getLevel(entry.getKey()), entry.getValue()));
            }
            ItemStack out = left.copy();
            out.set(DataComponents.ENCHANTMENTS, merged.toImmutable());
            event.setOutput(out);
            event.setCost(1);
            event.setMaterialCost(0);
        }
    }

    private AnvilEvents() {
    }
}
