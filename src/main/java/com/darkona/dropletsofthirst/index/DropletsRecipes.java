package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.thirst.CanteenFillRecipe;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class DropletsRecipes
{
    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, DropletsOfThirst.ID);

    public static final Supplier<RecipeSerializer<CanteenFillRecipe>> CANTEEN_FILL = SERIALIZERS.register("canteen_fill",
            () -> new SimpleCraftingRecipeSerializer<>(CanteenFillRecipe::new));

    private DropletsRecipes() {}

    public static void register(IEventBus modBus)
    {
        SERIALIZERS.register(modBus);
    }
}
