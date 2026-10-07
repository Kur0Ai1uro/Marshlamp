package io.tokenwasting.marshlamp.worldgen;

import io.tokenwasting.marshlamp.block.MarshWispBlock;
import io.tokenwasting.marshlamp.registry.MarshlampBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class MarshWispFeature extends Feature<NoneFeatureConfiguration> {
    public MarshWispFeature(com.mojang.serialization.Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos.MutableBlockPos cursor = context.origin().mutable();
        for (int step = 0; step < 24; step++) {
            BlockState state = level.getBlockState(cursor);
            if (isSoil(state)) {
                BlockPos above = cursor.above();
                if (!canPlant(level.getBlockState(above))) {
                    return false;
                }
                level.setBlock(above, MarshlampBlocks.MARSH_WISP.get().defaultBlockState().setValue(MarshWispBlock.BLOOMING, true), 2);
                return true;
            }
            if (!isCanopy(state)) {
                return false;
            }
            cursor.move(Direction.DOWN);
        }
        return false;
    }

    static boolean isSoil(BlockState state) {
        return state.is(BlockTags.DIRT)
                || state.is(Blocks.MUD)
                || state.is(Blocks.CLAY)
                || state.is(Blocks.MOSS_BLOCK)
                || state.is(Blocks.MUDDY_MANGROVE_ROOTS);
    }

    private static boolean isCanopy(BlockState state) {
        return state.isAir()
                || !state.getFluidState().isEmpty()
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.LOGS)
                || state.is(Blocks.MANGROVE_ROOTS)
                || state.is(Blocks.VINE)
                || state.is(Blocks.MANGROVE_PROPAGULE)
                || state.canBeReplaced();
    }

    private static boolean canPlant(BlockState state) {
        return state.isAir() || (state.canBeReplaced() && state.getFluidState().isEmpty());
    }
}
