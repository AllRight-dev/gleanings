package dev.gleanings;

import dev.gleanings.registry.ModBlocks;
import dev.gleanings.registry.ModEffects;
import dev.gleanings.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

@Mod(Gleanings.MODID)
public class Gleanings {
    public static final String MODID = "gleanings";

    public Gleanings(IEventBus modBus, ModContainer container) {
        ModItems.ITEMS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModEffects.MOB_EFFECTS.register(modBus);
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WASHED_ROTTEN_FLESH.get());
            event.accept(ModItems.RESONANCE_CORE.get());
            event.accept(ModItems.RESONANCE_DREGS.get());
            event.accept(ModItems.RESONANCE_STRING.get());
        }
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.RESONANCE_TOME.get());
        }
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(ModItems.RESONANCE_BOW.get());
            event.accept(ModItems.EARPLUGS.get());
            event.accept(ModItems.MINING_HELMET.get());
            event.accept(ModItems.HEAVY_SHIELD.get());
        }
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(ModBlocks.HEAVY_DEEPSLATE_ITEM.get());
            event.accept(ModBlocks.HEAVY_CORE_ITEM.get());
        }
    }
}
