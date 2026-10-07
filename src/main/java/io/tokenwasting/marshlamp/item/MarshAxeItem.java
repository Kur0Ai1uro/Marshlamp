package io.tokenwasting.marshlamp.item;

import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class MarshAxeItem extends AxeItem {
    public MarshAxeItem(Properties properties) {
        super(ToolMaterial.IRON, 6.0F, -3.1F, properties);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return Wick.boostSpeed(stack, super.getDestroySpeed(stack, state));
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        Wick.consumeIfMined(stack, level, state, pos);
        return super.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return Wick.get(stack) > 0;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return Wick.get(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Wick.barWidth(stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0xF0A202;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        Wick.addTooltip(stack, tooltip);
    }
}
