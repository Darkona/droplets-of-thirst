package com.darkona.dropletsofthirst.foundation.gui;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.thirst.EffectInit;
import com.darkona.dropletsofthirst.foundation.config.ClientConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import dev.darkona.jdlib.api.hud.HudStyles;
import dev.darkona.jdlib.core.registry.PriorityList;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

public final class ThirstBarStyles
{
    public static final int VAMPIRE_PRIORITY = 400;
    public static final int DEHYDRATION_PRIORITY = 300;
    public static final int OVERHYDRATED_PRIORITY = 250;
    public static final int POISON_PRIORITY = 200;
    public static final int QUENCHNESS_PRIORITY = 100;
    public static final int HYDRATED_PRIORITY = 50;
    public static final IntSupplier VAMPIRE_COLOR = color(ClientConfig.VAMPIRE_COLOR);

    private static final HudStyles<Player, IntSupplier, Void> STYLES = HudStyles.plain(PriorityList.Ties.BY_ID);
    private static final List<Holder<MobEffect>> waveEffects = new CopyOnWriteArrayList<>();

    private ThirstBarStyles() {}

    public static synchronized void register(ResourceLocation id, Predicate<Player> active, IntSupplier rgb, int priority)
    {
        STYLES.define(id, rgb);
        STYLES.addRule(id, priority, player -> active.test(player) ? id : null);
    }

    public static int resolve(Player player)
    {
        ResourceLocation id = STYLES.select(player);
        IntSupplier rgb = id == null ? null : STYLES.style(id);
        return rgb == null ? -1 : rgb.getAsInt();
    }

    public static @Nullable ResourceLocation activeStyle(Player player)
    {
        return STYLES.select(player);
    }

    public static void registerWave(Holder<MobEffect> effect)
    {
        waveEffects.add(effect);
    }

    public static boolean waves(Player player)
    {
        for (int i = 0; i < waveEffects.size(); i++)
            if (player.hasEffect(waveEffects.get(i)))
                return true;
        return false;
    }

    public static void registerBuiltIns()
    {
        registerWave(EffectInit.QUENCHNESS);
        registerWave(EffectInit.HYDRATED);
        register(DropletsOfThirst.asResource("dehydration"), player -> player.hasEffect(EffectInit.DEHYDRATION), color(ClientConfig.DEHYDRATION_COLOR), DEHYDRATION_PRIORITY);
        register(DropletsOfThirst.asResource("overhydrated"), player -> player.hasEffect(EffectInit.OVERHYDRATED), color(ClientConfig.OVERHYDRATED_COLOR), OVERHYDRATED_PRIORITY);
        register(ResourceLocation.withDefaultNamespace("poison"), player -> player.hasEffect(MobEffects.POISON), color(ClientConfig.POISON_COLOR), POISON_PRIORITY);
        register(DropletsOfThirst.asResource("quenchness"), player -> player.hasEffect(EffectInit.QUENCHNESS), color(ClientConfig.QUENCHNESS_COLOR), QUENCHNESS_PRIORITY);
        register(DropletsOfThirst.asResource("hydrated"), player -> player.hasEffect(EffectInit.HYDRATED), color(ClientConfig.HYDRATED_COLOR), HYDRATED_PRIORITY);
    }

    private static IntSupplier color(ModConfigSpec.ConfigValue<String> value)
    {
        return new IntSupplier()
        {
            private String parsed;
            private int rgb;

            @Override
            public int getAsInt()
            {
                String hex = value.get();
                if (hex != parsed)
                {
                    rgb = Integer.parseInt(hex, 1, 7, 16);
                    parsed = hex;
                }
                return rgb;
            }
        };
    }
}
