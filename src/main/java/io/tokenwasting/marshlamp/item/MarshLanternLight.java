package io.tokenwasting.marshlamp.item;

import javax.annotation.Nullable;

import io.tokenwasting.marshlamp.registry.MarshlampBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class MarshLanternLight {
    private static final String KEY = "marshlamp_glow";
    private static final int DRAIN_INTERVAL = 40;

    private MarshLanternLight() {
    }

    public static void tick(ServerPlayer player) {
        if (storedInAnotherDimension(player)) {
            clear(player);
        }
        var stack = MarshLanternItem.activeStack(player);
        if (stack.isEmpty()) {
            clear(player);
            return;
        }
        if (player.tickCount % DRAIN_INTERVAL == 0) {
            MarshLanternItem.setBrightness(stack, MarshLanternItem.brightnessOf(stack) - 1);
            if (MarshLanternItem.brightnessOf(stack) <= 0) {
                clear(player);
                return;
            }
        }
        BlockPos owned = ownedPos(player, player.level());
        BlockPos target = findAir(player, owned);
        if (target == null) {
            if (owned != null) {
                clear(player);
            }
            return;
        }
        if (target.equals(owned)) {
            return;
        }
        clear(player);
        place(player, target);
    }

    public static void clear(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) {
            return;
        }
        CompoundTag tag = root.getCompoundOrEmpty(KEY);
        String dimension = tag.getStringOr("dim", "");
        if (!dimension.isEmpty()) {
            Identifier dimensionId = Identifier.parse(dimension);
            ServerLevel level = player.level().getServer().getLevel(ResourceKey.create(Registries.DIMENSION, dimensionId));
            if (level != null) {
                BlockPos pos = new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
                if (level.getBlockState(pos).is(MarshlampBlocks.MARSH_GLOW.get())) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        root.remove(KEY);
    }

    private static boolean storedInAnotherDimension(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) {
            return false;
        }
        return !player.level().dimension().identifier().toString().equals(root.getCompoundOrEmpty(KEY).getStringOr("dim", ""));
    }

    private static void place(ServerPlayer player, BlockPos pos) {
        ServerLevel level = player.level();
        if (!canOccupy(level, pos, null)) {
            return;
        }
        level.setBlock(pos, MarshlampBlocks.MARSH_GLOW.get().defaultBlockState(), 3);
        CompoundTag tag = new CompoundTag();
        tag.putInt("x", pos.getX());
        tag.putInt("y", pos.getY());
        tag.putInt("z", pos.getZ());
        tag.putString("dim", level.dimension().identifier().toString());
        player.getPersistentData().put(KEY, tag);
    }

    @Nullable
    private static BlockPos ownedPos(ServerPlayer player, Level level) {
        CompoundTag root = player.getPersistentData();
        if (!root.contains(KEY)) {
            return null;
        }
        CompoundTag tag = root.getCompoundOrEmpty(KEY);
        if (!level.dimension().identifier().toString().equals(tag.getStringOr("dim", ""))) {
            return null;
        }
        return new BlockPos(tag.getIntOr("x", 0), tag.getIntOr("y", 0), tag.getIntOr("z", 0));
    }

    @Nullable
    private static BlockPos findAir(ServerPlayer player, @Nullable BlockPos owned) {
        ServerLevel level = player.level();
        BlockPos feet = player.blockPosition();
        if (canOccupy(level, feet, owned)) {
            return feet;
        }
        BlockPos head = feet.above();
        if (canOccupy(level, head, owned)) {
            return head;
        }
        return null;
    }

    private static boolean canOccupy(ServerLevel level, BlockPos pos, @Nullable BlockPos owned) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        return owned != null && owned.equals(pos) && state.is(MarshlampBlocks.MARSH_GLOW.get());
    }
}
