package io.tokenwasting.marshlamp;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class MarshlampConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue GROWTH_RADIUS = BUILDER
            .comment("沼灯催熟作物时搜索的水平半径。")
            .defineInRange("growthRadius", 3, 1, 8);

    public static final ModConfigSpec.IntValue GROWTH_INTERVAL_SECONDS = BUILDER
            .comment("沼灯两次催熟之间的秒数。")
            .defineInRange("growthIntervalSeconds", 10, 1, 120);

    public static final ModConfigSpec.IntValue BRIGHTNESS_PER_GROWTH = BUILDER
            .comment("每成功催熟一次消耗的亮度。")
            .defineInRange("brightnessPerGrowth", 1, 1, 20);

    public static final ModConfigSpec.IntValue BERRY_RESTORE = BUILDER
            .comment("一颗荧光浆果回复的亮度。")
            .defineInRange("berryRestore", 25, 1, 100);

    public static final ModConfigSpec.IntValue WISP_REGROW_SECONDS = BUILDER
            .comment("摘下沼灯芯花后，花朵还在且区块加载时，重新亮起需要的秒数。")
            .defineInRange("wispRegrowSeconds", 300, 10, 7200);

    public static final ModConfigSpec.IntValue BRIGHTNESS_DRAIN_SECONDS = BUILDER
            .comment("沼灯每掉 1 点亮度的秒数。旁边没有作物时也会变暗。")
            .defineInRange("brightnessDrainSeconds", 10, 1, 600);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private MarshlampConfig() {
    }

    public static int growthRadius() {
        return value(GROWTH_RADIUS, 3);
    }

    public static int brightnessPerGrowth() {
        return value(BRIGHTNESS_PER_GROWTH, 1);
    }

    public static int berryRestore() {
        return value(BERRY_RESTORE, 25);
    }

    public static int wispRegrowTicks() {
        return wispRegrowSeconds() * 20;
    }

    public static int wispRegrowSeconds() {
        return value(WISP_REGROW_SECONDS, 300);
    }

    public static int growthIntervalTicks() {
        return value(GROWTH_INTERVAL_SECONDS, 10) * 20;
    }

    public static int brightnessDrainTicks() {
        return value(BRIGHTNESS_DRAIN_SECONDS, 10) * 20;
    }

    private static int value(ModConfigSpec.IntValue config, int fallback) {
        return SPEC.isLoaded() ? config.get() : fallback;
    }
}
