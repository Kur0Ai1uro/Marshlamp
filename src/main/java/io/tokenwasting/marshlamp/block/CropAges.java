package io.tokenwasting.marshlamp.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeetrootBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public final class CropAges {
    private static final IntegerProperty[] AGE_PROPERTIES = {
            CropBlock.AGE,
            BeetrootBlock.AGE,
            BlockStateProperties.AGE_1,
            BlockStateProperties.AGE_2,
            BlockStateProperties.AGE_3,
            BlockStateProperties.AGE_4,
            BlockStateProperties.AGE_5,
            BlockStateProperties.AGE_7
    };
    private CropAges() {
    }

    public static boolean canGrow(BlockState state) {
        if (!(state.getBlock() instanceof CropBlock crop)) {
            return false;
        }
        IntegerProperty ageProperty = ageProperty(state);
        return ageProperty != null && state.getValue(ageProperty) < crop.getMaxAge();
    }

    public static boolean grow(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CropBlock crop)) {
            return false;
        }
        IntegerProperty ageProperty = ageProperty(state);
        if (ageProperty == null) {
            return false;
        }
        int age = state.getValue(ageProperty);
        if (age >= crop.getMaxAge()) {
            return false;
        }
        level.setBlock(pos, state.setValue(ageProperty, age + 1), 3);
        return true;
    }

    private static IntegerProperty ageProperty(BlockState state) {
        for (IntegerProperty property : AGE_PROPERTIES) {
            if (state.hasProperty(property)) {
                return property;
            }
        }
        return null;
    }
}
