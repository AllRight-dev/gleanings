package dev.gleanings.block;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.Block;

/**
 * A block that tints a beacon beam that passes through it.
 *
 * Vanilla already supports this: anything implementing BeaconBeamBlock supplies a
 * beam colour, which is how stained glass turns a beacon purple. No mixin, and the
 * beam keeps its height and visibility rules.
 */
public class HeavyCoreBlock extends Block implements BeaconBeamBlock {

    public HeavyCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public DyeColor getBeamColor() {
        return DyeColor.PURPLE;
    }
}
