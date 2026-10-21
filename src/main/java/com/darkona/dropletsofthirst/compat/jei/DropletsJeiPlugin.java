package com.darkona.dropletsofthirst.compat.jei;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

@JeiPlugin
public final class DropletsJeiPlugin implements IModPlugin
{
    static final RecipeType<PurificationEntry> PURIFICATION = RecipeType.create(DropletsOfThirst.ID, "purification", PurificationEntry.class);
    static final RecipeType<HydrationEntry> HYDRATION = RecipeType.create(DropletsOfThirst.ID, "hydration", HydrationEntry.class);

    private List<PurificationEntry> purification = List.of();

    @Override
    public ResourceLocation getPluginUid()
    {
        return DropletsOfThirst.asResource("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        purification = PurificationEntry.all();
        registration.addRecipeCategories(new HydrationCategory(registration.getJeiHelpers().getGuiHelper()));
        if (!purification.isEmpty())
            registration.addRecipeCategories(new PurificationCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        registration.addRecipes(HYDRATION, HydrationEntry.all());
        if (!purification.isEmpty())
            registration.addRecipes(PURIFICATION, purification);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        if (purification.isEmpty())
            return;
        registration.addRecipeCatalyst(Items.CAULDRON, PURIFICATION);
        Item filter = CreateCompat.sandFilter();
        if (filter != Items.AIR)
            registration.addRecipeCatalyst(filter, PURIFICATION);
    }
}
