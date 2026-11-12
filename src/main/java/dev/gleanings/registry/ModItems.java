package dev.gleanings.registry;

import dev.gleanings.Gleanings;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Gleanings.MODID);

    /**
     * Rotten flesh that has been scrubbed in a water cauldron. Not edible on its own:
     * it is the intermediate step towards leather.
     */
    public static final DeferredItem<Item> WASHED_ROTTEN_FLESH =
            ITEMS.registerSimpleItem("washed_rotten_flesh", new Item.Properties());

    private ModItems() {
    }
}
