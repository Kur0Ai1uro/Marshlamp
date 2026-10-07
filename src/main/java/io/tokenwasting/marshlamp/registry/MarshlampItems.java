package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.item.BottledWispItem;
import io.tokenwasting.marshlamp.item.MarshAxeItem;
import io.tokenwasting.marshlamp.item.MarshHoeItem;
import io.tokenwasting.marshlamp.item.MarshLanternItem;
import io.tokenwasting.marshlamp.item.MarshPickaxeItem;
import io.tokenwasting.marshlamp.item.MarshSproutSeedItem;
import io.tokenwasting.marshlamp.item.MarshWispFlowerItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MarshlampMod.MOD_ID);

    public static final DeferredItem<BlockItem> MARSH_LAMP = ITEMS.registerSimpleBlockItem(MarshlampBlocks.MARSH_LAMP);
    public static final DeferredItem<BottledWispItem> BOTTLED_WISP = ITEMS.registerItem("bottled_wisp", BottledWispItem::new);
    public static final DeferredItem<Item> MARSH_SHARD = ITEMS.registerSimpleItem("marsh_shard");
    public static final DeferredItem<Item> MARSH_INGOT = ITEMS.registerSimpleItem("marsh_ingot");
    public static final DeferredItem<MarshHoeItem> MARSH_HOE = ITEMS.registerItem("marsh_hoe", MarshHoeItem::new);
    public static final DeferredItem<MarshPickaxeItem> MARSH_PICKAXE = ITEMS.registerItem("marsh_pickaxe", MarshPickaxeItem::new);
    public static final DeferredItem<MarshAxeItem> MARSH_AXE = ITEMS.registerItem("marsh_axe", MarshAxeItem::new);
    public static final DeferredItem<MarshLanternItem> MARSH_LANTERN = ITEMS.registerItem("marsh_lantern", MarshLanternItem::new);
    public static final DeferredItem<MarshWispFlowerItem> MARSH_WISP = ITEMS.registerItem("marsh_wisp",
            properties -> new MarshWispFlowerItem(MarshlampBlocks.MARSH_WISP.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<MarshSproutSeedItem> MARSH_SPROUT_SEEDS = ITEMS.registerItem("marsh_sprout_seeds",
            properties -> new MarshSproutSeedItem(MarshlampBlocks.MARSH_SPROUT.get(), properties.useItemDescriptionPrefix()));
    public static final DeferredItem<Item> MARSH_WHEAT = ITEMS.registerSimpleItem("marsh_wheat");
    public static final DeferredItem<Item> MARSH_BREAD = ITEMS.registerItem("marsh_bread",
            properties -> new Item(properties.food(
                    new FoodProperties(6, 0.8F, false),
                    Consumable.builder().onConsume(new ApplyStatusEffectsConsumeEffect(java.util.List.of(
                            new MobEffectInstance(MobEffects.SPEED, 8 * 20, 0),
                            new MobEffectInstance(MobEffects.INSTANT_HEALTH, 1, 0)
                    ))).build())));

    private MarshlampItems() {
    }
}
