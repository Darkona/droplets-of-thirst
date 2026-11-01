package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.thirst.DehydrationEffect;
import com.darkona.dropletsofthirst.content.thirst.QuenchnessEffect;
import com.darkona.dropletsofthirst.content.thirst.SimpleEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class DropletsMobEffects {
    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, DropletsOfThirst.ID);
    public static final RegistryObject<MobEffect> QUENCHNESS = MOB_EFFECTS.register("quenchness", () -> new QuenchnessEffect(MobEffectCategory.BENEFICIAL, 0x3FB8E6));
    public static final RegistryObject<MobEffect> DEHYDRATION = MOB_EFFECTS.register("dehydration", () -> new DehydrationEffect(MobEffectCategory.HARMFUL, 0xA0621F));
    public static final RegistryObject<MobEffect> HYDRATED = MOB_EFFECTS.register("hydrated", () -> new SimpleEffect(MobEffectCategory.BENEFICIAL, 0x7FE0C0));
    public static final RegistryObject<MobEffect> OVERHYDRATED = MOB_EFFECTS.register("overhydrated", () -> new SimpleEffect(MobEffectCategory.HARMFUL, 0x9DB0C0)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, "5E0C8F2A-6B1D-4C3E-9A7F-2D4B8C1E6F30", -0.1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
