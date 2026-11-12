package dev.gleanings;

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
        modBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(ModItems.WASHED_ROTTEN_FLESH.get());
        }
    }
}
