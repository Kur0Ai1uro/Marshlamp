package io.tokenwasting.marshlamp.worldgen;

import java.util.Optional;

import io.tokenwasting.marshlamp.MarshlampConfig;
import io.tokenwasting.marshlamp.MarshlampMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;

public class MarshShrineFeature extends Feature<NoneFeatureConfiguration> {
    public static final ResourceKey<LootTable> LOOT = ResourceKey.create(Registries.LOOT_TABLE,
            Identifier.fromNamespaceAndPath(MarshlampMod.MOD_ID, "chests/marsh_shrine"));
    private static final Identifier TEMPLATE_ID = Identifier.fromNamespaceAndPath(MarshlampMod.MOD_ID, "marsh_shrine");

    public MarshShrineFeature(com.mojang.serialization.Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        if (random.nextInt(Math.max(1, MarshlampConfig.shrineRarity())) != 0) {
            return false;
        }
        Optional<StructureTemplate> found = level.getLevel().getStructureManager().get(TEMPLATE_ID);
        if (found.isEmpty()) {
            return false;
        }
        StructureTemplate template = found.get();
        Vec3i size = template.getSize();
        BlockPos origin = fitInChunk(level, context.origin(), size);
        if (origin == null || !nearWater(level, origin, size) || nearShrine(level, origin)) {
            return false;
        }
        support(level, origin, size);
        StructurePlaceSettings settings = new StructurePlaceSettings().setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
        if (!template.placeInWorld(level, origin, origin, settings, random, 2)) {
            return false;
        }
        fillChests(level, origin, size, random);
        return true;
    }

    private static BlockPos fitInChunk(WorldGenLevel level, BlockPos probe, Vec3i size) {
        ChunkAccess chunk = level.getChunk(probe);
        int minX = chunk.getPos().getMinBlockX();
        int minZ = chunk.getPos().getMinBlockZ();
        int roomX = Math.max(0, 16 - size.getX());
        int roomZ = Math.max(0, 16 - size.getZ());
        int localX = Math.min(Math.max(0, probe.getX() - minX), roomX);
        int localZ = Math.min(Math.max(0, probe.getZ() - minZ), roomZ);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(minX + localX + size.getX() / 2, probe.getY(), minZ + localZ + size.getZ() / 2);
        for (int step = 0; step < 24; step++) {
            BlockState state = level.getBlockState(cursor);
            if (MarshWispFeature.isSoil(state) && state.isFaceSturdy(level, cursor, Direction.UP)) {
                return new BlockPos(minX + localX, cursor.getY(), minZ + localZ);
            }
            if (!state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced() && !MarshWispFeature.isSoil(state)
                    && !state.is(net.minecraft.tags.BlockTags.LEAVES) && !state.is(net.minecraft.tags.BlockTags.LOGS)) {
                return null;
            }
            cursor.move(Direction.DOWN);
            if (cursor.getY() <= level.getMinY() + 2) {
                return null;
            }
        }
        return null;
    }

    private static void support(WorldGenLevel level, BlockPos origin, Vec3i size) {
        for (int dx = 0; dx < size.getX(); dx++) {
            for (int dz = 0; dz < size.getZ(); dz++) {
                BlockPos.MutableBlockPos cursor = origin.offset(dx, -1, dz).mutable();
                for (int step = 0; step < 6; step++) {
                    BlockState state = level.getBlockState(cursor);
                    if (state.isFaceSturdy(level, cursor, Direction.UP)) {
                        break;
                    }
                    if (!state.isAir() && state.getFluidState().isEmpty() && !state.canBeReplaced()) {
                        break;
                    }
                    level.setBlock(cursor, Blocks.MUD.defaultBlockState(), 2);
                    cursor.move(Direction.DOWN);
                }
            }
        }
    }

    private static boolean nearWater(WorldGenLevel level, BlockPos origin, Vec3i size) {
        BlockPos center = origin.offset(size.getX() / 2, 0, size.getZ() / 2);
        for (BlockPos scan : BlockPos.betweenClosed(center.offset(-8, -3, -8), center.offset(8, 2, 8))) {
            if (level.getFluidState(scan).is(Fluids.WATER)) {
                return true;
            }
        }
        return false;
    }

    private static boolean nearShrine(WorldGenLevel level, BlockPos origin) {
        for (BlockPos scan : BlockPos.betweenClosed(origin.offset(-12, -4, -12), origin.offset(12, 8, 12))) {
            if (level.getBlockState(scan).is(Blocks.CHISELED_STONE_BRICKS)) {
                return true;
            }
        }
        return false;
    }

    private static void fillChests(WorldGenLevel level, BlockPos origin, Vec3i size, RandomSource random) {
        for (BlockPos scan : BlockPos.betweenClosed(origin, origin.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1))) {
            if (level.getBlockEntity(scan) instanceof ChestBlockEntity chest) {
                chest.clearContent();
                chest.setLootTable(LOOT, random.nextLong());
            }
        }
    }
}
