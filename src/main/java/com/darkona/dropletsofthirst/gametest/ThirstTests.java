package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.api.event.DehydrationDamageEvent;
import java.util.Set;
import java.util.Map;
import java.util.List;
import com.darkona.dropletsofthirst.content.thirst.RecipeInference;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.Item;
import net.minecraft.world.Difficulty;
import net.minecraft.core.NonNullList;
import com.darkona.dropletsofthirst.content.thirst.ExhaustionFactors;
import com.darkona.dropletsofthirst.content.purity.PurityEffects;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirstManager;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;
import com.darkona.dropletsofthirst.api.ThirstValues;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.event.ThirstChangeEvent;
import com.darkona.dropletsofthirst.content.purity.DimensionWater;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.darkona.dropletsofthirst.index.DropletsMobEffects;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.UUID;

import static com.darkona.dropletsofthirst.gametest.TestSupport.player;
import static com.darkona.dropletsofthirst.gametest.TestSupport.set;
import static com.darkona.dropletsofthirst.gametest.TestSupport.thirst;
import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;

@GameTestHolder(DropletsOfThirst.ID)
@PrefixGameTestTemplate(false)
public class ThirstTests
{

    private static ThirstChangeEvent.Cause lastCause;
    private static float lastDehydrationDamage;

    private static final String IMMORTAL_TAG = "droplets-test-immortal";

    private static final String PROTECTED_TAG = "droplets-test-protected";

    private static final String FROZEN_TAG = "droplets-test-frozen";

