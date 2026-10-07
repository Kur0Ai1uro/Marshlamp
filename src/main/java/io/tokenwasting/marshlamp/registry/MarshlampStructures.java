package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.worldgen.KeepWaterProcessor;
import io.tokenwasting.marshlamp.worldgen.MarshShrinePiece;
import io.tokenwasting.marshlamp.worldgen.MarshShrineStructure;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampStructures {
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MarshlampMod.MOD_ID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MarshlampMod.MOD_ID);

    public static final DeferredRegister<StructureProcessorType<?>> PROCESSORS = DeferredRegister.create(Registries.STRUCTURE_PROCESSOR, MarshlampMod.MOD_ID);

    public static final DeferredHolder<StructureType<?>, StructureType<MarshShrineStructure>> SHRINE_TYPE = STRUCTURE_TYPES.register("marsh_shrine",
            () -> () -> MarshShrineStructure.CODEC);

    public static final DeferredHolder<StructurePieceType, StructurePieceType> SHRINE_PIECE = STRUCTURE_PIECES.register("marsh_shrine",
            () -> MarshShrinePiece::new);

    public static final DeferredHolder<StructureProcessorType<?>, StructureProcessorType<KeepWaterProcessor>> KEEP_WATER = PROCESSORS.register("keep_water",
            () -> () -> KeepWaterProcessor.CODEC);

    private MarshlampStructures() {
    }
}
