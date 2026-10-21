package com.darkona.dropletsofthirst.compat.jei;

import com.darkona.dropletsofthirst.api.ThirstValues;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record HydrationEntry(ItemStack stack, ThirstValues values)
{
    private static final Comparator<HydrationEntry> BY_ID = Comparator.comparing(entry -> BuiltInRegistries.ITEM.getKey(entry.stack.getItem()));

    public static List<HydrationEntry> all()
    {
        List<HydrationEntry> entries = new ArrayList<>();
        add(entries, ThirstHelper.drinkTable().keySet());
        int drinks = entries.size();
        add(entries, ThirstHelper.foodTable().keySet());
        entries.subList(0, drinks).sort(BY_ID);
        entries.subList(drinks, entries.size()).sort(BY_ID);
        return entries;
    }

    private static void add(List<HydrationEntry> entries, Collection<Item> items)
    {
        for (Item item : items)
        {
            ItemStack stack = item == Items.POTION ? PotionContents.createItemStack(Items.POTION, Potions.WATER) : new ItemStack(item);
            ThirstValues values = ThirstHelper.valuesOf(stack);
            if (values != null && (values.thirst() != 0 || values.quenched() != 0))
                entries.add(new HydrationEntry(stack, values));
        }
    }
}
