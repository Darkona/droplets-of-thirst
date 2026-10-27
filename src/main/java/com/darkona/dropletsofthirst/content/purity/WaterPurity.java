package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.config.SyncedValues;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.darkona.dropletsofthirst.index.DropletsLegacyAliases;
import com.darkona.dropletsofthirst.index.DropletsTags;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import dev.darkona.jdlib.core.config.NumberRows;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"SpellCheckingInspection","unused"})
public class WaterPurity
{
    private static final List<ContainerWithPurity> codeContainers = new CopyOnWriteArrayList<>();
    private static volatile Map<Item, ContainerWithPurity> waterContainers = Map.of();
    private static volatile List<ContainerWithPurity> dataContainers = List.of();
    public static final int MIN_PURITY = PurityLevel.MIN;
    public static final int MAX_PURITY = PurityLevel.MAX;

    public static void init()
    {
        registerContainers();
    }

    private static void registerContainers()
    {
        addContainer(new ContainerWithPurity(Items.GLASS_BOTTLE,
                PotionContents.createItemStack(Items.POTION,Potions.WATER).getItem()).setEqualsFilled(itemStack ->
                itemStack.is(Items.POTION) && isWater(itemStack.get(DataComponents.POTION_CONTENTS))));
        addContainer(new ContainerWithPurity(DropletsItems.TERRACOTTA_BOWL.get(),
                DropletsItems.TERRACOTTA_WATER_BOWL.get()));
        addContainer(new ContainerWithPurity(Items.BUCKET,
                Items.WATER_BUCKET, false).canHarvestRunningWater(false));
    }

    private static boolean isWater(@Nullable PotionContents contents)
    {
        return contents != null && contents.is(Potions.WATER);
    }

    @Deprecated
    public static void addContainer(ContainerWithPurity container)
    {
        codeContainers.add(container);
        waterContainers = merge(waterContainers.values(), List.of(container));
    }

    public static void setContainers(List<ContainerWithPurity> containers)
    {
        dataContainers = List.copyOf(containers);
        waterContainers = merge(codeContainers, containers);
    }

    public static List<Item> dataContainerItems()
    {
        return dataContainers.stream().map(ContainerWithPurity::getFilledItem).toList();
    }

    public static int defaultPurity()
    {
        return SyncedValues.defaultPurity();
    }

    public static boolean enabled()
    {
        return SyncedValues.purityEnabled();
    }

    private static Map<Item, ContainerWithPurity> merge(Collection<ContainerWithPurity> first, List<ContainerWithPurity> second)
    {
        Map<Item, ContainerWithPurity> byFilled = new HashMap<>();
        for (ContainerWithPurity container : first)
            byFilled.putIfAbsent(container.getFilledItem(), container);
        for (ContainerWithPurity container : second)
            byFilled.putIfAbsent(container.getFilledItem(), container);
        return Map.copyOf(byFilled);
    }

    public static boolean isWaterFilledContainer(ItemStack item)
    {
        ContainerWithPurity container = waterContainers.get(item.getItem());
        return container != null && !item.is(DropletsTags.PURITY_OPT_OUT) && container.equalsFilled(item);
    }

    public static Integer getPurity(ItemStack item)
    {
        Integer stored = item.get(DropletsDataComponents.PURITY);
        if (stored == null)
        {
            int fixed = ThirstHelper.getDrinkPurity(item);
            if (fixed >= MIN_PURITY)
                return fixed;
        }
        return sanitizePurity(stored);
    }

    public static Integer getPurity(FluidStack fluid)
    {
        return sanitizePurity(fluid.get(DropletsDataComponents.PURITY));
    }

    public static int sanitizePurity(@Nullable Integer purity)
    {
        return purity == null || purity < MIN_PURITY || purity > MAX_PURITY ? defaultPurity() : purity;
    }

    public static String getPurityText(int purity)
    {
        return purityTexts().texts()[level(purity).level()];
    }

    public static Component purityLine(int purity)
    {
        return purityTexts().lines()[level(purity).level()];
    }

    private record PurityTexts(Language language, String[] texts, Component[] lines) {}

    private static volatile @Nullable PurityTexts purityTexts;

    private static PurityTexts purityTexts()
    {
        Language language = Language.getInstance();
        PurityTexts cached = purityTexts;
        if (cached == null || cached.language() != language)
        {
            String[] texts = new String[MAX_PURITY + 1];
            Component[] lines = new Component[MAX_PURITY + 1];
            for (PurityLevel level : PurityLevel.values())
            {
                String text = MutableComponent.create(new TranslatableContents(level.translationKey(), level.id(), TranslatableContents.NO_ARGS)).getString();
                texts[level.level()] = text;
                lines[level.level()] = MutableComponent.create(new PlainTextContents.LiteralContents(text)).setStyle(Style.EMPTY.withColor(level.color()));
            }
            purityTexts = cached = new PurityTexts(language, texts, lines);
        }
        return cached;
    }

    public static int getPurityColor(int purity)
    {
        return level(purity).color();
    }

    private static PurityLevel level(int purity)
    {
        PurityLevel level = PurityLevel.byLevel(purity);
        return level != null ? level : Objects.requireNonNull(PurityLevel.byLevel(sanitizePurity(purity)));
    }

    public static boolean hasPurity(ItemStack item)
    {
        return item.get(DropletsDataComponents.PURITY) != null && !item.is(DropletsTags.PURITY_OPT_OUT);
    }

    public static boolean hasPurity(FluidStack fluid)
    {
        return fluid.get(DropletsDataComponents.PURITY) != null;
    }

