package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.index.DropletsMobEffects;
import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.compat.sereneseasons.SereneSeasonsCompat;
import com.darkona.dropletsofthirst.config.CompatConfig;
import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import com.darkona.dropletsofthirst.content.purity.DimensionWater;
import com.darkona.dropletsofthirst.mixin.BiomeAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.biome.Biome;
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
    private static final NumberRows TEMPERATURE_CURVE = new NumberRows(2);
    private static final NumberRows HUMIDITY_CURVE = new NumberRows(2);
    private static final NumberRows BODY_TEMPERATURE_CURVE = new NumberRows(2);
    private static @Nullable RegistryAccess fireProtectionAccess;
    private static @Nullable Holder<Enchantment> fireProtectionHolder;
    private static final PriorityList<ResourceLocation, ExhaustionModifier> MODIFIERS = new PriorityList<>(PriorityList.Order.LOWEST_FIRST, PriorityList.Ties.BY_ID);

    private ExhaustionFactors() {}

    public static void register(ResourceLocation id, ExhaustionModifier modifier)
    {
        MODIFIERS.put(id, 0, modifier);
    }

    public static float compute(Player player, @Nullable float[] breakdown)
    {
        Level level = player.level();
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        boolean dayCycle = !level.dimensionType().hasFixedTime();

        float climate = getExhaustionBiomeModifier(player);
        float fireProtection = getExhaustionFireProtModifier(player);
        float fireResistance = getExhaustionFireResistanceModifier(player);
        float weather = !level.isRainingAt(eyes) ? 1.0F
                : (level.isThundering() ? GameplayConfig.THUNDER_MULTIPLIER : GameplayConfig.RAIN_MULTIPLIER).get().floatValue();
        float time = !dayCycle ? 1.0F : (level.isDay() ? GameplayConfig.DAY_MULTIPLIER : GameplayConfig.NIGHT_MULTIPLIER).get().floatValue();
        float sun = dayCycle && level.isDay() && !level.isRaining() && level.canSeeSky(eyes) ? GameplayConfig.SUN_MULTIPLIER.get().floatValue() : 1.0F;
        float altitude = (float) NumberRows.band(ALTITUDE.get(GameplayConfig.ALTITUDE_MULTIPLIERS.get()), player.getBlockY() - level.getSeaLevel(), 1.0);
        float water = player.isUnderWater() ? GameplayConfig.UNDERWATER_MULTIPLIER.get().floatValue() * underwaterBreathing(player)
                : player.isInWater() ? GameplayConfig.IN_WATER_MULTIPLIER.get().floatValue() : 1.0F;
        MobEffectInstance hydratedEffect = player.getEffect(DropletsMobEffects.HYDRATED);
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

    private static float getExhaustionFireProtModifier(Player player)
    {
        Holder<Enchantment> fireProtection = fireProtection(player.level());
        if(fireProtection == null)
            return 1.0f;
        int levels = 0;
        for(ItemStack armor : player.getArmorSlots())
            levels += armor.getEnchantmentLevel(fireProtection);
        return Math.max(0.0f, 1.0f - Math.min(levels, GameplayConfig.FIRE_PROTECTION_MAX_LEVELS.get()) * GameplayConfig.FIRE_PROTECTION_PER_LEVEL.get().floatValue());
    }

    private static @Nullable Holder<Enchantment> fireProtection(Level level)
    {
        RegistryAccess access = level.registryAccess();
        if(access != fireProtectionAccess)
        {
            fireProtectionHolder = access.registryOrThrow(Registries.ENCHANTMENT).getHolder(Enchantments.FIRE_PROTECTION).orElse(null);
            fireProtectionAccess = access;
        }
        return fireProtectionHolder;
    }

    private static float getExhaustionFireResistanceModifier(Player player){
        if(player.hasEffect(MobEffects.FIRE_RESISTANCE)){
            return (float) GameplayConfig.FIRE_RESISTANCE_PERCENT.get() /100;
        }else return 1.0f;
    }

    public static float getExhaustionBiomeModifier(Player player)
    {
        Level level = player.level();
        DimensionWater dimension = level.dimensionTypeRegistration().getData(DropletsDataMaps.DIMENSION_WATER);
        if (dimension != null && dimension.thirstMultiplier().isPresent())
            return dimension.thirstMultiplier().get();
        if (level.dimensionType().ultraWarm())
            return GameplayConfig.NETHER_MULTIPLIER.get().floatValue();

        float multiplier = GameplayConfig.DEPLETION_MULTIPLIER.get().floatValue();
        if (ColdSweatCompat.LOADED && CompatConfig.COLD_SWEAT_BODY_TEMPERATURE.get())
            return multiplier * (float) NumberRows.curve(BODY_TEMPERATURE_CURVE.get(CompatConfig.COLD_SWEAT_BODY_TEMPERATURE_CURVE.get()), ColdSweatCompat.bodyTemperature(player));

        BlockPos pos = player.getOnPos();
        return biomeClimate(level, level.getBiome(pos), pos, multiplier, SereneSeasonsCompat.ACTIVE);
    }

    public static float biomeClimate(Level level, Holder<Biome> biomeHolder, BlockPos pos, float multiplier, boolean seasons)
    {
        Biome biome = biomeHolder.value();
        float base = ((BiomeAccessor) (Object) biome).droplets_of_thirst$getTemperature(pos);
        float temperature = seasons ? SereneSeasonsCompat.temperature(level, biomeHolder, pos, base) : base;
        float season = seasons ? SereneSeasonsCompat.seasonMultiplier(level, biomeHolder) : 1.0F;
        float downfall = biome.getModifiedClimateSettings().downfall();
        return season * multiplier * (float) (NumberRows.curve(TEMPERATURE_CURVE.get(GameplayConfig.TEMPERATURE_CURVE.get()), temperature)
                * NumberRows.curve(HUMIDITY_CURVE.get(GameplayConfig.HUMIDITY_CURVE.get()), downfall));
    }

    public static boolean isHotClimate(Player player)
    {
        Level level = player.level();
        if (level.dimensionType().ultraWarm())
            return true;
        if (level.getBiome(player.getOnPos()).value().getBaseTemperature() >= PurityConfig.HOT_DIRTY_WATER_MIN_BIOME_TEMPERATURE.get())
            return true;
        return ColdSweatCompat.LOADED && PurityConfig.HOT_DIRTY_WATER_COLD_SWEAT.get()
                && ColdSweatCompat.bodyTemperature(player) > PurityConfig.HOT_DIRTY_WATER_COLD_SWEAT_MIN_BODY_TEMP.get();
    }
}
