package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class CanteenFluidHandler extends FluidHandlerItemStack
{
    public CanteenFluidHandler(ItemStack container)
    {
        super(container, CanteenItem.CAPACITY);
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

    public void store(FluidStack fluid)
    {
        setFluid(fluid);
    }

    @Override
    public boolean canFillFluidType(FluidStack fluid)
    {
        return CanteenItem.heldDrink(container).isEmpty() && CanteenItem.drinkable(fluid);
    }
}
