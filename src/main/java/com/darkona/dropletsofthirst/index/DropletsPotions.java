package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DropletsPotions {

    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(BuiltInRegistries.POTION, DropletsOfThirst.ID);
    public static final DeferredHolder<Potion, Potion> QUENCHNESS_POTION = POTIONS.register("quenchness", () -> new Potion("quenchness", new MobEffectInstance(DropletsMobEffects.QUENCHNESS, 900)));
    public static final DeferredHolder<Potion, Potion> LONG_QUENCHNESS_POTION = POTIONS.register("long_quenchness", () -> new Potion("quenchness", new MobEffectInstance(DropletsMobEffects.QUENCHNESS, 1800)));
    public static final DeferredHolder<Potion, Potion> STRONG_QUENCHNESS_POTION = POTIONS.register("strong_quenchness", () -> new Potion("quenchness", new MobEffectInstance(DropletsMobEffects.QUENCHNESS, 450, 1)));

    public static void register(IEventBus modBus) {
        POTIONS.register(modBus);
        NeoForge.EVENT_BUS.addListener(DropletsPotions::registerBrewing);
    }

    private static void registerBrewing(RegisterBrewingRecipesEvent event) {
        if (!GameplayConfig.QUENCHNESS_POTION.get())
            return;
        PotionBrewing.Builder builder = event.getBuilder();
        builder.addMix(Potions.AWKWARD, Items.PRISMARINE_CRYSTALS, QUENCHNESS_POTION);
        builder.addMix(QUENCHNESS_POTION, Items.REDSTONE, LONG_QUENCHNESS_POTION);
        builder.addMix(QUENCHNESS_POTION, Items.GLOWSTONE_DUST, STRONG_QUENCHNESS_POTION);
    }
}