    public static ItemStack addPurity(ItemStack item, int purity)
    {
        if (enabled() && !item.is(DropletsTags.PURITY_OPT_OUT))
            item.set(DropletsDataComponents.PURITY, sanitizePurity(purity));
        return item;
    }

    public static FluidStack addPurity(FluidStack fluid, int purity)
    {
        if (enabled())
            fluid.set(DropletsDataComponents.PURITY, sanitizePurity(purity));
        return fluid;
    }

    public static int getBlockPurity(Level level, BlockPos pos)
    {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER))
            return getWaterPurity(level, pos, fluid.isSource());
        return level.getBlockState(pos).is(Blocks.WATER_CAULDRON) ? CauldronPurity.at(level, pos) : defaultPurity();
    }

    public static int takenWaterPurity(Level level, BlockPos pos)
    {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER))
            return getWaterPurity(level, pos, fluid.isSource());
        if (level.getBlockState(pos).is(Blocks.WATER_CAULDRON))
            return CauldronPurity.at(level, pos);
        int poured = PouredWater.pickedUp(level, pos);
        return poured >= MIN_PURITY ? poured : getWaterPurity(level, pos, true);
    }

    public static int getWaterPurity(Level level, BlockPos pos, boolean source)
    {
        return getWaterPurity(level, pos, source, null);
    }

    public static int getWaterPurity(Level level, BlockPos pos, boolean source, @Nullable List<String> trace)
    {
        if (source)
        {
            int poured = PouredWater.purityAt(level, pos);
            if (poured >= MIN_PURITY)
            {
                if (trace != null)
                    trace.add("poured water: fixed " + poured);
                return poured;
            }
        }
        return getWaterPurity(level, level.getBiome(pos), pos, source, trace);
    }

    public static int getWaterPurity(Level level, Holder<Biome> biome, BlockPos pos, boolean source, @Nullable List<String> trace)
    {
        int salt = PurityConfig.SALT_WATER_PURITY.get();
        if (salt >= MIN_PURITY && biome.is(DropletsTags.SALT_WATER))
        {
            if (trace != null)
                trace.add("salt water biome: fixed " + salt);
            return salt;
        }

        BiomeWater biomeWater = biome.getData(DropletsDataMaps.BIOME_WATER);
        int purity = basePurity(level, biome, biomeWater, trace);
        int altitude = altitudeDelta(level, pos.getY());
        int flow = source ? PurityConfig.STILL_WATER_PURIFICATION_AMOUNT.get() : PurityConfig.RUNNING_WATER_PURIFICATION_AMOUNT.get();
        int delta = biomeWater != null ? biomeWater.delta() : 0;
        int max = biomeWater != null ? biomeWater.max() : MAX_PURITY;
        if (trace != null)
        {
            trace.add("altitude band: " + signed(altitude));
            trace.add((source ? "still" : "running") + " water: " + signed(flow));
            trace.add("biome_water delta: " + signed(delta) + ", max " + max);
        }
        return Math.max(MIN_PURITY, Math.min(purity + altitude + flow + delta, max));
    }

    private static String signed(int value)
    {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private static final NumberRows ALTITUDE_BANDS = new NumberRows(3);

    private static int altitudeDelta(Level level, int y)
    {
        if (PurityConfig.ALTITUDE_RELATIVE_TO_SEA_LEVEL.get())
            y -= level.getSeaLevel();
        return (int) NumberRows.band(ALTITUDE_BANDS.get(PurityConfig.ALTITUDE_BANDS.get()), y, 0);
    }

    private static int basePurity(Level level, Holder<Biome> biome, @Nullable BiomeWater biomeWater, @Nullable List<String> trace)
    {
        String from;
        int purity;
        DimensionWater dimensionWater;
        if (biomeWater != null && biomeWater.base().isPresent())
        {
            from = "biome_water data map";
            purity = biomeWater.base().get();
        }
        else if ((purity = taggedPurity(biome)) >= MIN_PURITY)
            from = "biome tag droplets_of_thirst:water_purity/" + purity;
        else if ((dimensionWater = level.dimensionTypeRegistration().getData(DropletsDataMaps.DIMENSION_WATER)) != null && dimensionWater.base().isPresent())
        {
            from = "dimension_water data map";
            purity = dimensionWater.base().get();
        }
        else
        {
            from = "worldWaterBasePurity";
            purity = PurityConfig.WORLD_WATER_BASE_PURITY.get();
        }
        if (trace != null)
            trace.add("base " + purity + " from " + from);
        return purity;
    }

    private static int taggedPurity(Holder<Biome> biome)
    {
        for (int purity = MAX_PURITY; purity >= MIN_PURITY; purity--)
            if (biome.is(DropletsTags.WATER_PURITY[purity]))
                return purity;
        return -1;
    }

    public static int waterThirstBonus(int purity)
    {
        return purity == PurityLevel.PURE.level() && enabled() ? SyncedValues.pureThirstBonus() : 0;
    }

    public static int waterQuenchedBonus(int purity)
    {
        return purity == PurityLevel.PURE.level() && enabled() ? SyncedValues.pureQuenchedBonus() : 0;
    }

    public static int drinkPurity(ItemStack item)
    {
        return isWaterFilledContainer(item) ? getPurity(item) : ThirstHelper.getDrinkPurity(item);
    }

    public static void migrateLegacyPurity(MutableDataComponentHolder stack)
    {
        if (!stack.has(DropletsDataComponents.LEGACY_PURITY))
            return;
        Integer legacy = stack.remove(DropletsDataComponents.LEGACY_PURITY);
        if (legacy != null && !stack.has(DropletsDataComponents.PURITY))
            stack.set(DropletsDataComponents.PURITY, DropletsDataComponents.bounded(DropletsLegacyAliases.purityFromLegacy(legacy)));
    }
}
