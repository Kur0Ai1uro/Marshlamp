package io.tokenwasting.marshlamp.item;

import java.util.function.Consumer;

import io.tokenwasting.marshlamp.MarshlampConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

public class MarshWispFlowerItem extends BlockItem {
    public MarshWispFlowerItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.marshlamp.wisp_flower", MarshlampConfig.wispRegrowSeconds()).withStyle(ChatFormatting.GRAY));
    }
}
