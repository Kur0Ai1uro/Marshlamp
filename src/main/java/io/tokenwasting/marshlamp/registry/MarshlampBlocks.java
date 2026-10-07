package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.block.MarshGlowBlock;
import io.tokenwasting.marshlamp.block.MarshLampBlock;
import io.tokenwasting.marshlamp.block.MarshSproutBlock;
import io.tokenwasting.marshlamp.block.MarshWispBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MarshlampMod.MOD_ID);

    public static final DeferredBlock<MarshLampBlock> MARSH_LAMP = BLOCKS.registerBlock("marsh_lamp", MarshLampBlock::new, props -> props
            .mapColor(MapColor.COLOR_ORANGE)
            .strength(1.5F)
            .sound(SoundType.LANTERN)
            .lightLevel(state -> state.getValue(BlockStateProperties.LEVEL))
            .noOcclusion());

    public static final DeferredBlock<MarshWispBlock> MARSH_WISP = BLOCKS.registerBlock("marsh_wisp", MarshWispBlock::new, props -> props
            .mapColor(MapColor.COLOR_YELLOW)
            .instabreak()
            .noCollision()
            .sound(SoundType.GRASS)
            .lightLevel(state -> state.getValue(MarshWispBlock.BLOOMING) ? 6 : 0)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .noOcclusion());

    public static final DeferredBlock<MarshSproutBlock> MARSH_SPROUT = BLOCKS.registerBlock("marsh_sprout", MarshSproutBlock::new, props -> props
            .mapColor(MapColor.COLOR_GREEN)
            .instabreak()
            .noCollision()
            .sound(SoundType.CROP)
            .noOcclusion());

    public static final DeferredBlock<MarshGlowBlock> MARSH_GLOW = BLOCKS.registerBlock("marsh_glow", MarshGlowBlock::new, props -> props
            .replaceable()
            .noCollision()
            .noOcclusion()
            .noLootTable()
            .instabreak()
            .lightLevel(state -> MarshGlowBlock.LIGHT)
            .sound(SoundType.EMPTY));

    private MarshlampBlocks() {
    }
}
