package io.tokenwasting.marshlamp.blockentity;

import io.tokenwasting.marshlamp.MarshlampConfig;
import io.tokenwasting.marshlamp.block.MarshWispBlock;
import io.tokenwasting.marshlamp.registry.MarshlampBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MarshWispBlockEntity extends BlockEntity {
    private int ticks;

    public MarshWispBlockEntity(BlockPos pos, BlockState state) {
        super(MarshlampBlockEntities.MARSH_WISP.get(), pos, state);
    }

    public void beginRegrow() {
        this.ticks = 0;
        this.setChanged();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MarshWispBlockEntity bloom) {
        if (state.getValue(MarshWispBlock.BLOOMING) || !(level instanceof ServerLevel server)) {
            return;
        }
        bloom.ticks++;
        if (bloom.ticks < MarshlampConfig.wispRegrowTicks()) {
            return;
        }
        bloom.ticks = 0;
        bloom.setChanged();
        server.setBlock(pos, state.setValue(MarshWispBlock.BLOOMING, true), 3);
        server.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.6F, 1.4F);
        server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 6, 0.15, 0.2, 0.15, 0.01);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("RegrowTicks", this.ticks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.ticks = input.getIntOr("RegrowTicks", 0);
    }
}
