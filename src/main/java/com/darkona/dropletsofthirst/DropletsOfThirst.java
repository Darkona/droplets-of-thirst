package com.darkona.dropletsofthirst;

import com.darkona.dropletsofthirst.api.DropletsAPI;
import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import com.darkona.dropletsofthirst.compat.create.CreateRegistry;
import com.darkona.dropletsofthirst.compat.delight.DelightCompat;
import com.darkona.dropletsofthirst.compat.reliquary.ReliquaryCompat;
import com.darkona.dropletsofthirst.compat.travelersbackpack.TravelersBackpackCompat;
import com.darkona.dropletsofthirst.compat.vampirism.VampirismCompat;
import com.darkona.dropletsofthirst.config.DiscouragedMods;
import com.darkona.dropletsofthirst.config.DropletsConfig;
import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirstManager;
import com.darkona.dropletsofthirst.gametest.DropletsGameTests;
import com.darkona.dropletsofthirst.impl.DropletsServiceImpl;
import com.darkona.dropletsofthirst.index.DropletsAttributes;
import com.darkona.dropletsofthirst.index.DropletsBlockEntityTypes;
import com.darkona.dropletsofthirst.index.DropletsBlocks;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.darkona.dropletsofthirst.index.DropletsConditions;
import com.darkona.dropletsofthirst.index.DropletsCreativeModeTabs;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.darkona.dropletsofthirst.index.DropletsLoot;
import com.darkona.dropletsofthirst.index.DropletsMobEffects;
import com.darkona.dropletsofthirst.index.DropletsPotions;
import com.darkona.dropletsofthirst.index.DropletsRecipes;
import com.darkona.dropletsofthirst.network.DropletsPackets;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.nio.file.Path;

@Mod(DropletsOfThirst.ID)
public class DropletsOfThirst
{
    public static final String ID = DropletsAPI.MOD_ID;

    public DropletsOfThirst()
    {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        DropletsAPI.setService(DropletsServiceImpl.INSTANCE);
        DropletsCapabilities.register(modBus);
        DropletsBlocks.register(modBus);
        DropletsBlockEntityTypes.register(modBus);
        DropletsItems.register(modBus);
        DropletsRecipes.register(modBus);
        DropletsMobEffects.register(modBus);
        DropletsPotions.register(modBus);
        DropletsAttributes.register(modBus);
        DropletsConditions.register(modBus);
        DropletsLoot.register(modBus);
        DropletsCreativeModeTabs.register(modBus);
        DropletsPackets.register();
        modBus.addListener(this::commonSetup);
        modBus.addListener(PlayerThirstManager::onConfigReloaded);
        modBus.addListener(DropletsOfThirst::addPacks);
        modBus.addListener(DropletsGameTests::register);
        MinecraftForge.EVENT_BUS.addListener(DropletsDataMaps::addReloadListeners);
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGH, DropletsDataMaps::bind);
        DiscouragedMods.warn();
        TravelersBackpackCompat.init(modBus);
        ColdSweatCompat.init();
        ReliquaryCompat.init();
        VampirismCompat.init();
        DelightCompat.init();

        if (CreateCompat.LOADED)
            CreateRegistry.register();

        if (FMLEnvironment.dist.isClient())
            DropletsClient.init(modBus);

        DropletsConfig.register();
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        WaterPurity.init();
        event.enqueueWork(CauldronPurity::registerInteractions);
        event.enqueueWork(DropletsPotions::registerBrewing);
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
        if (event.getPackType() != PackType.SERVER_DATA)
            return;
        Path path = ModList.get().getModFileById(ID).getFile().findResource("datapacks/" + name);
        String id = "mod/" + ID + ":datapacks/" + name;
        event.addRepositorySource(packs -> {
            Pack pack = Pack.readMetaAndCreate(id, Component.literal("Droplets of Thirst: " + title), false,
                    packId -> new PathPackResources(packId, path, false), PackType.SERVER_DATA, Pack.Position.TOP, source);
            if (pack != null)
                packs.accept(pack);
        });
    }

    public static ResourceLocation asResource(String path)
    {
        return new ResourceLocation(ID, path);
    }
}
