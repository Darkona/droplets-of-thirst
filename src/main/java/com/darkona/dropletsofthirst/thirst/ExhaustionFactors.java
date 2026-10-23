package com.darkona.dropletsofthirst.thirst;

import com.darkona.dropletsofthirst.api.ExhaustionModifier;
import dev.darkona.jdlib.core.config.NumberRows;
import dev.darkona.jdlib.core.registry.PriorityList;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ExhaustionFactors
{
    public static final String[] FACTORS = {"climate", "fire protection", "fire resistance", "rain/thunder", "day/night", "sun", "altitude", "water", "hydrated", "other mods"};

    private static final NumberRows ALTITUDE = new NumberRows(3);
    private static final PriorityList<ResourceLocation, ExhaustionModifier> MODIFIERS = new PriorityList<>(PriorityList.Order.LOWEST_FIRST, PriorityList.Ties.BY_ID);

    public static void register(ResourceLocation id, ExhaustionModifier modifier)
    {
        MODIFIERS.put(id, 0, modifier);
    }

    private ExhaustionFactors() {}

    public static float compute(Player player, @Nullable float[] breakdown)
    {
        Level level = player.level();
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        boolean dayCycle = !level.dimensionType().hasFixedTime();

        float climate = ThirstHelper.getExhaustionBiomeModifier(player);
        float fireProtection = ThirstHelper.getExhaustionFireProtModifier(player);
        float fireResistance = ThirstHelper.getExhaustionFireResistanceModifier(player);
        float weather = !level.isRainingAt(eyes) ? 1.0F
                : (level.isThundering() ? GameplayConfig.THUNDER_MULTIPLIER : GameplayConfig.RAIN_MULTIPLIER).get().floatValue();
        float time = !dayCycle ? 1.0F : (level.isDay() ? GameplayConfig.DAY_MULTIPLIER : GameplayConfig.NIGHT_MULTIPLIER).get().floatValue();
        float sun = dayCycle && level.isDay() && !level.isRaining() && level.canSeeSky(eyes) ? GameplayConfig.SUN_MULTIPLIER.get().floatValue() : 1.0F;
        float altitude = (float) NumberRows.band(ALTITUDE.get(GameplayConfig.ALTITUDE_MULTIPLIERS.get()), player.getBlockY() - level.getSeaLevel(), 1.0);
        float water = player.isUnderWater() ? GameplayConfig.UNDERWATER_MULTIPLIER.get().floatValue() * underwaterBreathing(player)
                : player.isInWater() ? GameplayConfig.IN_WATER_MULTIPLIER.get().floatValue() : 1.0F;
        MobEffectInstance hydratedEffect = player.getEffect(EffectInit.HYDRATED);
        float hydrated = hydratedEffect == null ? 1.0F : (float) Math.pow(GameplayConfig.HYDRATED_MULTIPLIER.get(), hydratedEffect.getAmplifier() + 1);

        if (breakdown != null)
        {
            breakdown[0] = climate;
            breakdown[1] = fireProtection;
            breakdown[2] = fireResistance;
            breakdown[3] = weather;
            breakdown[4] = time;
            breakdown[5] = sun;
            breakdown[6] = altitude;
            breakdown[7] = water;
            breakdown[8] = hydrated;
        }
        float own = climate * fireProtection * fireResistance * weather * time * sun * altitude * water * hydrated;
        float result = own;
        PriorityList.Snapshot<ResourceLocation, ExhaustionModifier> modifiers = MODIFIERS.snapshot();
        for (int i = 0; i < modifiers.size(); i++)
            result = Math.max(0.0F, modifiers.value(i).apply(player, result));
        if (breakdown != null)
            breakdown[9] = own == 0.0F ? 1.0F : result / own;
        return result;
    }

    private static float underwaterBreathing(Player player)
    {
        return GameplayConfig.WATER_BREATHING_REDUCES_THIRST.get() && (player.hasEffect(MobEffects.WATER_BREATHING) || player.hasEffect(MobEffects.CONDUIT_POWER))
                ? GameplayConfig.UNDERWATER_BREATHING_MULTIPLIER.get().floatValue() : 1.0F;
    }
}
