package dev.gleanings.mixin;

import dev.gleanings.item.EarplugsItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vanilla refuses sleep when a monster is nearby (BedSleepingProblem.NOT_SAFE).
 * With earplugs in, that specific refusal is not a refusal any more - every other
 * reason to be turned away (daytime, not a bed, already sleeping) still applies.
 *
 * Targeting the return value rather than the monster scan keeps the change to one
 * line of behaviour: we only ever convert NOT_SAFE into success.
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerSleepMixin {

    @Inject(method = "startSleepInBed", at = @At("RETURN"), cancellable = true)
    private void gleanings$sleepThroughMonsters(BlockPos pos,
                                                CallbackInfoReturnable<com.mojang.datafixers.util.Either<Player.BedSleepingProblem, Unit>> cir) {
        var result = cir.getReturnValue();
        if (result == null || result.left().isEmpty()) {
            return;
        }
        if (result.left().get() != Player.BedSleepingProblem.NOT_SAFE) {
            return;
        }

        ServerPlayer self = (ServerPlayer) (Object) this;
        if (!EarplugsItem.isWorn(self)) {
            return;
        }

        cir.setReturnValue(com.mojang.datafixers.util.Either.right(Unit.INSTANCE));
    }
}
