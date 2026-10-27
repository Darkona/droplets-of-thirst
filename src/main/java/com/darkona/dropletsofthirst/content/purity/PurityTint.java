package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.index.DropletsItems;
import net.minecraft.world.item.ItemStack;

public final class PurityTint
{
    public static final int VANILLA_WATER = 0xFF385DC6;

    private static final int[] COLORS = {0xFF6B4A2B, 0xFF9C7A52, 0xFF6F7F66, VANILLA_WATER, 0xFF6FC8FF, 0xFFD8F1FF};

    public static int colorOf(int purity)
    {
        return purity >= PurityLevel.MIN && purity <= PurityLevel.MAX ? COLORS[purity] : VANILLA_WATER;
    }

    public static int color(ItemStack stack, int tintIndex, int original)
    {
        if (tintIndex != liquidLayer(stack) || !WaterPurity.enabled() || !WaterPurity.isWaterFilledContainer(stack) || !WaterPurity.hasPurity(stack))
            return original;
        return colorOf(WaterPurity.getPurity(stack));
    }

    public static int liquidLayer(ItemStack stack)
    {
        return stack.is(DropletsItems.TERRACOTTA_WATER_BOWL.get()) ? 1 : 0;
    }

    private PurityTint() {}
}
