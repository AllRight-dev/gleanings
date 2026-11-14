package dev.gleanings.event;

import dev.gleanings.Gleanings;
import dev.gleanings.registry.ModBlocks;
import dev.gleanings.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Monsters inside a beacon's field move 40% slower while a heavy core caps that
 * beacon.
 *
 * Found by walking the chunks around players rather than by hooking the beacon:
 * beacons are sparse, loaded chunks are few, and this way nothing in
 * BeaconBlockEntity has to be touched. The beacon's field size is recomputed from
 * its pyramid (vanilla: level * 10 + 10) so a four-layer beacon covers 50 blocks.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class HeavyCoreBeacon {

    private static final int CHUNK_RADIUS = 4;
    private static final int REFRESH_TICKS = 20;
    private static final int EFFECT_TICKS = 100;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % REFRESH_TICKS != 0) {
            return;
        }
        if (level.players().isEmpty()) {
            return;
        }

        for (ServerPlayer player : level.players()) {
            int centerChunkX = player.chunkPosition().x;
            int centerChunkZ = player.chunkPosition().z;

            for (int dx = -CHUNK_RADIUS; dx <= CHUNK_RADIUS; dx++) {
                for (int dz = -CHUNK_RADIUS; dz <= CHUNK_RADIUS; dz++) {
                    LevelChunk chunk = level.getChunkSource().getChunkNow(centerChunkX + dx, centerChunkZ + dz);
                    if (chunk == null) {
                        continue;
                    }
                    for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                        if (blockEntity instanceof BeaconBlockEntity beacon) {
                            applyIfCapped(level, beacon.getBlockPos());
                        }
                    }
                }
            }
        }
    }

    private static void applyIfCapped(ServerLevel level, BlockPos beaconPos) {
        BlockPos above = beaconPos.above();
        if (!level.getBlockState(above).is(ModBlocks.HEAVY_CORE.get())) {
            return;
        }

        int pyramid = pyramidLevel(level, beaconPos);
        if (pyramid <= 0) {
            return;
        }

        double range = pyramid * 10.0 + 10.0;
        AABB field = new AABB(beaconPos).inflate(range).expandTowards(0.0, level.getHeight(), 0.0);

        for (Mob mob : level.getEntitiesOfClass(Mob.class, field, m -> m.isAlive())) {
            mob.addEffect(new MobEffectInstance(ModEffects.HEAVY_PRESENCE.get(), EFFECT_TICKS, 0,
                    false, false, false));
        }
    }

    /** Counts the beacon base layers below the beacon, same rule vanilla uses. */
    private static int pyramidLevel(ServerLevel level, BlockPos beaconPos) {
        int found = 0;
        for (int layer = 1; layer <= 4; layer++) {
            boolean complete = true;
            outer:
            for (int dx = -layer; dx <= layer; dx++) {
                for (int dz = -layer; dz <= layer; dz++) {
                    if (!level.getBlockState(beaconPos.offset(dx, -layer, dz)).is(BlockTags.BEACON_BASE_BLOCKS)) {
                        complete = false;
                        break outer;
                    }
                }
            }
            if (!complete) {
                break;
            }
            found = layer;
        }
        return found;
    }

    private HeavyCoreBeacon() {
    }
}