    static
    {
        MinecraftForge.EVENT_BUS.addListener((ThirstChangeEvent.Post event) -> lastCause = event.getCause());
        MinecraftForge.EVENT_BUS.addListener((DehydrationDamageEvent event) -> lastDehydrationDamage = event.getAmount());
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, (LivingDeathEvent event) -> {
            if (event.getEntity().getTags().contains(IMMORTAL_TAG))
                event.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, (BlockEvent.BreakEvent event) -> {
            if (event.getPlayer().getTags().contains(PROTECTED_TAG))
                event.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, (AttackEntityEvent event) -> {
            if (event.getEntity().getTags().contains(PROTECTED_TAG))
                event.setCanceled(true);
        });
        MinecraftForge.EVENT_BUS.addListener((ThirstChangeEvent.Pre event) -> {
            if (event.getEntity().getTags().contains(FROZEN_TAG))
                event.setCanceled(true);
        });
    }

    private static ItemStack waterBottle(int purity)
    {
        return WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), purity);
    }

    @GameTest(template = "empty")
    public static void emptiedHungerExhaustionStillCounts(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        double basal = GameplayConfig.BASAL_PER_TICK.get();
        try
        {
            set(GameplayConfig.BASAL_PER_TICK, 0.0);
            FoodData food = player.getFoodData();
            food.setExhaustion(0.0F);
            thirst.tick(player);
            float start = thirst.getExhaustion();
            food.setExhaustion(0.5F);
            thirst.tick(player);
            float first = thirst.getExhaustion();
            helper.assertTrue(first > start, "hunger exhaustion did not add thirst exhaustion");
            food.setExhaustion(0.25F);
            thirst.tick(player);
            helper.assertTrue(thirst.getExhaustion() > first, "thirst exhaustion went from " + first + " to " + thirst.getExhaustion() + " after the hunger exhaustion was emptied and grew again");
        }
        finally
        {
            set(GameplayConfig.BASAL_PER_TICK, basal);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void waterBottleIsDrunk(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 4, 0, ThirstChangeEvent.Cause.COMMAND);
        PlayerThirst.consume(waterBottle(PurityLevel.PURE.level()), player);
        assertValueEqual(helper, thirst.getThirst(), 10, "thirst after a pure water bottle (4 + 2 pure)");
        assertValueEqual(helper, thirst.getQuenched(), 8, "quenched after a pure water bottle (5 + 3 pure)");
        assertValueEqual(helper, lastCause, ThirstChangeEvent.Cause.DRINK, "cause of drinking a water bottle");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void onlyPureWaterGetsTheBonus(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        int pure = PurityLevel.PURE.level();
        thirst.change(player, 4, 0, ThirstChangeEvent.Cause.COMMAND);
        PlayerThirst.consume(waterBottle(PurityLevel.CLEAN.level()), player);
        assertValueEqual(helper, thirst.getThirst(), 8, "thirst after a clean water bottle (4)");
        assertValueEqual(helper, thirst.getQuenched(), 5, "quenched after a clean water bottle (5)");

        ThirstValues values = ThirstHelper.drinkValuesOf(waterBottle(pure));
        helper.assertTrue(values != null && values.thirst() == 6 && values.quenched() == 8, "values shown for a pure water bottle: " + values);
        helper.assertTrue(ThirstHelper.drinkValuesOf(waterBottle(pure)) == values, "the pure values are built again for the same stack");
        for (int purity = PurityLevel.MIN; purity < pure; purity++)
        {
            ThirstValues other = ThirstHelper.drinkValuesOf(waterBottle(purity));
            helper.assertTrue(other != null && other.thirst() == 4 && other.quenched() == 5, "values shown for a water bottle of purity " + purity + ": " + other);
        }

        thirst.change(player, 4, 0, ThirstChangeEvent.Cause.COMMAND);
        PlayerThirst.drinkWater(player, 3, 2, pure);
        assertValueEqual(helper, thirst.getThirst(), 9, "thirst after a hose sip of pure water (3 + 2)");
        assertValueEqual(helper, thirst.getQuenched(), 5, "quenched after a hose sip of pure water (2 + 3)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void drinkingABowlGivesTheEmptyBowlBack(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        ItemStack bowls = new ItemStack(DropletsItems.TERRACOTTA_WATER_BOWL.get(), 2);
        ItemStack left = bowls.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(left.is(DropletsItems.TERRACOTTA_WATER_BOWL.get()) && left.getCount() == 1, "water bowls left after drinking one of two");
        assertValueEqual(helper, player.getInventory().countItem(DropletsItems.TERRACOTTA_BOWL.get()), 1, "empty bowls in the inventory");
        ItemStack last = left.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(last.is(DropletsItems.TERRACOTTA_BOWL.get()) && last.getCount() == 1, "drinking the last water bowl leaves the empty bowl in hand");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void appleIsEaten(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 4, 0, ThirstChangeEvent.Cause.COMMAND);
        PlayerThirst.consume(new ItemStack(Items.APPLE), player);
        assertValueEqual(helper, thirst.getThirst(), 6, "thirst after an apple (2)");
        assertValueEqual(helper, lastCause, ThirstChangeEvent.Cause.EAT, "cause of eating an apple");
        helper.assertFalse(player.hasEffect(MobEffects.CONFUSION), "food rolled purity effects");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rainStopsAtFullThirst(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        ServerLevel level = helper.getLevel();
        PlayerThirst thirst = thirst(player);
        float rain = level.getRainLevel(1.0F);
        BlockPos sky = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, player.blockPosition());
        player.setPos(sky.getX(), sky.getY(), sky.getZ());
        player.setXRot(-90.0F);
        player.tickCount = GameplayConfig.RAIN_INTERVAL_TICKS.get();
        level.setRainLevel(1.0F);
        try
        {
            thirst.setThirst(20);
            thirst.setQuenched(20);
            thirst.tick(player);
            helper.assertTrue(thirst.serializeNBT().getFloat("overflow") == 0.0F, "rain at full thirst overflows");
            thirst.setThirst(10);
            thirst.setQuenched(0);
            thirst.tick(player);
            if (level.isRainingAt(player.blockPosition().above()))
                helper.assertTrue(thirst.getThirst() == 11, "rain gives a sip below full, thirst " + thirst.getThirst());
        }
        finally
        {
            level.setRainLevel(rain);
        }
        helper.succeed();
    }

    private static boolean healsWithin(ServerPlayer player, int thirst, int food, float saturation)
    {
        thirst(player).setThirst(thirst);
        player.setHealth(10.0F);
        player.getFoodData().setFoodLevel(food);
        player.getFoodData().setSaturation(saturation);
        for (int tick = 0; tick < 100 && player.getHealth() <= 10.0F; tick++)
            player.getFoodData().tick(player);
        return player.getHealth() > 10.0F;
    }

    @GameTest(template = "empty")
    public static void healingNeedsTheMinimumThirstOnBothPaths(GameTestHelper helper)
    {
        int slow = GameplayConfig.SLOW_REGEN_MIN_THIRST.get();
        int hunger = GameplayConfig.HUNGER_REGEN_MIN_THIRST.get();
        TestSupport.set(GameplayConfig.SLOW_REGEN_MIN_THIRST, GameplayConfig.SLOW_REGEN_MIN_THIRST.getDefault());
        TestSupport.set(GameplayConfig.HUNGER_REGEN_MIN_THIRST, GameplayConfig.HUNGER_REGEN_MIN_THIRST.getDefault());
        try
        {
            helper.assertTrue(healsWithin(player(helper), 16, 20, 5.0F), "saturation heals slowly at 16 thirst");
            helper.assertTrue(healsWithin(player(helper), 16, 18, 0.0F), "food heals at 16 thirst");
            helper.assertFalse(healsWithin(player(helper), 15, 20, 5.0F), "saturation does not heal at 15 thirst");
            helper.assertFalse(healsWithin(player(helper), 15, 18, 0.0F), "food does not heal at 15 thirst");
        }
        finally
        {
            TestSupport.set(GameplayConfig.SLOW_REGEN_MIN_THIRST, slow);
            TestSupport.set(GameplayConfig.HUNGER_REGEN_MIN_THIRST, hunger);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dehydrationStopsAtMinimumHealth(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        double amount = GameplayConfig.DAMAGE_AMOUNT.get();
        Difficulty difficulty = helper.getLevel().getDifficulty();
        TestSupport.set(GameplayConfig.DAMAGE_AMOUNT, 5.0);
        helper.getLevel().getServer().setDifficulty(Difficulty.NORMAL, true);
        try
        {
            thirst.setThirst(0);
            player.setHealth(3.0F);
            for (int i = 0; i < GameplayConfig.DAMAGE_INTERVAL_TICKS.get(); i++)
                thirst.tick(player);
            float expected = 3.0F - GameplayConfig.DAMAGE_MIN_HEALTH_NORMAL.get().floatValue();
            helper.assertTrue(lastDehydrationDamage == expected, "the hit stops at the minimum health, was " + lastDehydrationDamage);
        }
        finally
        {
            TestSupport.set(GameplayConfig.DAMAGE_AMOUNT, amount);
            helper.getLevel().getServer().setDifficulty(difficulty, true);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void estimatesDoNotDependOnWhichItemComesFirst(GameTestHelper helper)
    {
        RecipeManager recipes = new RecipeManager();
        recipes.replaceRecipes(List.of(
                shapeless("bread_from_apple", Items.APPLE, Items.BREAD),
                shapeless("apple_from_bread", Items.BREAD, Items.APPLE),
                shapeless("apple_from_sugar", Items.SUGAR, Items.APPLE)));
        Map<Item, int[]> known = Map.of(Items.SUGAR, new int[]{4, 2, -1});
        RecipeInference.Result result = RecipeInference.run(new RecipeInference.Inputs(known, Set.of()), recipes, helper.getLevel().registryAccess());
        helper.assertTrue(result.foods().containsKey(Items.APPLE) && result.foods().containsKey(Items.BREAD), "both items get an estimate");
        helper.assertTrue(result.foods().get(Items.BREAD)[0] == result.foods().get(Items.APPLE)[0], "the item inside the cycle keeps its value");
        helper.succeed();
    }

    private static Recipe<?> shapeless(String name, Item ingredient, Item result)
    {
        return new ShapelessRecipe(DropletsOfThirst.asResource("test/" + name), "", CraftingBookCategory.MISC, new ItemStack(result), NonNullList.of(Ingredient.EMPTY, Ingredient.of(ingredient)));
    }

    @GameTest(template = "empty")
    public static void quenchedNeverExceedsThirst(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 4, 10, ThirstChangeEvent.Cause.COMMAND);
        assertValueEqual(helper, thirst.getQuenched(), 4, "quenched set above thirst");
        thirst.change(player, 2, 2, ThirstChangeEvent.Cause.COMMAND);
        assertValueEqual(helper, thirst.getQuenched(), 2, "quenched after thirst went down");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void saltyValuesStopAtZero(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 1, 1, ThirstChangeEvent.Cause.COMMAND);
        thirst.hydrate(player, -6, -6, true, ThirstChangeEvent.Cause.EAT);
        assertValueEqual(helper, thirst.getThirst(), 0, "thirst after salty food");
        assertValueEqual(helper, thirst.getQuenched(), 0, "quenched after salty food");
        helper.assertFalse(player.hasEffect(DropletsMobEffects.OVERHYDRATED.get()), "salty food overhydrated");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void drinkingFarPastFullOverhydrates(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 20, 20, ThirstChangeEvent.Cause.COMMAND);
        thirst.hydrate(player, 6, 8, true, ThirstChangeEvent.Cause.DRINK);
        helper.assertFalse(player.hasEffect(DropletsMobEffects.OVERHYDRATED.get()), "one bottle past full already overhydrated");
        thirst.hydrate(player, 6, 8, true, ThirstChangeEvent.Cause.DRINK);
        MobEffectInstance effect = player.getEffect(DropletsMobEffects.OVERHYDRATED.get());
        helper.assertTrue(effect != null && effect.getAmplifier() == 0, "two bottles past full give Overhydrated I");
        helper.assertTrue(player.hasEffect(MobEffects.CONFUSION), "Overhydrated comes with Nausea");
        helper.assertTrue(player.getEffect(MobEffects.CONFUSION).getDuration() >= 200, "Overhydrated brings at least 10 seconds of Nausea");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledDrinksDoNotOverhydrate(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 20, 12, ThirstChangeEvent.Cause.COMMAND);
        player.addTag(FROZEN_TAG);
        for (int i = 0; i < 4; i++)
            thirst.hydrate(player, 6, 8, true, ThirstChangeEvent.Cause.DRINK);
        player.removeTag(FROZEN_TAG);
        helper.assertFalse(player.hasEffect(DropletsMobEffects.OVERHYDRATED.get()), "cancelled drinks overhydrated");
        thirst.hydrate(player, 6, 8, true, ThirstChangeEvent.Cause.DRINK);
        helper.assertFalse(player.hasEffect(DropletsMobEffects.OVERHYDRATED.get()), "one bottle past full after cancelled drinks overhydrated");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void invulnerablePlayersDoNotOverhydrate(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        player.getAbilities().invulnerable = true;
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 20, 20, ThirstChangeEvent.Cause.COMMAND);
        for (int i = 0; i < 4; i++)
            thirst.hydrate(player, 6, 8, true, ThirstChangeEvent.Cause.DRINK);
        helper.assertFalse(player.hasEffect(DropletsMobEffects.OVERHYDRATED.get()), "an invulnerable player overhydrated");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dirtyWaterGivesItsEffects(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst.consume(waterBottle(0), player);
        MobEffectInstance nausea = player.getEffect(MobEffects.CONFUSION);
        helper.assertTrue(nausea != null, "dirty water gives Nausea (100% by default)");

        helper.assertTrue(nausea.getDuration() >= 200, "dirty water gives at least 10 seconds of Nausea, not " + nausea.getDuration() + " ticks");
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "dirty water gives Hunger (100% by default)");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cleanAndPureWaterGiveNoEffects(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst.consume(waterBottle(PurityLevel.CLEAN.level()), player);
        helper.assertTrue(player.getActiveEffects().isEmpty(), "clean water gave " + player.getActiveEffects());
        PlayerThirst.consume(waterBottle(PurityLevel.PURE.level()), player);
        helper.assertTrue(player.getActiveEffects().isEmpty(), "pure water gave " + player.getActiveEffects());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void defaultEffectTablesFollowTheSixLevels(GameTestHelper helper)
    {
        double[][] chances = {{100, 100, 40}, {60, 60, 15}, {25, 25}, {5, 5}, {}, {}};
        assertValueEqual(helper, PurityConfig.EFFECTS.size(), PurityLevel.values().length, "effect lists");
        for (PurityLevel level : PurityLevel.values())
        {
            PurityEffects.Effect[] table = PurityEffects.table(level.level());
            double[] expected = chances[level.level()];
            assertValueEqual(helper, table.length, expected.length, "effects of " + level.id());
            for (int i = 0; i < table.length; i++)
            {
                helper.assertTrue(Math.abs(table[i].chance() * 100 - expected[i]) < 0.001, level.id() + " effect " + i + " chance " + table[i].chance());
                assertValueEqual(helper, table[i].blocksHydration(), i == 2, level.id() + " effect " + i + " blocks hydration");
            }
        }
        assertValueEqual(helper, PurityConfig.HOT_DIRTY_WATER_MAX_PURITY.getDefault(), PurityLevel.MURKY.level(), "hotDirtyWater.maxPurity default");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void climateFallsWithAltitude(GameTestHelper helper)
    {
        Level level = helper.getLevel();
        BlockPos low = helper.absolutePos(BlockPos.ZERO).atY(level.getSeaLevel());
        BlockPos high = low.above(200);

        var plains = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        float atSea = ExhaustionFactors.biomeClimate(level, plains, low, 1.0F, false);
        float onTop = ExhaustionFactors.biomeClimate(level, plains, high, 1.0F, false);
        helper.assertTrue(onTop < atSea, "climate multiplier 200 blocks above sea level " + onTop + ", not below " + atSea);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void theEndIsCold(GameTestHelper helper)
    {
        ServerLevel end = helper.getLevel().getServer().getLevel(Level.END);
        helper.assertTrue(end != null, "no End");
        ServerPlayer player = FakePlayerFactory.get(end, new GameProfile(UUID.randomUUID(), "droplets-test"));
        player.moveTo(0.5, 64, 0.5);
        DimensionWater water = DropletsDataMaps.DIMENSION_WATER.get(end.dimensionTypeRegistration());
        helper.assertTrue(water != null && water.thirstMultiplier().isPresent(), "the End has no thirst_multiplier");
        float climate = ExhaustionFactors.getExhaustionBiomeModifier(player);
        assertValueEqual(helper, climate, water.thirstMultiplier().get(), "climate multiplier in the End");
        helper.assertTrue(climate < 1.0F, "climate multiplier in the End " + climate + ", not below 1");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void commandsReturnTheirResult(GameTestHelper helper) throws Exception
    {
        ServerPlayer player = player(helper);
        CommandSourceStack source = player.createCommandSourceStack().withPermission(2).withSuppressedOutput();
        var dispatcher = helper.getLevel().getServer().getCommands().getDispatcher();
        assertValueEqual(helper, dispatcher.execute("droplets set @s 4 10", source), 4, "result of set");
        assertValueEqual(helper, thirst(player).getQuenched(), 4, "quenched after set 4 10");
        assertValueEqual(helper, dispatcher.execute("droplets query @s", source), 4, "result of query");
        assertValueEqual(helper, dispatcher.execute("droplets enable @s false", source), 1, "result of enable");
        helper.assertFalse(thirst(player).getShouldTickThirst(), "thirst still enabled after enable false");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void effectsRecomputeTheMultiplier(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        float plain = thirst.exhaustionModifier(player);
        helper.assertTrue(plain > 0.0F, "multiplier without effects " + plain);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 200));
        assertValueEqual(helper, thirst.exhaustionModifier(player), plain * GameplayConfig.FIRE_RESISTANCE_PERCENT.get() / 100.0F, "multiplier with Fire Resistance");
        player.removeEffect(MobEffects.FIRE_RESISTANCE);
        assertValueEqual(helper, thirst.exhaustionModifier(player), plain, "multiplier after Fire Resistance is removed");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void aCancelledDeathKeepsThirst(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 7, 3, ThirstChangeEvent.Cause.COMMAND);
        player.addTag(IMMORTAL_TAG);
        MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(player, player.damageSources().generic()));
        assertValueEqual(helper, thirst.getThirst(), 7, "thirst after a cancelled death");
        assertValueEqual(helper, thirst.getQuenched(), 3, "quenched after a cancelled death");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void respawningGivesTheRespawnValues(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        thirst.change(player, 7, 3, ThirstChangeEvent.Cause.COMMAND);
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(player, true));
        assertValueEqual(helper, thirst.getThirst(), 7, "thirst after coming back from the End");
        assertValueEqual(helper, thirst.getQuenched(), 3, "quenched after coming back from the End");
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.PlayerRespawnEvent(player, false));
        assertValueEqual(helper, thirst.getThirst(), GameplayConfig.RESPAWN_THIRST.get(), "thirst after respawning");
        assertValueEqual(helper, thirst.getQuenched(), GameplayConfig.RESPAWN_QUENCHED.get(), "quenched after respawning");
        assertValueEqual(helper, lastCause, ThirstChangeEvent.Cause.DEATH, "cause of the respawn values");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void dehydrationSparesCreativeAndDisabledPlayers(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        MobEffect dehydration = DropletsMobEffects.DEHYDRATION.get();
        float start = thirst.getExhaustion();
        dehydration.applyEffectTick(player, 0);
        helper.assertTrue(thirst.getExhaustion() > start, "Dehydration added no exhaustion to a survival player");
        float survival = thirst.getExhaustion();
        player.getAbilities().invulnerable = true;
        dehydration.applyEffectTick(player, 0);
        assertValueEqual(helper, thirst.getExhaustion(), survival, "exhaustion of a creative player with Dehydration");
        player.getAbilities().invulnerable = false;
        thirst.setShouldTickThirst(false);
        dehydration.applyEffectTick(player, 0);
        assertValueEqual(helper, thirst.getExhaustion(), survival, "exhaustion of a player with thirst disabled and Dehydration");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void cancelledActivitiesCostNothingInOwnMode(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        GameplayConfig.Mode mode = GameplayConfig.MODE.get();
        double basal = GameplayConfig.BASAL_PER_TICK.get();
        try
        {
            set(GameplayConfig.MODE, GameplayConfig.Mode.OWN);
            set(GameplayConfig.BASAL_PER_TICK, 0.0);
            BlockPos pos = helper.absolutePos(BlockPos.ZERO);
            Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, BlockPos.ZERO.above());
            thirst.tick(player);
            float start = thirst.getExhaustion();
            player.addTag(PROTECTED_TAG);
            MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), player));
            player.attack(zombie);
            thirst.tick(player);
            assertValueEqual(helper, thirst.getExhaustion(), start, "exhaustion after a cancelled block break and attack");
            player.removeTag(PROTECTED_TAG);
            MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(helper.getLevel(), pos, helper.getLevel().getBlockState(pos), player));
            thirst.tick(player);
            helper.assertTrue(thirst.getExhaustion() > start, "a block break added no exhaustion in OWN mode");
        }
        finally
        {
            set(GameplayConfig.MODE, mode);
            set(GameplayConfig.BASAL_PER_TICK, basal);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void onlyLandedAttacksCostThirstInOwnMode(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        PlayerThirst thirst = thirst(player);
        GameplayConfig.Mode mode = GameplayConfig.MODE.get();
        double basal = GameplayConfig.BASAL_PER_TICK.get();
        try
        {
            set(GameplayConfig.MODE, GameplayConfig.Mode.OWN);
            set(GameplayConfig.BASAL_PER_TICK, 0.0);
            Zombie zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, BlockPos.ZERO.above());
            zombie.setInvulnerable(true);
            thirst.tick(player);
            float start = thirst.getExhaustion();
            player.attack(zombie);
            thirst.tick(player);
            assertValueEqual(helper, thirst.getExhaustion(), start, "exhaustion after an attack on an invulnerable mob");
            zombie.setInvulnerable(false);
            player.attack(zombie);
            thirst.tick(player);
            helper.assertTrue(thirst.getExhaustion() > start, "an attack that landed added no exhaustion in OWN mode");
        }
        finally
        {
            set(GameplayConfig.MODE, mode);
            set(GameplayConfig.BASAL_PER_TICK, basal);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void rightClicksRunOnTheServer(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        BlockPos pos = helper.absolutePos(BlockPos.ZERO);
        MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, pos, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false)));
        MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickEmpty(player, InteractionHand.MAIN_HAND));
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void estimatedLineIsKeptWhileTheValuesStay(GameTestHelper helper)
    {
        Component line = PlayerThirstManager.estimatedLine(3, 2);
        assertValueEqual(helper, line, Component.translatable("droplets_of_thirst.tooltip.estimated", 3, 2).withStyle(ChatFormatting.GRAY), "line for 3 thirst and 2 quenched");
        helper.assertTrue(PlayerThirstManager.estimatedLine(3, 2) == line, "the line is built again for the same values");
        assertValueEqual(helper, PlayerThirstManager.estimatedLine(4, 2), Component.translatable("droplets_of_thirst.tooltip.estimated", 4, 2).withStyle(ChatFormatting.GRAY), "line for 4 thirst and 2 quenched");
        helper.succeed();
    }
}
