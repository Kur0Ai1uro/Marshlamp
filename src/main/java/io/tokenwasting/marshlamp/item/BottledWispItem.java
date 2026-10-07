package io.tokenwasting.marshlamp.item;

import java.util.function.Consumer;

import io.tokenwasting.marshlamp.registry.MarshlampDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class BottledWispItem extends Item {
    public static final int MAX_BRIGHTNESS = 100;
    public static final int HARVEST_BRIGHTNESS = 80;

    public BottledWispItem(Properties properties) {
        super(properties.stacksTo(16));
    }

    public static ItemStack withBrightness(int brightness) {
        ItemStack stack = new ItemStack(io.tokenwasting.marshlamp.registry.MarshlampItems.BOTTLED_WISP.get());
        stack.set(MarshlampDataComponents.BRIGHTNESS.get(), Mth.clamp(brightness, 0, MAX_BRIGHTNESS));
        return stack;
    }

    public static int brightnessOf(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(MarshlampDataComponents.BRIGHTNESS.get(), 0), 0, MAX_BRIGHTNESS);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.marshlamp.brightness", brightnessOf(stack)).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("tooltip.marshlamp.bottled_wisp").withStyle(ChatFormatting.GRAY));
    }
}
