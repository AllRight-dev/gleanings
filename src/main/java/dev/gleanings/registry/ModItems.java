package dev.gleanings.registry;

import dev.gleanings.Gleanings;
import dev.gleanings.item.ResonanceCoreItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
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

    /**
     * What is left of a budding amethyst once the growth engine is taken apart.
     * A budding amethyst drops nothing in vanilla - this is the thing it drops now.
     * Grind it on a grindstone to get the dregs that actually carry enchantments.
     */
    public static final DeferredItem<Item> RESONANCE_CORE =
            ITEMS.register("resonance_core",
                    () -> new ResonanceCoreItem(new Item.Properties().rarity(Rarity.UNCOMMON)));

    /**
     * An enchanted book with its enchantment held in amethyst instead of paper.
     * Five uses, then it wears out. Vanilla renders the foil and the durability bar
     * on its own as soon as the stack carries enchantments and a max damage.
     */
    public static final DeferredItem<Item> RESONANCE_TOME =
            ITEMS.registerSimpleItem("resonance_tome",
                    new Item.Properties().stacksTo(1).durability(5).rarity(Rarity.RARE));

    /** What a worn-out tome crumbles into. Purpose not designed yet. */
    public static final DeferredItem<Item> RESONANCE_DREGS =
            ITEMS.registerSimpleItem("resonance_dregs", new Item.Properties());

    private ModItems() {
    }
}
