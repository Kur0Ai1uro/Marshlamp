package io.tokenwasting.marshlamp.worldgen;

import io.tokenwasting.marshlamp.registry.MarshlampStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class MarshShrinePiece extends StructurePiece {
    private BlockPos probe;
    private BlockPos origin;
    private boolean placed;

    public MarshShrinePiece(BlockPos probe) {
        super(MarshlampStructures.SHRINE_PIECE.get(), 0, cover(probe));
        this.probe = probe;
    }

    public MarshShrinePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(MarshlampStructures.SHRINE_PIECE.get(), tag);
        this.probe = new BlockPos(tag.getIntOr("ProbeX", 0), tag.getIntOr("ProbeY", 0), tag.getIntOr("ProbeZ", 0));
        this.placed = tag.getBooleanOr("Placed", false);
        if (this.placed) {
            this.origin = new BlockPos(tag.getIntOr("OriginX", 0), tag.getIntOr("OriginY", 0), tag.getIntOr("OriginZ", 0));
        }
    }

    private static BoundingBox cover(BlockPos probe) {
        return BoundingBox.fromCorners(probe.offset(0, -24, 0), probe.offset(10, 20, 10));
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("ProbeX", this.probe.getX());
        tag.putInt("ProbeY", this.probe.getY());
        tag.putInt("ProbeZ", this.probe.getZ());
        tag.putBoolean("Placed", this.placed);
        if (this.origin != null) {
            tag.putInt("OriginX", this.origin.getX());
            tag.putInt("OriginY", this.origin.getY());
            tag.putInt("OriginZ", this.origin.getZ());
        }
    }

    @Override
    public void postProcess(WorldGenLevel level, net.minecraft.world.level.StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBox, ChunkPos chunkPos, BlockPos reference) {
        if (this.origin == null) {
            BlockPos soil = findSoil(level, this.probe.offset(4, 0, 4));
            BlockPos ground = soil != null ? soil : this.probe.offset(4, 0, 4);
            this.origin = new BlockPos(ground.getX() - 4, ground.getY(), ground.getZ() - 4);
            this.boundingBox = BoundingBox.fromCorners(this.origin, this.origin.offset(8, 16, 8));
            this.placed = true;
        }
        MarshShrineStructure.place(level, this.origin, random);
    }

    private static BlockPos findSoil(WorldGenLevel level, BlockPos start) {
        BlockPos.MutableBlockPos cursor = start.mutable();
        for (int step = 0; step < 32; step++) {
            BlockState state = level.getBlockState(cursor);
            if (MarshWispFeature.isSoil(state)) {
                return cursor.immutable();
            }
            if (cursor.getY() <= level.getMinY() + 2) {
                return null;
            }
            cursor.move(Direction.DOWN);
        }
        return null;
    }
}
