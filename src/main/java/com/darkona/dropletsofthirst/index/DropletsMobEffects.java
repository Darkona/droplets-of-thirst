package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.content.thirst.QuenchnessEffect;
import com.darkona.dropletsofthirst.content.thirst.DehydrationEffect;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DropletsMobEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, DropletsOfThirst.ID);
    public static final DeferredHolder<MobEffect, MobEffect> QUENCHNESS = MOB_EFFECTS.register("quenchness", () -> new QuenchnessEffect(MobEffectCategory.BENEFICIAL, 0x3FB8E6));
    public static final DeferredHolder<MobEffect, MobEffect> DEHYDRATION = MOB_EFFECTS.register("dehydration", () -> new DehydrationEffect(MobEffectCategory.HARMFUL, 0xA0621F));
    public static final DeferredHolder<MobEffect, MobEffect> HYDRATED = MOB_EFFECTS.register("hydrated", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x7FE0C0) {});
    public static final DeferredHolder<MobEffect, MobEffect> OVERHYDRATED = MOB_EFFECTS.register("overhydrated", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x9DB0C0) {}
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, DropletsOfThirst.asResource("effect.overhydrated"), -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }

}
