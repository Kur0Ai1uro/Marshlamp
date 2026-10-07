package io.tokenwasting.marshlamp.item;

import java.util.function.Consumer;

import io.tokenwasting.marshlamp.registry.MarshlampDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class MarshLanternItem extends Item {
    public MarshLanternItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    public static ItemStack activeStack(Player player) {
        ItemStack main = player.getMainHandItem();
        if (isLit(main)) {
            return main;
        }
        ItemStack offhand = player.getOffhandItem();
        if (isLit(offhand)) {
            return offhand;
        }
        return ItemStack.EMPTY;
    }

    public static boolean isLit(ItemStack stack) {
        return stack.getItem() instanceof MarshLanternItem && brightnessOf(stack) > 0;
    }

    public static int brightnessOf(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(MarshlampDataComponents.BRIGHTNESS.get(), 0), 0, BottledWispItem.MAX_BRIGHTNESS);
    }

    public static void setBrightness(ItemStack stack, int brightness) {
        int clamped = Mth.clamp(brightness, 0, BottledWispItem.MAX_BRIGHTNESS);
        if (clamped <= 0) {
            stack.remove(MarshlampDataComponents.BRIGHTNESS.get());
        } else {
            stack.set(MarshlampDataComponents.BRIGHTNESS.get(), clamped);
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return brightnessOf(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(brightnessOf(stack) * 13.0F / BottledWispItem.MAX_BRIGHTNESS);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xE8941A;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return brightnessOf(stack) > 0;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.marshlamp.brightness", brightnessOf(stack)).withStyle(ChatFormatting.GOLD));
        tooltip.accept(Component.translatable("tooltip.marshlamp.lantern").withStyle(ChatFormatting.GRAY));
    }
}
