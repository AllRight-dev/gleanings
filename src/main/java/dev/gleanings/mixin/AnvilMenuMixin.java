package dev.gleanings.mixin;

import dev.gleanings.registry.ModItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Vanilla's AnvilMenu#onTake clears the second slot unconditionally, which would
 * eat the tome on first use. Capture it before, put a worn copy back after.
 *
 * This is the one mixin in the project; everything else is data or events.
 */
@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {

    @Unique
    private ItemStack gleanings$tome = ItemStack.EMPTY;

    @Inject(method = "onTake", at = @At("HEAD"))
    private void gleanings$captureTome(Player player, ItemStack stack, CallbackInfo ci) {
        ItemStack second = ((AbstractContainerMenu) (Object) this).getSlot(1).getItem();
        this.gleanings$tome = second.is(ModItems.RESONANCE_TOME.get()) ? second.copy() : ItemStack.EMPTY;
    }

    @Inject(method = "onTake", at = @At("TAIL"))
    private void gleanings$restoreTome(Player player, ItemStack stack, CallbackInfo ci) {
        ItemStack tome = this.gleanings$tome;
        this.gleanings$tome = ItemStack.EMPTY;
        if (tome.isEmpty()) {
            return;
        }

        AbstractContainerMenu menu = (AbstractContainerMenu) (Object) this;
        ItemStack worn = tome.copy();
        int next = worn.getDamageValue() + 1;

        if (next >= worn.getMaxDamage()) {
            ItemStack dregs = new ItemStack(ModItems.RESONANCE_DREGS.get());
            if (!player.getInventory().add(dregs)) {
                player.drop(dregs, false);
            }
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.PLAYERS, 0.6F, 0.55F);
        } else {
            worn.setDamageValue(next);
            menu.getSlot(1).set(worn);
            // The amethyst chime, pitched down: the same sound the crystal makes, gone muddy.
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.5F, 0.6F);
        }
    }
}
