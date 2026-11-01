package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.config.PurityConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public final class PurityNbt {

    public static final String PURITY = "droplets_of_thirst:purity";

    private static final int UNLOADED_DEFAULT_PURITY = PurityLevel.ACCEPTABLE.level();

    private PurityNbt() {
    }

    public static @Nullable Integer get(ItemStack stack)
    {
        return read(stack.getTag());
    }

    public static @Nullable Integer get(FluidStack fluid)
    {
        return read(fluid.getTag());
    }

    public static boolean has(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.contains(PURITY, Tag.TAG_ANY_NUMERIC);
    }

    public static void set(ItemStack stack, int purity)
    {
        stack.getOrCreateTag().putInt(PURITY, bounded(purity));
    }

    public static void set(FluidStack fluid, int purity)
    {
        fluid.getOrCreateTag().putInt(PURITY, bounded(purity));
    }

    public static void remove(ItemStack stack)
    {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(PURITY))
            return;
        tag.remove(PURITY);
        if (tag.isEmpty())
            stack.setTag(null);
    }

    public static void remove(FluidStack fluid)
    {
        CompoundTag tag = fluid.getTag();
        if (tag == null || !tag.contains(PURITY))
            return;
        tag.remove(PURITY);
        if (tag.isEmpty())
            fluid.setTag(null);
    }

    private static @Nullable Integer read(@Nullable CompoundTag tag)
    {
        if (tag == null || !tag.contains(PURITY, Tag.TAG_ANY_NUMERIC))
            return null;
        return bounded(tag.getInt(PURITY));
    }

    private static int bounded(int purity)
    {
        if (purity >= WaterPurity.MIN_PURITY && purity <= WaterPurity.MAX_PURITY)
            return purity;
        return PurityConfig.SPEC.isLoaded() ? WaterPurity.defaultPurity() : UNLOADED_DEFAULT_PURITY;
    }
}
