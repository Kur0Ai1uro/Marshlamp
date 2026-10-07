package io.tokenwasting.marshlamp;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import io.tokenwasting.marshlamp.registry.MarshlampBlockEntities;
import io.tokenwasting.marshlamp.registry.MarshlampBlocks;
import io.tokenwasting.marshlamp.registry.MarshlampCreativeTabs;
import io.tokenwasting.marshlamp.registry.MarshlampCriteria;
import io.tokenwasting.marshlamp.registry.MarshlampDataComponents;
import io.tokenwasting.marshlamp.registry.MarshlampFeatures;
import io.tokenwasting.marshlamp.registry.MarshlampItems;
import io.tokenwasting.marshlamp.registry.MarshlampStructures;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(MarshlampMod.MOD_ID)
public class MarshlampMod {
    public static final String MOD_ID = "marshlamp";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MarshlampMod(IEventBus modEventBus, ModContainer modContainer) {
        MarshlampDataComponents.DATA_COMPONENTS.register(modEventBus);
        MarshlampBlocks.BLOCKS.register(modEventBus);
        MarshlampBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        MarshlampItems.ITEMS.register(modEventBus);
        MarshlampFeatures.FEATURES.register(modEventBus);
        MarshlampStructures.STRUCTURE_TYPES.register(modEventBus);
        MarshlampStructures.STRUCTURE_PIECES.register(modEventBus);
        MarshlampStructures.PROCESSORS.register(modEventBus);
        MarshlampCriteria.TRIGGERS.register(modEventBus);
        MarshlampCreativeTabs.TABS.register(modEventBus);
        modContainer.registerConfig(ModConfig.Type.SERVER, MarshlampConfig.SPEC);
    }
}
