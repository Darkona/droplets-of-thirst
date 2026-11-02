package com.darkona.dropletsofthirst.compat.emi;

import com.darkona.dropletsofthirst.compat.jei.HydrationEntry;
import com.darkona.dropletsofthirst.content.thirst.client.DrinkTooltip;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

final class HydrationRecipe implements EmiRecipe
{
    private static final int WIDTH = 150;
    private static final int HEIGHT = 32;
    private static final int VALUES_X = 24;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final HydrationEntry entry;
    private final EmiStack stack;

    HydrationRecipe(EmiRecipeCategory category, ResourceLocation id, HydrationEntry entry)
    {
        this.category = category;
        this.id = id;
        this.entry = entry;
        this.stack = EmiStack.of(entry.stack());
    }

    @Override
    public EmiRecipeCategory getCategory()
    {
        return category;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs()
    {
        return List.of(stack);
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return List.of();
    }

    @Override
    public int getDisplayWidth()
    {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight()
    {
        return HEIGHT;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        Font font = Minecraft.getInstance().font;
        DrinkTooltip row = DrinkTooltip.of(entry.values());
        widgets.addSlot(stack, 0, 7);
        widgets.addDrawable(VALUES_X, 1, row.getWidth(font), row.getHeight(), (draw, mouseX, mouseY, delta) -> row.renderImage(font, 0, 0, draw));
        widgets.addText(entry.valuesText(), VALUES_X, 22, -1, true);
    }
}
