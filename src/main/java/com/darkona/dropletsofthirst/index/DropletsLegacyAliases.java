package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.PurityLevel;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.List;
import java.util.Map;

public final class DropletsLegacyAliases
{
    public static final String LEGACY_NAMESPACE = "thirst";

    private static final Map<ResourceKey<? extends Registry<?>>, List<String>> ALIASES = Map.of(
            Registries.ITEM, List.of("clay_bowl", "terracotta_bowl", "terracotta_water_bowl", "sand_filter"),
            Registries.BLOCK, List.of("sand_filter"),
            Registries.BLOCK_ENTITY_TYPE, List.of("sand_filter"),
            Registries.MOB_EFFECT, List.of("quenchness"),
            NeoForgeRegistries.Keys.ATTACHMENT_TYPES, List.of("player_thirst"),
            NeoForgeRegistries.Keys.CONDITION_CODECS, List.of("loot_config")
    );

    private static final int[] LEGACY_PURITY = {PurityLevel.CONTAMINATED.level(), PurityLevel.DIRTY.level(), PurityLevel.ACCEPTABLE.level(), PurityLevel.PURE.level()};

    private DropletsLegacyAliases() {}

    public static int purityFromLegacy(int purity)
    {
        return purity >= 0 && purity < LEGACY_PURITY.length ? LEGACY_PURITY[purity] : purity;
    }

    public static void register(IEventBus modBus)
    {
        modBus.addListener(DropletsLegacyAliases::addAliases);
    }

    private static void addAliases(RegisterEvent event)
    {
        if (event.getRegistryKey().equals(Registries.DATA_COMPONENT_TYPE))
            event.getRegistry().addAlias(ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, "purity"), DropletsOfThirst.asResource("legacy_purity"));
        List<String> paths = ALIASES.get(event.getRegistryKey());
        if (paths == null)
            return;

        Registry<?> registry = event.getRegistry();
        for (String path : paths)
            registry.addAlias(ResourceLocation.fromNamespaceAndPath(LEGACY_NAMESPACE, path), DropletsOfThirst.asResource(path));
    }
}
