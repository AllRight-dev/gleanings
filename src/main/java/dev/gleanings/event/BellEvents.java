package dev.gleanings.event;

import dev.gleanings.Gleanings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Right-click a bell to read the clock. The bell keeps its vanilla ringing behaviour;
 * this only adds an action bar line on top of it.
 */
@EventBusSubscriber(modid = Gleanings.MODID)
public final class BellEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Level level = event.getLevel();
        if (level.isClientSide || !level.getBlockState(event.getPos()).is(Blocks.BELL)) {
            return;
        }

        Player player = event.getEntity();

        long dayTime = level.getDayTime();
        long day = dayTime / 24000L;
        int ticks = (int) (dayTime % 24000L);

        // Minecraft day starts at 06:00.
        int hours = (ticks / 1000 + 6) % 24;
        int minutes = (ticks % 1000) * 60 / 1000;
        String clock = String.format("%02d:%02d", hours, minutes);

        Component moon = Component.translatable("message.gleanings.moon." + level.getMoonPhase());
        Component weather = Component.translatable("message.gleanings.weather." + weatherId(level));

        player.displayClientMessage(
                Component.translatable("message.gleanings.bell", day, clock, moon, weather)
                        .withStyle(ChatFormatting.GRAY),
                true);
    }

    private static int weatherId(Level level) {
        if (level.isThundering()) {
            return 2;
        }
        return level.isRaining() ? 1 : 0;
    }

    private BellEvents() {
    }
}
