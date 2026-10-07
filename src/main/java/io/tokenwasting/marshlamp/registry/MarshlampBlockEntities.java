package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.blockentity.MarshLampBlockEntity;
import io.tokenwasting.marshlamp.blockentity.MarshWispBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MarshlampMod.MOD_ID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MarshLampBlockEntity>> MARSH_LAMP = BLOCK_ENTITIES.register("marsh_lamp",
            () -> new BlockEntityType<>(MarshLampBlockEntity::new, MarshlampBlocks.MARSH_LAMP.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MarshWispBlockEntity>> MARSH_WISP = BLOCK_ENTITIES.register("marsh_wisp",
            () -> new BlockEntityType<>(MarshWispBlockEntity::new, MarshlampBlocks.MARSH_WISP.get()));

    private MarshlampBlockEntities() {
    }
}
