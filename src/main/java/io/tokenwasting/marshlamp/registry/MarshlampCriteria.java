package io.tokenwasting.marshlamp.registry;

import io.tokenwasting.marshlamp.MarshlampMod;
import io.tokenwasting.marshlamp.advancement.LampActionTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class MarshlampCriteria {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, MarshlampMod.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, LampActionTrigger> LAMP_LIT = TRIGGERS.register("lamp_lit", LampActionTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, LampActionTrigger> LAMP_GREW_CROP = TRIGGERS.register("lamp_grew_crop", LampActionTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, LampActionTrigger> LANTERN_CHARGED = TRIGGERS.register("lantern_charged", LampActionTrigger::new);

    private MarshlampCriteria() {
    }
}
