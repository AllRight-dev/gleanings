package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The mining helmet's light. Vanilla has no dynamic light, but it does have the
 * invisible `minecraft:light` block - so the helmet parks a level-15 light block
 * on the player and puts the old state back when they move or take it off.
 *
 * Works for everyone server side, not just the wearer's client.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class MiningHelmetLight {

    private record Placed(BlockPos pos, BlockState previous) {
    }

    private static final Map<UUID, Placed> LIGHTS = new HashMap<>();

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean wearing = player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.MINING_HELMET.get());
        Placed current = LIGHTS.get(player.getUUID());

        if (!wearing) {
            restore(player, current);
            return;
        }

        BlockPos target = player.blockPosition();
        if (current != null && current.pos().equals(target)) {
            return;
        }

        restore(player, current);

        BlockState there = player.level().getBlockState(target);
        if (!there.isAir()) {
            LIGHTS.remove(player.getUUID());
            return;
        }

        player.level().setBlockAndUpdate(target, Blocks.LIGHT.defaultBlockState()
                .setValue(BlockStateProperties.LEVEL, 15));
        LIGHTS.put(player.getUUID(), new Placed(target, there));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            restore(player, LIGHTS.get(player.getUUID()));
        }
    }

    private static void restore(ServerPlayer player, Placed placed) {
        if (placed == null) {
            return;
        }
        LIGHTS.remove(player.getUUID());
        if (player.level().getBlockState(placed.pos()).is(Blocks.LIGHT)) {
            player.level().setBlockAndUpdate(placed.pos(), placed.previous());
        }
    }

    private MiningHelmetLight() {
    }
}
