package io.tokenwasting.marshlamp.item;

import java.util.function.Consumer;

import io.tokenwasting.marshlamp.registry.MarshlampDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;

public final class Wick {
    public static final int MAX = 64;

    private Wick() {
    }

    public static int get(ItemStack stack) {
        return stack.getOrDefault(MarshlampDataComponents.WICK.get(), 0);
    }

    public static void set(ItemStack stack, int amount) {
        int clamped = Mth.clamp(amount, 0, MAX);
        if (clamped <= 0) {
            stack.remove(MarshlampDataComponents.WICK.get());
        } else {
            stack.set(MarshlampDataComponents.WICK.get(), clamped);
        }
    }

    public static void add(ItemStack stack, int delta) {
        set(stack, get(stack) + delta);
    }

    public static float boostSpeed(ItemStack stack, float baseSpeed) {
        if (baseSpeed > 1.0F && get(stack) > 0) {
            return baseSpeed + 2.0F;
        }
        return baseSpeed;
    }

    public static void consumeIfMined(ItemStack stack, Level level, BlockState state, BlockPos pos) {
        if (!level.isClientSide() && get(stack) > 0 && state.getDestroySpeed(level, pos) != 0.0F) {
            add(stack, -1);
        }
    }

    public static void addTooltip(ItemStack stack, Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable("tooltip.marshlamp.wick", get(stack), MAX).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("tooltip.marshlamp.tool").withStyle(ChatFormatting.GRAY));
    }

    public static int barWidth(ItemStack stack) {
        return Math.round(get(stack) * 13.0F / MAX);
    }
}
