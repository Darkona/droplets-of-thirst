package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.thirst.CanteenFillRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DropletsRecipes
{
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, DropletsOfThirst.ID);

    public static final RegistryObject<RecipeSerializer<CanteenFillRecipe>> CANTEEN_FILL = SERIALIZERS.register("canteen_fill",
            () -> new SimpleCraftingRecipeSerializer<>(CanteenFillRecipe::new));

    private DropletsRecipes() {}

    public static void register(IEventBus modBus)
    {
        SERIALIZERS.register(modBus);
    }
}
