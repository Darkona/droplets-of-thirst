package com.darkona.dropletsofthirst.compat.jei;

import com.darkona.dropletsofthirst.purity.CauldronPurity;
import com.darkona.dropletsofthirst.compat.create.CreateCompat;
import com.darkona.dropletsofthirst.index.DropletsTags;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsItems;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;

public record PurificationEntry(String method, List<ItemStack> inputs, List<ItemStack> outputs, FluidStack fluidIn, FluidStack fluidOut,
                                ItemStack machine, List<ItemStack> below)
{
    public static final int FLUID_AMOUNT = 1000;

    public static List<PurificationEntry> all()
    {
        if (!WaterPurity.enabled())
            return List.of();
        List<PurificationEntry> entries = new ArrayList<>();
        ItemStack cauldron = new ItemStack(Items.CAULDRON);
        entries.add(containers("cauldron", CauldronPurity.CAULDRON_PURITY, cauldron, List.of()));
        List<ItemStack> heat = heatSources();
        if (!heat.isEmpty())
            entries.add(containers("heated_cauldron", CauldronPurity.HEATED_CAULDRON_PURITY, cauldron, heat));
        Item filter = CreateCompat.sandFilter();
        if (filter != Items.AIR)
            for (int purity = WaterPurity.MIN_PURITY; purity < WaterPurity.MAX_PURITY; purity++)
            {
                int filtered = CreateCompat.sandFilterPurity(purity);
                if (filtered > purity)
                    entries.add(new PurificationEntry("sand_filter", List.of(), List.of(), water(purity), water(filtered), new ItemStack(filter), List.of()));
            }
        return entries;
    }

    private static PurificationEntry containers(String method, int purity, ItemStack machine, List<ItemStack> below)
    {
        List<ItemStack> inputs = new ArrayList<>();
        List<ItemStack> outputs = new ArrayList<>();
        for (int from = WaterPurity.MIN_PURITY; from < purity; from++)
            for (ItemStack container : waterContainers())
            {
                inputs.add(WaterPurity.addPurity(container.copy(), from));
                outputs.add(WaterPurity.addPurity(container.copy(), purity));
            }
        return new PurificationEntry(method, inputs, outputs, FluidStack.EMPTY, FluidStack.EMPTY, machine, below);
    }

    private static List<ItemStack> waterContainers()
    {
        return List.of(PotionContents.createItemStack(Items.POTION, Potions.WATER), new ItemStack(Items.WATER_BUCKET), new ItemStack(DropletsItems.TERRACOTTA_WATER_BOWL.get()));
    }

    private static List<ItemStack> heatSources()
    {
        List<ItemStack> items = new ArrayList<>();
        for (Holder<Block> block : BuiltInRegistries.BLOCK.getTagOrEmpty(DropletsTags.CAULDRON_HEAT_SOURCES))
        {
            Item item = block.value().asItem();
            if (item != Items.AIR)
                items.add(new ItemStack(item));
        }
        return items;
    }

    private static FluidStack water(int purity)
    {
        return WaterPurity.addPurity(new FluidStack(Fluids.WATER, FLUID_AMOUNT), purity);
    }
}
