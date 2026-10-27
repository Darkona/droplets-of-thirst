package com.darkona.dropletsofthirst.compat.create.ponder;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.compat.create.CreateRegistry;
import com.simibubi.create.infrastructure.ponder.AllCreatePonderTags;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.foundation.PonderIndex;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ThirstPonderPlugin implements PonderPlugin {
    public static final ResourceLocation PURIFICATION = DropletsOfThirst.asResource("purification");

    public static void register() {
        PonderIndex.addPlugin(new ThirstPonderPlugin());
    }

    @Override
    public @NotNull String getModId() {
        return DropletsOfThirst.ID;
    }

    @Override
    public void registerScenes(@NotNull PonderSceneRegistrationHelper<ResourceLocation> helper) {
        PonderSceneRegistrationHelper<ItemProviderEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.addStoryBoard(
                CreateRegistry.SAND_FILTER_BLOCK,
                "sand_filter",
                SandFilterScene::filtering,
                AllCreatePonderTags.FLUIDS,
                PURIFICATION
        );
    }

    @Override
    public void registerTags(@NotNull PonderTagRegistrationHelper<ResourceLocation> helper) {
        PonderTagRegistrationHelper<RegistryEntry<?,?>> HELPER = helper.withKeyFunction(RegistryEntry::getId);
        HELPER.registerTag(PURIFICATION)
                .addToIndex()
                .item(CreateRegistry.SAND_FILTER_BLOCK, true, false)
                .title("Purification")
                .description("Components which purifying water")
                .register();
    }
}
