package com.darkona.dropletsofthirst.compat.emi;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import com.darkona.dropletsofthirst.compat.jei.HydrationEntry;
import com.darkona.dropletsofthirst.compat.jei.PurificationEntry;
import com.darkona.dropletsofthirst.index.DropletsItems;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.util.List;

@EmiEntrypoint
public final class DropletsEmiPlugin implements EmiPlugin
{
    @Override
    public void register(EmiRegistry registry)
    {
        EmiRecipeCategory hydration = category("hydration", DropletsItems.TERRACOTTA_WATER_BOWL.get());
        registry.addCategory(hydration);
        for (HydrationEntry entry : HydrationEntry.all())
        {
            ResourceLocation item = BuiltInRegistries.ITEM.getKey(entry.stack().getItem());
            registry.addRecipe(new HydrationRecipe(hydration, DropletsOfThirst.asResource("/hydration/" + item.getNamespace() + "/" + item.getPath()), entry));
        }

        List<PurificationEntry> purification = PurificationEntry.all();
        if (purification.isEmpty())
            return;
        EmiRecipeCategory purify = category("purification", Items.CAULDRON);
        registry.addCategory(purify);
        registry.addWorkstation(purify, EmiStack.of(Items.CAULDRON));
        Item filter = CreateCompat.sandFilter();
        if (filter != Items.AIR)
            registry.addWorkstation(purify, EmiStack.of(filter));
        int id = 0;
        for (PurificationEntry entry : purification)
        {
            if (entry.fluidIn().isEmpty())
                for (int i = 0; i < entry.inputs().size(); i++)
                    registry.addRecipe(new PurificationRecipe(purify, id(entry, id++), entry, EmiStack.of(entry.inputs().get(i)), EmiStack.of(entry.outputs().get(i))));
            else
                registry.addRecipe(new PurificationRecipe(purify, id(entry, id++), entry, PurificationRecipe.water(entry.fluidIn()), PurificationRecipe.water(entry.fluidOut())));
        }
    }

    private static ResourceLocation id(PurificationEntry entry, int index)
    {
        return DropletsOfThirst.asResource("/purification/" + entry.method() + "/" + index);
    }

    private static EmiRecipeCategory category(String name, ItemLike icon)
    {
        return new EmiRecipeCategory(DropletsOfThirst.asResource(name), EmiStack.of(icon))
        {
            @Override
            public Component getName()
            {
                return Component.translatable(DropletsOfThirst.ID + ".jei." + name);
            }
        };
    }
}
