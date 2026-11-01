package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidHandlerItemStack;

public class CanteenFluidHandler extends FluidHandlerItemStack
{
    public CanteenFluidHandler(ItemStack container)
    {
        super(() -> DropletsDataComponents.CANTEEN_FLUID, container, CanteenItem.CAPACITY);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action)
    {
        FluidStack held = getFluid();
        if (held.getAmount() >= capacity || !canFillFluidType(resource))
            return super.fill(resource, action);
        FluidStack mixed = WaterPurity.mixInto(held, resource, action.execute());
        if (action.execute() && !held.isEmpty())
            setFluid(held);
        return super.fill(mixed, action);
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid)
    {
        return CanteenItem.heldDrink(container).isEmpty() && CanteenItem.drinkable(fluid);
    }
}
