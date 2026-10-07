package io.tokenwasting.marshlamp.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import io.tokenwasting.marshlamp.MarshlampConfig;
import io.tokenwasting.marshlamp.blockentity.MarshLampBlockEntity;
import io.tokenwasting.marshlamp.item.BottledWispItem;
import io.tokenwasting.marshlamp.item.MarshLanternItem;
import io.tokenwasting.marshlamp.item.Wick;
import io.tokenwasting.marshlamp.registry.MarshlampBlockEntities;
import io.tokenwasting.marshlamp.registry.MarshlampCriteria;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class MarshLampBlock extends BaseEntityBlock {
    public static final MapCodec<MarshLampBlock> CODEC = simpleCodec(MarshLampBlock::new);
    public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;

    public MarshLampBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(LEVEL);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarshLampBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, MarshlampBlockEntities.MARSH_LAMP.get(), MarshLampBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof MarshLampBlockEntity lamp)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        if (stack.getItem() instanceof BottledWispItem) {
            if (lamp.getBrightness() >= BottledWispItem.MAX_BRIGHTNESS) {
                if (!level.isClientSide()) {
                    player.displayClientMessage(Component.translatable("message.marshlamp.lamp_full"), true);
                }
                return InteractionResult.SUCCESS;
            }
            if (!level.isClientSide()) {
                lamp.addBrightness(BottledWispItem.brightnessOf(stack));
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.8F, 1.2F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                    give(player, new ItemStack(Items.GLASS_BOTTLE));
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    MarshlampCriteria.LAMP_LIT.get().trigger(serverPlayer);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.is(Items.GLOW_BERRIES) && lamp.getBrightness() < BottledWispItem.MAX_BRIGHTNESS) {
            if (!level.isClientSide()) {
                lamp.addBrightness(MarshlampConfig.berryRestore());
                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.7F, 1.4F);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof MarshLanternItem && MarshLanternItem.brightnessOf(stack) < BottledWispItem.MAX_BRIGHTNESS && lamp.getBrightness() > 0) {
            if (!level.isClientSide()) {
                int transfer = Math.min(BottledWispItem.MAX_BRIGHTNESS - MarshLanternItem.brightnessOf(stack), lamp.getBrightness());
                if (transfer > 0) {
                    lamp.addBrightness(-transfer);
                    MarshLanternItem.setBrightness(stack, MarshLanternItem.brightnessOf(stack) + transfer);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.7F, 1.35F);
                    if (player instanceof ServerPlayer serverPlayer) {
                        MarshlampCriteria.LANTERN_CHARGED.get().trigger(serverPlayer);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (Wick.get(stack) < Wick.MAX && lamp.getBrightness() > 0 && isMarshTool(stack)) {
            if (!level.isClientSide()) {
                int transfer = Math.min(Wick.MAX - Wick.get(stack), lamp.getBrightness());
                if (transfer > 0) {
                    lamp.addBrightness(-transfer);
                    Wick.add(stack, transfer);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.7F, 1.35F);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    private static boolean isMarshTool(ItemStack stack) {
        return stack.is(io.tokenwasting.marshlamp.registry.MarshlampItems.MARSH_HOE.get())
                || stack.is(io.tokenwasting.marshlamp.registry.MarshlampItems.MARSH_PICKAXE.get())
                || stack.is(io.tokenwasting.marshlamp.registry.MarshlampItems.MARSH_AXE.get());
    }

    private static void give(Player player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
