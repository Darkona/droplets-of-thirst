package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.index.DropletsItems;
import com.darkona.dropletsofthirst.index.DropletsRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CanteenFillRecipe extends CustomRecipe
{
    public CanteenFillRecipe(CraftingBookCategory category)
    {
        super(category);
    }

    private static ItemStack filled(CraftingInput input)
    {
        ItemStack canteen = ItemStack.EMPTY;
        for (int slot = 0; slot < input.size() && canteen.isEmpty(); slot++)
            if (input.getItem(slot).is(DropletsItems.CANTEEN.get()))
                canteen = input.getItem(slot);
        if (canteen.isEmpty())
            return ItemStack.EMPTY;
        ItemStack result = canteen.copy();
        boolean filled = false;
        for (int slot = 0; slot < input.size(); slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty() || stack == canteen)
                continue;
            if (!CanteenItem.fill(result, stack))
                return ItemStack.EMPTY;
            filled = true;
        }
        return filled ? result : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(@NotNull CraftingInput input, @NotNull Level level)
    {
        return !filled(input).isEmpty();
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull CraftingInput input, HolderLookup.@NotNull Provider registries)
    {
        return filled(input);
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull CraftingInput input)
    {
        NonNullList<ItemStack> left = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        for (int slot = 0; slot < input.size(); slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (!stack.isEmpty() && !stack.is(DropletsItems.CANTEEN.get()))
                left.set(slot, CanteenItem.emptied(stack));
        }
        return left;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer()
    {
        return DropletsRecipes.CANTEEN_FILL.get();
    }
}
