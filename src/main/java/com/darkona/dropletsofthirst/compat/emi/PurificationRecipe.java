package com.darkona.dropletsofthirst.compat.emi;

import com.darkona.dropletsofthirst.compat.jei.PurificationEntry;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TextWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

final class PurificationRecipe implements EmiRecipe
{
    private static final int WIDTH = 150;
    private static final int TEXT_Y = 44;
    private static final int LINE_HEIGHT = 10;

    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    private final PurificationEntry entry;
    private final EmiStack input;
    private final EmiStack output;
    private final EmiStack machine;
    private final EmiIngredient below;

    PurificationRecipe(EmiRecipeCategory category, ResourceLocation id, PurificationEntry entry, EmiStack input, EmiStack output)
    {
        this.category = category;
        this.id = id;
        this.entry = entry;
        this.input = input;
        this.output = output;
        this.machine = EmiStack.of(entry.machine());
        this.below = EmiIngredient.of(entry.below().stream().map(EmiStack::of).toList());
    }

    static EmiStack water(FluidStack fluid)
    {
        return NeoForgeEmiStack.of(fluid);
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
        return List.of(input);
    }

    @Override
    public List<EmiIngredient> getCatalysts()
    {
        List<EmiIngredient> catalysts = new ArrayList<>();
        catalysts.add(machine);
        if (!below.isEmpty())
            catalysts.add(below);
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs()
    {
        return List.of(output);
    }

    @Override
    public int getDisplayWidth()
    {
        return WIDTH;
    }

    @Override
    public int getDisplayHeight()
    {
        return TEXT_Y + 20;
    }

    @Override
    public void addWidgets(WidgetHolder widgets)
    {
        withPurity(widgets.addSlot(input, 12, 10), entry.fluidIn());
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 36, 10);
        widgets.addSlot(machine, WIDTH / 2 - 9, 1).catalyst(true);
        if (!below.isEmpty())
            widgets.addSlot(below, WIDTH / 2 - 9, 19).catalyst(true);
        widgets.addTexture(EmiTexture.EMPTY_ARROW, WIDTH - 60, 10);
        withPurity(widgets.addSlot(output, WIDTH - 28, 10), entry.fluidOut()).recipeContext(this);
        Font font = Minecraft.getInstance().font;
        int y = TEXT_Y;
        for (FormattedCharSequence line : font.split(entry.description(), WIDTH))
        {
            widgets.addText(line, WIDTH / 2, y, -1, true).horizontalAlign(TextWidget.Alignment.CENTER);
            y += LINE_HEIGHT;
        }
    }

    private static SlotWidget withPurity(SlotWidget slot, FluidStack fluid)
    {
        if (fluid.isEmpty())
            return slot;
        int purity = WaterPurity.getPurity(fluid);
        return slot.appendTooltip(Component.literal(WaterPurity.getPurityText(purity)).withColor(WaterPurity.getPurityColor(purity)));
    }
}
