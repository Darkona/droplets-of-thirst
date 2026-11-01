package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.api.event.PurityEffectEvent;
import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.content.thirst.ExhaustionFactors;
import com.darkona.dropletsofthirst.core.Events;
import com.darkona.dropletsofthirst.index.DropletsMobEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public final class PurityEffects
{
    private PurityEffects() {}

    public record Effect(MobEffect effect, int duration, int amplifier, float chance, boolean blocksHydration) {}

    private record EffectTables(List<List<? extends String>> source, Effect[][] byPurity) {}

    private static volatile EffectTables tables = new EffectTables(List.of(), new Effect[0][]);

    public static boolean give(Player player, int purity)
    {
        if (!WaterPurity.enabled() || purity < WaterPurity.MIN_PURITY || purity > WaterPurity.MAX_PURITY)
            return true;
        boolean hydrate = true;
        List<MobEffectInstance> effects = new ArrayList<>();
        float roll = player.getRandom().nextFloat();
        for (Effect effect : table(purity))
        {
            if (roll >= effect.chance())
                continue;
            effects.add(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
            hydrate &= !effect.blocksHydration();
        }
        if (PurityConfig.HOT_DIRTY_WATER.get() && purity <= PurityConfig.HOT_DIRTY_WATER_MAX_PURITY.get() && player instanceof ServerPlayer && ExhaustionFactors.isHotClimate(player))
            effects.add(new MobEffectInstance(DropletsMobEffects.DEHYDRATION.get(), PurityConfig.HOT_DIRTY_WATER_DURATION.get(), PurityConfig.HOT_DIRTY_WATER_AMPLIFIER.get()));
        PurityEffectEvent event = Events.post(new PurityEffectEvent(player, purity, effects, hydrate || PurityConfig.QUENCH_WHEN_DEBUFFED.get()));
        if (event.isCanceled())
            return true;
        if (player instanceof ServerPlayer)
            for (MobEffectInstance effect : event.getEffects())
                player.addEffect(effect);
        return event.hydrates();
    }

    public static void cure(LivingEntity entity)
    {
        for (int purity = WaterPurity.MIN_PURITY; purity <= WaterPurity.MAX_PURITY; purity++)
            for (Effect effect : table(purity))
                if (effect.effect().getCategory() == MobEffectCategory.HARMFUL)
                    entity.removeEffect(effect.effect());
        entity.removeEffect(DropletsMobEffects.DEHYDRATION.get());
    }

    public static Effect[] table(int purity)
    {
        EffectTables cached = tables;
        List<List<? extends String>> source = cached.source();
        boolean stale = source.size() != PurityConfig.EFFECTS.size();
        for (int i = 0; !stale && i < source.size(); i++)
            stale = source.get(i) != PurityConfig.EFFECTS.get(i).get();
        if (stale)
        {
            List<List<? extends String>> lists = new ArrayList<>();
            Effect[][] byPurity = new Effect[PurityConfig.EFFECTS.size()][];
            for (int i = 0; i < byPurity.length; i++)
            {
                List<? extends String> list = PurityConfig.EFFECTS.get(i).get();
                lists.add(list);
                byPurity[i] = list.stream().map(PurityEffects::parseEffect).filter(Objects::nonNull).toArray(Effect[]::new);
            }
            tables = cached = new EffectTables(lists, byPurity);
        }
        return cached.byPurity()[purity];
    }

    private static @Nullable Effect parseEffect(String entry)
    {
        if (!PurityConfig.isValidEffect(entry))
            return null;
        String[] parts = entry.split(",");
        ResourceLocation id = ResourceLocation.tryParse(parts[0].trim());
        MobEffect effect = id == null ? null : BuiltInRegistries.MOB_EFFECT.getOptional(id).orElse(null);
        if (effect == null)
            return null;
        return new Effect(effect, Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()),
                (float) (Double.parseDouble(parts[3].trim()) / 100.0), parts.length == 5 && parts[4].trim().equals("true"));
    }
}
