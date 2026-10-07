package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.item.BottledWispItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MarshlampMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MARSHLAMP = TABS.register("marshlamp", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.marshlamp"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> MarshlampItems.MARSH_LAMP.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(MarshlampItems.MARSH_LAMP.get());
                output.accept(MarshlampItems.MARSH_LANTERN.get());
                output.accept(BottledWispItem.withBrightness(80));
                output.accept(MarshlampItems.MARSH_SHARD.get());
                output.accept(MarshlampItems.MARSH_INGOT.get());
                output.accept(MarshlampItems.MARSH_HOE.get());
                output.accept(MarshlampItems.MARSH_PICKAXE.get());
                output.accept(MarshlampItems.MARSH_AXE.get());
                output.accept(MarshlampItems.MARSH_WISP.get());
                output.accept(MarshlampItems.MARSH_SPROUT_SEEDS.get());
                output.accept(MarshlampItems.MARSH_WHEAT.get());
                output.accept(MarshlampItems.MARSH_BREAD.get());
            })
            .build());

    private MarshlampCreativeTabs() {
    }
}
