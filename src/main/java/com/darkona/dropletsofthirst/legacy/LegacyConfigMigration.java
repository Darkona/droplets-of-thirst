package com.darkona.dropletsofthirst.legacy;

import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import dev.darkona.jdlib.api.config.ConfigMigration;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public final class LegacyConfigMigration
{
    private static final String NAME = "Droplets of Thirst";

    private static final String[][] MOVES = {
            {"common", "General.thirstDepletionModifier", "gameplay", "depletion.multiplier"},
            {"common", "General.thirstDepletionInPeace", "gameplay", "depletion.inPeaceful"},
            {"common", "General.netherThirstDeletionModifier", "gameplay", "depletion.netherMultiplier"},
            {"common", "General.fireResistanceDehydration", "gameplay", "depletion.fireResistancePercent"},
            {"common", "General.depletesWhenNausea", "gameplay", "depletion.nauseaDepletes"},
            {"common", "General.moveSlowWhenThirsty", "gameplay", "sprint.blockedWhenThirsty"},
            {"common", "General.DrinkRainWater", "gameplay", "drinking.rain"},
            {"common", "General.EnableLoot", "gameplay", "loot.enabled"},
            {"common", "Drinking Mechanics.waterBottleStacksize", "gameplay", "drinking.waterBottleStackSize"},
            {"common", "Drinking Mechanics.ExtraHydrationConvertToQuenched", "gameplay", "drinking.extraThirstToQuenched"},
            {"common", "Drinking Mechanics.dehydrationHaltsHealthRegen", "gameplay", "regeneration.haltedWhenThirsty"},
            {"common", "Drinking Mechanics.healthRegenDepletesHydration", "gameplay", "regeneration.depletesThirst"},
            {"common", "Drinking Mechanics.healthRegenDehydrationIsBiomeDependent", "gameplay", "regeneration.climateDependent"},
            {"common", "Drinking Mechanics.canDrinkByHand", "gameplay", "hand.enabled"},
            {"common", "Drinking Mechanics.DrinkBothHandNeeded", "gameplay", "hand.bothHandsEmpty"},
            {"common", "Drinking Mechanics.handDrinkingHydration", "gameplay", "hand.thirst"},
            {"common", "Drinking Mechanics.handDrinkingQuenched", "gameplay", "hand.quenched"},
            {"common", "Drinking Mechanics.handDrinkingCooldown", "gameplay", "hand.cooldownTicks"},
            {"common", "World.altitudeBands", "purity", "world.altitudeBands"},
            {"common", "World.altitudeRelativeToSeaLevel", "purity", "world.altitudeRelativeToSeaLevel"},
            {"common", "World.worldWaterBasePurity", "purity", "world.worldWaterBasePurity"},
            {"common", "World.saltWaterPurity", "purity", "world.saltWaterPurity"},
            {"common", "World.runningWaterPurificationAmount", "purity", "world.runningWaterPurificationAmount"},
            {"common", "World.stillWaterPurificationAmount", "purity", "world.stillWaterPurificationAmount"},
            {"common", "Purity-related Effects.defaultPurity", "purity", "general.defaultPurity"},
            {"common", "Purity-related Effects.quenchThirstWhenDebuffed", "purity", "general.quenchWhenDebuffed"},
            {"common", "Create compatibility.sandFilterFiltrationAmount", "compat", "create.sandFilterFiltrationAmount"},
            {"common", "Create compatibility.sandFilterMbPerTick", "compat", "create.sandFilterMbPerTick"},
            {"item_settings", "Drinks.drinks", "items", "overrides.drinks"},
            {"item_settings", "Foods.foods", "items", "overrides.foods"},
            {"item_settings", "Blacklist.itemsBlacklist", "items", "overrides.blacklist"},
            {"container", "Container.Containers", "items", "containers.containers"},
            {"keyword", "Keyword config.enableKeywordConfig", "items", "keywords.enabled"},
            {"keyword", "Keyword config.Default Hydration values.defaultDrinkHydration", "items", "keywords.drinkThirst"},
            {"keyword", "Keyword config.Default Hydration values.defaultDrinkQuenchness", "items", "keywords.drinkQuenched"},
            {"keyword", "Keyword config.Default Hydration values.defaultSoupHydration", "items", "keywords.soupThirst"},
            {"keyword", "Keyword config.Default Hydration values.defaultSoupQuenchness", "items", "keywords.soupQuenched"},
            {"keyword", "Keyword config.Default Hydration values.defaultFruitHydration", "items", "keywords.fruitThirst"},
            {"keyword", "Keyword config.Default Hydration values.defaultFruitQuenchness", "items", "keywords.fruitQuenched"},
            {"keyword", "Keyword config.Default Hydration values.Blacklisted Keywords.keyword_blacklist", "items", "keywords.blacklist"},
            {"keyword", "Keyword config.Default Hydration values.Drink Keywords.keyword_drink", "items", "keywords.drink"},
            {"keyword", "Keyword config.Default Hydration values.Soup Keywords.keyword_soup", "items", "keywords.soup"},
            {"keyword", "Keyword config.Default Hydration values.Fruit Keywords.keyword_fruit", "items", "keywords.fruit"},
    };

    private static final Set<String> LEVEL_PATHS = Set.of("general.defaultPurity", "world.worldWaterBasePurity", "world.saltWaterPurity");

    private static final String[] LEGACY_LEVELS = {"dirty", "slightlyDirty", "acceptable", "purified"};

    private LegacyConfigMigration() {}

    public static void run()
    {
        Path dir = FMLPaths.CONFIGDIR.get().resolve(DropletsOfThirst.ID);
        ConfigMigration.copyFolder(dir.resolveSibling(LegacyIds.LEGACY_NAMESPACE), dir, NAME);
        ConfigMigration.of(dir, NAME, MOVES)
                .convert(LegacyConfigMigration::convert)
                .derive("common", "purity", LegacyConfigMigration::moveEffectPercentages)
                .run();
    }

    private static Object convert(String newFile, String newPath, Object value)
    {
        return value instanceof Number number && LEVEL_PATHS.contains(newPath) ? LegacyIds.purityFromLegacy(number.intValue()) : value;
    }

    private static void moveEffectPercentages(Config common, Supplier<CommentedConfig> purityFile)
    {
        int[][] defaults = {{100, 30}, {50, 10}, {5, 0}, {0, 0}};
        for (int purity = 0; purity < LEGACY_LEVELS.length; purity++)
        {
            String legacy = LEGACY_LEVELS[purity];
            String level = PurityConfig.EFFECT_LEVELS[LegacyIds.purityFromLegacy(purity)];
            Number nausea = common.get(List.of("Purity-related Effects", legacy + "NauseaPercentage"));
            Number poison = common.get(List.of("Purity-related Effects", legacy + "PoisonPercentage"));
            if (nausea == null && poison == null)
                continue;
            int nauseaChance = nausea == null ? defaults[purity][0] : nausea.intValue();
            int poisonChance = poison == null ? defaults[purity][1] : poison.intValue();
            List<String> effects = new ArrayList<>();
            if (nauseaChance > 0)
            {
                effects.add("minecraft:nausea,200,0," + nauseaChance);
                effects.add("minecraft:hunger,600,0," + nauseaChance);
            }
            if (poisonChance > 0)
                effects.add("minecraft:poison,200,0," + poisonChance + ",true");
            purityFile.get().set(List.of("effects", level), effects);
        }
    }
}
