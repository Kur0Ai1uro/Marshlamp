package io.tokenwasting.marshlamp.worldgen;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;

public final class KeepWaterProcessor extends StructureProcessor {
    public static final KeepWaterProcessor INSTANCE = new KeepWaterProcessor();
    public static final MapCodec<KeepWaterProcessor> CODEC = MapCodec.unit(INSTANCE);

    private KeepWaterProcessor() {
    }

    @Override
    protected net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType<?> getType() {
        return io.tokenwasting.marshlamp.registry.MarshlampStructures.KEEP_WATER.get();
    }

    @Override
    public StructureTemplate.StructureBlockInfo processBlock(LevelReader level, BlockPos piecePos, BlockPos structurePos, StructureTemplate.StructureBlockInfo original, StructureTemplate.StructureBlockInfo modified, StructurePlaceSettings settings) {
        if (modified.state().isAir() && level.getFluidState(modified.pos()).is(Fluids.WATER)) {
            return null;
        }
        return modified;
    }
}
