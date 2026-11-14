package dev.gleanings.client;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * A bow's draw animation is driven by item model overrides keyed on the "pull"
 * and "pulling" properties. Vanilla registers those two predicates against the
 * Items.BOW instance only, so a custom bow silently gets no animation at all -
 * the overrides never match and the icon never changes.
 *
 * We register our own, under our own namespace, and point the model at those.
 */
@EventBusSubscriber(modid = Gleanings.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.RESONANCE_BOW.get(), id("pulling"),
                    (stack, level, entity, seed) ->
                            entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);

            ItemProperties.register(ModItems.RESONANCE_BOW.get(), id("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null || entity.getUseItem() != stack) {
                            return 0.0F;
                        }
                        float charge = stack.getUseDuration(entity) - entity.getUseItemRemainingTicks();
                        return Math.min(1.0F, charge / 20.0F);
                    });
        });
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Gleanings.MODID, path);
    }

    private ClientSetup() {
    }
}
