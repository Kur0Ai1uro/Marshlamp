package io.tokenwasting.marshlamp.blockentity;

import io.tokenwasting.marshlamp.MarshlampConfig;
import io.tokenwasting.marshlamp.block.MarshLampBlock;
import io.tokenwasting.marshlamp.item.BottledWispItem;
import io.tokenwasting.marshlamp.registry.MarshlampBlockEntities;
import io.tokenwasting.marshlamp.registry.MarshlampCriteria;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MarshLampBlockEntity extends BlockEntity {
    private boolean hasWisp;
    private int brightness;
    private int ticks;
    private int drainTicks;

    public MarshLampBlockEntity(BlockPos pos, BlockState state) {
        super(MarshlampBlockEntities.MARSH_LAMP.get(), pos, state);
    }

    public static int lightFor(int brightness, boolean hasWisp) {
        if (!hasWisp || brightness <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(brightness * 15.0 / BottledWispItem.MAX_BRIGHTNESS));
    }

    public boolean hasWisp() {
        return this.hasWisp;
    }

    public int getBrightness() {
        return this.brightness;
    }

    public void insertWisp(int brightness) {
        this.hasWisp = true;
        this.brightness = Mth.clamp(brightness, 0, BottledWispItem.MAX_BRIGHTNESS);
        this.updateLight();
        this.markUpdated();
    }

    public int extractWisp() {
        int stored = this.brightness;
        this.hasWisp = false;
        this.brightness = 0;
        this.updateLight();
        this.markUpdated();
        return stored;
    }

    public void addBrightness(int delta) {
        if (!this.hasWisp && delta > 0) {
            this.hasWisp = true;
        }
        this.brightness = Mth.clamp(this.brightness + delta, 0, BottledWispItem.MAX_BRIGHTNESS);
        this.hasWisp = this.brightness > 0;
        this.updateLight();
        this.markUpdated();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MarshLampBlockEntity lamp) {
        if (!(level instanceof ServerLevel server) || !lamp.hasWisp) {
            return;
        }
        if (lamp.brightness > 0 && server.getGameTime() % 40L == 0L) {
            server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 2, 0.15, 0.15, 0.15, 0.01);
        }
        lamp.drainTicks++;
        if (lamp.drainTicks >= MarshlampConfig.brightnessDrainTicks()) {
            lamp.drainTicks = 0;
            lamp.brightness = Math.max(0, lamp.brightness - 1);
            lamp.hasWisp = lamp.brightness > 0;
            lamp.updateLight();
            lamp.markUpdated();
            if (lamp.brightness <= 0) {
                return;
            }
        }
        lamp.ticks++;
        if (lamp.ticks < MarshlampConfig.growthIntervalTicks() || lamp.brightness <= 0) {
            return;
        }
        lamp.ticks = 0;
        BlockPos grown = lamp.tryGrow(server, pos);
        if (grown == null) {
            return;
        }
        lamp.brightness = Math.max(0, lamp.brightness - MarshlampConfig.brightnessPerGrowth());
        lamp.hasWisp = lamp.brightness > 0;
        lamp.updateLight();
        lamp.markUpdated();
        server.sendParticles(ParticleTypes.HAPPY_VILLAGER, grown.getX() + 0.5, grown.getY() + 0.4, grown.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.0);
        server.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 8, 0.2, 0.2, 0.2, 0.01);
        for (ServerPlayer player : server.players()) {
            if (player.blockPosition().distSqr(pos) <= 256) {
                MarshlampCriteria.LAMP_GREW_CROP.get().trigger(player);
            }
        }
    }

    private BlockPos tryGrow(ServerLevel level, BlockPos origin) {
        int radius = MarshlampConfig.growthRadius();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos sprout = findSprout(level, origin, radius);
        if (sprout != null) {
            return sprout;
        }
        for (int attempt = 0; attempt < 12; attempt++) {
            int dx = level.random.nextInt(radius * 2 + 1) - radius;
            int dy = level.random.nextInt(3) - 1;
            int dz = level.random.nextInt(radius * 2 + 1) - radius;
            cursor.setWithOffset(origin, dx, dy, dz);
            if (io.tokenwasting.marshlamp.block.CropAges.grow(level, cursor)) {
                return cursor.immutable();
            }
        }
        return null;
    }

    private BlockPos findSprout(ServerLevel level, BlockPos origin, int radius) {
        java.util.ArrayList<BlockPos> sprouts = new java.util.ArrayList<>();
        for (BlockPos scan : BlockPos.betweenClosed(origin.offset(-radius, -1, -radius), origin.offset(radius, 1, radius))) {
            BlockState state = level.getBlockState(scan);
            if (state.getBlock() instanceof io.tokenwasting.marshlamp.block.MarshSproutBlock
                    && io.tokenwasting.marshlamp.block.CropAges.canGrow(state)) {
                sprouts.add(scan.immutable());
            }
        }
        if (sprouts.isEmpty()) {
            return null;
        }
        BlockPos chosen = sprouts.get(level.random.nextInt(sprouts.size()));
        return io.tokenwasting.marshlamp.block.CropAges.grow(level, chosen) ? chosen : null;
    }

    private void updateLight() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        BlockState state = this.getBlockState();
        if (!state.hasProperty(MarshLampBlock.LEVEL)) {
            return;
        }
        int light = lightFor(this.brightness, this.hasWisp);
        if (state.getValue(MarshLampBlock.LEVEL) != light) {
            this.level.setBlock(this.worldPosition, state.setValue(MarshLampBlock.LEVEL, light), 3);
        }
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level instanceof ServerLevel server) {
            BlockState state = this.getBlockState();
            server.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("HasWisp", this.hasWisp);
        output.putInt("Brightness", this.brightness);
        output.putInt("Ticks", this.ticks);
        output.putInt("DrainTicks", this.drainTicks);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.hasWisp = input.getBooleanOr("HasWisp", false);
        this.brightness = input.getIntOr("Brightness", 0);
        this.ticks = input.getIntOr("Ticks", 0);
        this.drainTicks = input.getIntOr("DrainTicks", 0);
    }

    @Override
    public CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
