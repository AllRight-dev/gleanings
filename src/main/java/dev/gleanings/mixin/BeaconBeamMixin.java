package dev.gleanings.mixin;

import dev.gleanings.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/**
 * A heavy core on top turns the beacon beam purple-black.
 *
 * The beam colour is normally decided by stained glass stacked above the beacon,
 * and getBeamSections() is where that list is assembled - so we rewrite the list
 * on the way out. Nothing else about the beam changes: same height, same
 * visibility rule.
 */
@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBeamMixin {

    private static final float[] HEAVY_COLOR = {0.42F, 0.12F, 0.55F};

    @Inject(method = "getBeamSections", at = @At("RETURN"), cancellable = true)
    private void gleanings$heavyBeam(CallbackInfoReturnable<List<BeaconBlockEntity.BeamSection>> cir) {
        List<BeaconBlockEntity.BeamSection> sections = cir.getReturnValue();
        if (sections == null || sections.isEmpty()) {
            return;
        }

        BeaconBlockEntity self = (BeaconBlockEntity) (Object) this;
        Level level = self.getLevel();
        if (level == null) {
            return;
        }

        BlockPos above = self.getBlockPos().above();
        BlockState state = level.getBlockState(above);
        if (!state.is(ModBlocks.HEAVY_CORE.get())) {
            return;
        }

        int totalHeight = sections.stream().mapToInt(BeaconBlockEntity.BeamSection::height).sum();
        cir.setReturnValue(List.of(new BeaconBlockEntity.BeamSection(HEAVY_COLOR.clone(), totalHeight)));
    }
}
