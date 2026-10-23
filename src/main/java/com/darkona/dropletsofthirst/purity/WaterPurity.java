package com.darkona.dropletsofthirst.purity;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.api.event.PurityEffectEvent;
import com.darkona.dropletsofthirst.data.DropletsDataMaps;
import com.darkona.dropletsofthirst.data.DropletsDataMaps.BiomeWater;
import com.darkona.dropletsofthirst.data.DropletsDataMaps.DimensionWater;
import com.darkona.dropletsofthirst.data.DropletsTags;
import com.darkona.dropletsofthirst.thirst.EffectInit;
import com.darkona.dropletsofthirst.item.ItemInit;
import com.darkona.dropletsofthirst.thirst.ThirstHelper;
import com.darkona.dropletsofthirst.thirst.RegisterThirstValueEvent;
import com.darkona.dropletsofthirst.foundation.config.ClientConfig;
import com.darkona.dropletsofthirst.foundation.config.PurityConfig;
import com.darkona.dropletsofthirst.foundation.config.SyncedValues;
import dev.darkona.jdlib.core.config.NumberRows;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
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
    public static final int CAULDRON_PURITY = PurityLevel.MURKY.level();
    public static final int HEATED_CAULDRON_PURITY = PurityLevel.CLEAN.level();

    public static void init()
    {
        registerContainers();
    }

    public static void registerCauldronInteractions()
    {
        CauldronInteraction.WATER.map().put(ItemInit.TERRACOTTA_BOWL.get(), fillFromCauldron(() -> new ItemStack(ItemInit.TERRACOTTA_WATER_BOWL.get()), SoundEvents.BUCKET_FILL));
    }

    public static CauldronInteraction fillFromCauldron(Supplier<ItemStack> filled, SoundEvent sound)
    {
        return (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide())
            {
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, filled.get()));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(stack.getItem()));
                LayeredCauldronBlock.lowerFillLevel(state, level, pos);
                level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        };
    }

    private static void registerContainers()
    {
        addContainer(new ContainerWithPurity(Items.GLASS_BOTTLE,
                PotionContents.createItemStack(Items.POTION,Potions.WATER).getItem()).setEqualsFilled(itemStack ->
                itemStack.is(Items.POTION) && isWater(itemStack.get(DataComponents.POTION_CONTENTS))));
        addContainer(new ContainerWithPurity(ItemInit.TERRACOTTA_BOWL.get(),
                ItemInit.TERRACOTTA_WATER_BOWL.get()));
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

    public static BlockHitResult pickFluid(Player player, ClipContext.Fluid fluid)
    {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getViewVector(1.0F).scale(player.blockInteractionRange()));
        return player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, fluid, player));
    }

    public static void renderPurityTooltip(ItemTooltipEvent event)
    {
        if(enabled() && isWaterFilledContainer(event.getItemStack()) && (event.getFlags().hasShiftDown() || !ClientConfig.ONLY_SHOW_PURITY_WHEN_SHIFTING.get()))
        {
            int purity = getPurity(event.getItemStack());
            if(purity >= MIN_PURITY && purity <= MAX_PURITY)
                event.getToolTip().add(purityLine(purity));
        }
    }

    public static boolean isWaterFilledContainer(ItemStack item)
    {
        ContainerWithPurity container = waterContainers.get(item.getItem());
        return container != null && !item.is(DropletsTags.PURITY_OPT_OUT) && container.equalsFilled(item);
    }

    public static Integer getPurity(ItemStack item)
    {
        Integer stored = item.get(ThirstComponent.PURITY);
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
        return sanitizePurity(fluid.get(ThirstComponent.PURITY));
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
        return item.get(ThirstComponent.PURITY) != null && !item.is(DropletsTags.PURITY_OPT_OUT);
    }

    public static boolean hasPurity(FluidStack fluid)
    {
        return fluid.get(ThirstComponent.PURITY) != null;
    }

    public static ItemStack addPurity(ItemStack item, int purity)
    {
        if (enabled() && !item.is(DropletsTags.PURITY_OPT_OUT))
            item.set(ThirstComponent.PURITY, sanitizePurity(purity));
        return item;
    }

    public static FluidStack addPurity(FluidStack fluid, int purity)
    {
        if (enabled())
            fluid.set(ThirstComponent.PURITY, sanitizePurity(purity));
        return fluid;
    }

    public static int getBlockPurity(Level level, BlockPos pos)
    {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER))
            return getWaterPurity(level, pos, fluid.isSource());
        return level.getBlockState(pos).is(Blocks.WATER_CAULDRON) ? cauldronPurity(level, pos) : defaultPurity();
    }

    public static int takenWaterPurity(Level level, BlockPos pos)
    {
        FluidState fluid = level.getFluidState(pos);
        if (fluid.is(FluidTags.WATER))
            return getWaterPurity(level, pos, fluid.isSource());
        if (level.getBlockState(pos).is(Blocks.WATER_CAULDRON))
            return cauldronPurity(level, pos);
        int poured = PouredWater.pickedUp(level, pos);
        return poured >= MIN_PURITY ? poured : getWaterPurity(level, pos, true);
    }

    private static int takingFromCauldron = -1;

    public static boolean takingFromCauldron(BlockState state, Level level, BlockPos pos)
    {
        if (level.isClientSide() || !enabled() || !state.is(Blocks.WATER_CAULDRON))
            return false;
        takingFromCauldron = cauldronPurity(level, pos);
        return true;
    }

    public static void doneTakingFromCauldron()
    {
        takingFromCauldron = -1;
    }

    public static void applyCauldronPurity(ItemStack filled)
    {
        if (takingFromCauldron >= MIN_PURITY && isWaterFilledContainer(filled) && !hasPurity(filled))
            addPurity(filled, takingFromCauldron);
    }

    public static int cauldronPurity(BlockGetter level, BlockPos pos)
    {
        BlockState below = level.getBlockState(pos.below());
        return below.is(DropletsTags.CAULDRON_HEAT_SOURCES) && below.getOptionalValue(BlockStateProperties.LIT).orElse(true)
                ? HEATED_CAULDRON_PURITY : CAULDRON_PURITY;
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

    public record PurityEffect(Holder<MobEffect> effect, int duration, int amplifier, float chance, boolean blocksHydration) {}

    private record EffectTables(List<List<? extends String>> source, PurityEffect[][] byPurity) {}

    private static volatile EffectTables effectTables = new EffectTables(List.of(), new PurityEffect[0][]);

    public static boolean givePurityEffects(Player player, int purity)
    {
        if (!enabled() || purity < MIN_PURITY || purity > MAX_PURITY)
            return true;
        boolean hydrate = true;
        List<MobEffectInstance> effects = new ArrayList<>();
        float roll = player.getRandom().nextFloat();
        for (PurityEffect effect : effectTable(purity))
        {
            if (roll >= effect.chance())
                continue;
            effects.add(new MobEffectInstance(effect.effect(), effect.duration(), effect.amplifier()));
            hydrate &= !effect.blocksHydration();
        }
        if (PurityConfig.HOT_DIRTY_WATER.get() && purity <= PurityConfig.HOT_DIRTY_WATER_MAX_PURITY.get() && player instanceof ServerPlayer && ThirstHelper.isHotClimate(player))
            effects.add(new MobEffectInstance(EffectInit.DEHYDRATION, PurityConfig.HOT_DIRTY_WATER_DURATION.get(), PurityConfig.HOT_DIRTY_WATER_AMPLIFIER.get()));
        PurityEffectEvent event = NeoForge.EVENT_BUS.post(new PurityEffectEvent(player, purity, effects, hydrate || PurityConfig.QUENCH_WHEN_DEBUFFED.get()));
        if (event.isCanceled())
            return true;
        if (player instanceof ServerPlayer)
            for (MobEffectInstance effect : event.getEffects())
                player.addEffect(effect);
        return event.hydrates();
    }

    public static PurityEffect[] effectTable(int purity)
    {
        EffectTables cached = effectTables;
        List<List<? extends String>> source = cached.source();
        boolean stale = source.size() != PurityConfig.EFFECTS.size();
        for (int i = 0; !stale && i < source.size(); i++)
            stale = source.get(i) != PurityConfig.EFFECTS.get(i).get();
        if (stale)
        {
            List<List<? extends String>> lists = new ArrayList<>();
            PurityEffect[][] byPurity = new PurityEffect[PurityConfig.EFFECTS.size()][];
            for (int i = 0; i < byPurity.length; i++)
            {
                List<? extends String> list = PurityConfig.EFFECTS.get(i).get();
                lists.add(list);
                byPurity[i] = list.stream().map(WaterPurity::parseEffect).filter(Objects::nonNull).toArray(PurityEffect[]::new);
            }
            effectTables = cached = new EffectTables(lists, byPurity);
        }
        return cached.byPurity()[purity];
    }

    private static @Nullable PurityEffect parseEffect(String entry)
    {
        if (!PurityConfig.isValidEffect(entry))
            return null;
        String[] parts = entry.split(",");
        Holder<MobEffect> effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(parts[0].trim())).orElse(null);
        if (effect == null)
            return null;
        return new PurityEffect(effect, Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()),
                (float) (Double.parseDouble(parts[3].trim()) / 100.0), parts.length == 5 && parts[4].trim().equals("true"));
    }
}
