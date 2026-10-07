package io.tokenwasting.marshlamp.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import io.tokenwasting.marshlamp.blockentity.MarshWispBlockEntity;
import io.tokenwasting.marshlamp.item.BottledWispItem;
import io.tokenwasting.marshlamp.registry.MarshlampBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MarshWispBlock extends BaseEntityBlock {
    public static final MapCodec<MarshWispBlock> CODEC = simpleCodec(MarshWispBlock::new);
    public static final BooleanProperty BLOOMING = BooleanProperty.create("blooming");
    private static final VoxelShape SHAPE = box(2.0, 0.0, 2.0, 14.0, 13.0, 14.0);

    public MarshWispBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(BLOOMING, true));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(BLOOMING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Vec3 offset = state.getOffset(pos);
        return SHAPE.move(offset.x, offset.y, offset.z);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarshWispBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, MarshlampBlockEntities.MARSH_WISP.get(), MarshWispBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.GLASS_BOTTLE)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return harvest(level, pos, state, player, stack);
    }

    private InteractionResult harvest(Level level, BlockPos pos, BlockState state, Player player, ItemStack bottle) {
        if (!state.getValue(BLOOMING)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, state.setValue(BLOOMING, false), 3);
            if (level.getBlockEntity(pos) instanceof MarshWispBlockEntity bloom) {
                bloom.beginRegrow();
            }
            if (!player.getAbilities().instabuild) {
                bottle.shrink(1);
            }
            ItemStack drop = BottledWispItem.withBrightness(BottledWispItem.HARVEST_BRIGHTNESS);
            if (!player.getInventory().add(drop)) {
                player.drop(drop, false);
            }
            if (level.random.nextFloat() < 0.3F) {
                ItemStack seeds = new ItemStack(io.tokenwasting.marshlamp.registry.MarshlampItems.MARSH_SPROUT_SEEDS.get());
                if (!player.getInventory().add(seeds)) {
                    player.drop(seeds, false);
                }
            }
            level.playSound(null, pos, SoundEvents.CAVE_VINES_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.2F);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.02);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
