package dev.gleanings.registry;

import dev.gleanings.Gleanings;
import dev.gleanings.item.EarplugsItem;
import dev.gleanings.item.HeavyShieldItem;
import dev.gleanings.item.MiningHelmetItem;
import dev.gleanings.item.ResonanceBowItem;
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
            ITEMS.registerSimpleItem("resonance_core", new Item.Properties().rarity(Rarity.UNCOMMON));

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

    /** Core ground with an echo shard: the string that carries resonance into a bow. */
    public static final DeferredItem<Item> RESONANCE_STRING =
            ITEMS.registerSimpleItem("resonance_string", new Item.Properties().rarity(Rarity.UNCOMMON));

    /**
     * Fires a small warden sonic boom. No arrows, pays in durability.
     * 384 uses, same as a vanilla bow.
     */
    public static final DeferredItem<Item> RESONANCE_BOW =
            ITEMS.register("resonance_bow",
                    () -> new ResonanceBowItem(new Item.Properties().durability(384).rarity(Rarity.RARE)));

    /**
     * Worn in the helmet slot: you can sleep with monsters around, and sonic
     * damage is cut by 40%. The slot is the cost - no helmet while these are in.
     */
    public static final DeferredItem<Item> EARPLUGS =
            ITEMS.register("earplugs", () -> new EarplugsItem(new Item.Properties().stacksTo(1)));

    /** A helmet with a lamp in it: level-15 light follows the wearer. */
    public static final DeferredItem<Item> MINING_HELMET =
            ITEMS.register("mining_helmet", () -> new MiningHelmetItem(new Item.Properties().stacksTo(1)));

    /** A shield that blocks on its own, five hits at a time. */
    public static final DeferredItem<Item> HEAVY_SHIELD =
            ITEMS.register("heavy_shield", () -> new HeavyShieldItem(new Item.Properties()
                    .stacksTo(1)
                    .durability(HeavyShieldItem.BLOCKS_PER_CHARGE)
                    .rarity(Rarity.RARE)));

    private ModItems() {
    }
}
