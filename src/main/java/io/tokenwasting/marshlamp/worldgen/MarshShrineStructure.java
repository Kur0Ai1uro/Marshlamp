package io.tokenwasting.marshlamp.worldgen;

import java.util.Optional;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.block.MarshWispBlock;
import io.tokenwasting.marshlamp.registry.MarshlampBlocks;
import io.tokenwasting.marshlamp.registry.MarshlampStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.loot.LootTable;

public class MarshShrineStructure extends Structure {
    public static final MapCodec<MarshShrineStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance)
    ).apply(instance, MarshShrineStructure::new));
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(MarshlampMod.MOD_ID, "chests/marsh_shrine"));
    public static final Identifier TEMPLATE_ID = Identifier.fromNamespaceAndPath(MarshlampMod.MOD_ID, "marsh_shrine");

    public MarshShrineStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return onTopOfChunkCenter(context, Heightmap.Types.WORLD_SURFACE_WG, builder -> {
            ChunkPos chunkPos = context.chunkPos();
            int x = chunkPos.getMiddleBlockX();
            int z = chunkPos.getMiddleBlockZ();
            int y = context.chunkGenerator().getFirstOccupiedHeight(
                    x, z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            builder.addPiece(new MarshShrinePiece(new BlockPos(x - 4, y, z - 4)));
        });
    }

    static void place(net.minecraft.world.level.WorldGenLevel level, BlockPos origin, net.minecraft.util.RandomSource random) {
        Optional<StructureTemplate> found = level.getLevel().getStructureManager().get(TEMPLATE_ID);
        if (found.isEmpty()) {
            io.tokenwasting.marshlamp.MarshlampMod.LOGGER.warn("Missing structure template {}", TEMPLATE_ID);
            return;
        }
        StructureTemplate template = found.get();
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(true)
                .addProcessor(KeepWaterProcessor.INSTANCE);
        template.placeInWorld(level, origin, origin, settings, random, 2);
        var size = template.getSize();
        for (BlockPos scan : BlockPos.betweenClosed(origin, origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
            if (level.getBlockEntity(scan) instanceof ChestBlockEntity chest) {
                chest.clearContent();
                chest.setLootTable(LOOT, random.nextLong());
            }
        }
        plantWisps(level, origin, size, random);
    }

    private static void plantWisps(net.minecraft.world.level.WorldGenLevel level, BlockPos origin, net.minecraft.core.Vec3i size, net.minecraft.util.RandomSource random) {
        int placed = 0;
        for (int dx = -2; dx < size.getX() + 2 && placed < 6; dx++) {
            for (int dz = -2; dz < size.getZ() + 2 && placed < 6; dz++) {
                if (dx >= 0 && dz >= 0 && dx < size.getX() && dz < size.getZ()) {
                    continue;
                }
                if (random.nextInt(3) != 0) {
                    continue;
                }
                BlockPos column = origin.offset(dx, 0, dz);
                for (int dy = 2; dy >= -2; dy--) {
                    BlockPos ground = column.offset(0, dy, 0);
                    BlockPos above = ground.above();
                    if (!MarshWispFeature.isSoil(level.getBlockState(ground))) {
                        continue;
                    }
                    if (!level.getFluidState(above).isEmpty() || !level.getBlockState(above).isAir()) {
                        continue;
                    }
                    level.setBlock(above, MarshlampBlocks.MARSH_WISP.get().defaultBlockState().setValue(MarshWispBlock.BLOOMING, true), 2);
                    placed++;
                    break;
                }
            }
        }
    }

    @Override
    public StructureType<?> type() {
        return MarshlampStructures.SHRINE_TYPE.get();
    }
}
