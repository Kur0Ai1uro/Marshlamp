package io.tokenwasting.marshlamp.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MarshHoeItem extends HoeItem {
    public MarshHoeItem(Properties properties) {
        super(ToolMaterial.IRON, -2.0F, -1.0F, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        if (state.getBlock() instanceof CropBlock && Wick.get(stack) >= 5 && io.tokenwasting.marshlamp.block.CropAges.canGrow(state)) {
            if (!level.isClientSide() && io.tokenwasting.marshlamp.block.CropAges.grow(level, pos)) {
                Wick.add(stack, -5);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
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
        tooltip.accept(Component.translatable("tooltip.marshlamp.hoe").withStyle(ChatFormatting.GRAY));
    }
}
