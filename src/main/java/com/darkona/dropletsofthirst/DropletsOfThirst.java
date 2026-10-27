package com.darkona.dropletsofthirst;

import com.darkona.dropletsofthirst.impl.DropletsServiceImpl;
import com.darkona.dropletsofthirst.api.DropletsAPI;
import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import com.darkona.dropletsofthirst.compat.create.CreateRegistry;
import com.darkona.dropletsofthirst.compat.create.SandFilterBlockEntity;
import com.darkona.dropletsofthirst.compat.delight.DelightCompat;
import com.darkona.dropletsofthirst.compat.reliquary.ReliquaryCompat;
import com.darkona.dropletsofthirst.compat.travelersbackpack.TravelersBackpackCompat;
import com.darkona.dropletsofthirst.compat.vampirism.VampirismCompat;
import com.darkona.dropletsofthirst.config.DropletsConfig;
import com.darkona.dropletsofthirst.index.DropletsConditions;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.darkona.dropletsofthirst.index.DropletsLootConditions;
import com.darkona.dropletsofthirst.index.DropletsPotions;
import com.darkona.dropletsofthirst.index.DropletsCreativeModeTabs;
import com.darkona.dropletsofthirst.index.DropletsLegacyAliases;
import com.darkona.dropletsofthirst.network.DropletsPackets;
import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsAttributes;
import com.darkona.dropletsofthirst.index.DropletsMobEffects;
import com.darkona.dropletsofthirst.index.DropletsAttachmentTypes;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirstManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(DropletsOfThirst.ID)
public class DropletsOfThirst
{
    public static final String ID = DropletsAPI.MOD_ID;

    public DropletsOfThirst(IEventBus modBus, ModContainer modContainer)
    {
        DropletsAPI.setService(DropletsServiceImpl.INSTANCE);
        DropletsAttachmentTypes.register(modBus);
        DropletsDataComponents.register(modBus);
        DropletsItems.register(modBus);
        DropletsMobEffects.register(modBus);
        DropletsPotions.register(modBus);
        DropletsAttributes.register(modBus);
        DropletsConditions.register(modBus);
        DropletsLootConditions.register(modBus);
        DropletsDataMaps.register(modBus);
        DropletsCreativeModeTabs.register(modBus);
        DropletsLegacyAliases.register(modBus);
        DropletsPackets.register(modBus);
        modBus.addListener(this::commonSetup);
        modBus.addListener(PlayerThirstManager::onConfigReloaded);
        modBus.addListener(DropletsOfThirst::addPacks);
        TravelersBackpackCompat.init(modBus);
        ColdSweatCompat.init();
        ReliquaryCompat.init();
        VampirismCompat.init();
        DelightCompat.init();

        if(CreateCompat.LOADED)
        {
            CreateRegistry.register();
            modBus.addListener(SandFilterBlockEntity::registerCapabilities);
        }

        DropletsConfig.register(modContainer);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        WaterPurity.init();
        event.enqueueWork(CauldronPurity::registerInteractions);
    }

    private static void addPacks(AddPackFindersEvent event)
    {
        addPack(event, "purify_smelting", "Water purification: furnace", PackSource.BUILT_IN);
        addPack(event, "purify_campfire", "Water purification: campfire", PackSource.BUILT_IN);
        addPack(event, "purify_smoking", "Water purification: smoker", PackSource.FEATURE);
        addPack(event, "purify_cooking_pot", "Water purification: cooking pot", PackSource.BUILT_IN);
        addPack(event, "clean_water_cooking", "Clean water for cooking recipes", PackSource.BUILT_IN);
        addPack(event, "preset_casual", "casual preset", PackSource.FEATURE);
        addPack(event, "preset_hardcore", "hardcore preset", PackSource.FEATURE);
    }

    private static void addPack(AddPackFindersEvent event, String name, String title, PackSource source)
    {
        event.addPackFinders(asResource("datapacks/" + name), PackType.SERVER_DATA, Component.literal("Droplets of Thirst: " + title), source, false, Pack.Position.TOP);
    }

    public static ResourceLocation asResource(String path)
    {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }
}
