package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.worldgen.MarshShrineFeature;
import io.tokenwasting.marshlamp.worldgen.MarshWispFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, MarshlampMod.MOD_ID);

    public static final DeferredHolder<Feature<?>, MarshShrineFeature> MARSH_SHRINE = FEATURES.register("marsh_shrine",
            () -> new MarshShrineFeature(NoneFeatureConfiguration.CODEC));

    public static final DeferredHolder<Feature<?>, MarshWispFeature> MARSH_WISP = FEATURES.register("marsh_wisp",
            () -> new MarshWispFeature(NoneFeatureConfiguration.CODEC));

    private MarshlampFeatures() {
    }
}
