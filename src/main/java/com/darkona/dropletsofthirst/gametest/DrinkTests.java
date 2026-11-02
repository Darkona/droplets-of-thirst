package com.darkona.dropletsofthirst.gametest;

import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;
import static com.darkona.dropletsofthirst.gametest.TestSupport.player;
import static com.darkona.dropletsofthirst.gametest.TestSupport.thirst;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.CanteenItem;
import com.darkona.dropletsofthirst.index.DropletsBlocks;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.darkona.dropletsofthirst.index.DropletsMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(DropletsOfThirst.ID)
@PrefixGameTestTemplate(false)
public class DrinkTests
{
    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID)
    public static void aBottleOnABubbleColumnFillsWithSodaWater(GameTestHelper helper)
    {
        BlockPos column = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(column.below(), Blocks.SOUL_SAND.defaultBlockState());
        helper.getLevel().setBlockAndUpdate(column, Blocks.BUBBLE_COLUMN.defaultBlockState());
        ServerPlayer player = player(helper);
        player.moveTo(column.getX() + 0.5, column.getY() + 1, column.getZ() + 0.5, 0.0F, 90.0F);
        ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, bottle);
        ItemStack filled = bottle.use(player.level(), player, InteractionHand.MAIN_HAND).getObject();
        helper.assertTrue(filled.is(DropletsItems.SODA_WATER.get()), "the bottle holds " + filled);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void juiceHydratesMoreThanPureWaterAndLeavesTheBottle(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        thirst(player).setThirst(0);
        ItemStack juice = new ItemStack(DropletsItems.ABC_JUICE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, juice);
        ItemStack left = juice.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(left.is(Items.GLASS_BOTTLE), "drinking left " + left);
        helper.assertTrue(thirst(player).getThirst() > 6, "thirst after the juice " + thirst(player).getThirst());
        helper.assertTrue(player.hasEffect(DropletsMobEffects.HYDRATED.get()), "the juice gives Hydrated");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void milkCuresDirtyWaterEffectsOnly(GameTestHelper helper)
    {
        ServerPlayer player = player(helper);
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200));
        player.addEffect(new MobEffectInstance(DropletsMobEffects.DEHYDRATION.get(), 200));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 200));
        ItemStack milk = new ItemStack(DropletsItems.MILK_BOTTLE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, milk);
        milk.finishUsingItem(helper.getLevel(), player);
        helper.assertFalse(player.hasEffect(MobEffects.HUNGER), "milk cures Hunger");
        helper.assertFalse(player.hasEffect(DropletsMobEffects.DEHYDRATION.get()), "milk cures Dehydration");
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SPEED), "milk keeps other effects");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void juiceNeedsDrinkableWater(GameTestHelper helper)
    {
        CraftingRecipe recipe = (CraftingRecipe) helper.getLevel().getRecipeManager().byKey(DropletsOfThirst.asResource("abc_juice")).orElseThrow();
        ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        helper.assertTrue(recipe.matches(input(water.copy()), helper.getLevel()), "plain water makes juice");
        helper.assertTrue(recipe.matches(input(WaterPurity.addPurity(water.copy(), 3)), helper.getLevel()), "acceptable water makes juice");
        helper.assertFalse(recipe.matches(input(WaterPurity.addPurity(water.copy(), 2)), helper.getLevel()), "murky water makes no juice");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void machinesFillTheCanteenWithDrinkableFluidsOnly(GameTestHelper helper)
    {
        ItemStack canteen = new ItemStack(DropletsItems.CANTEEN.get());
        IFluidHandlerItem tank = canteen.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        helper.assertTrue(tank != null, "the canteen has a fluid capability");
        assertValueEqual(helper, tank.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE), 0, "lava accepted");
        FluidStack pure = WaterPurity.addPurity(new FluidStack(Fluids.WATER, 2000), 5);
        assertValueEqual(helper, tank.fill(pure, IFluidHandler.FluidAction.EXECUTE), 2000, "water accepted");
        assertValueEqual(helper, CanteenItem.sips(canteen), 8, "sips of a full canteen");
        ServerPlayer player = player(helper);
        thirst(player).setThirst(0);
        player.setItemInHand(InteractionHand.MAIN_HAND, canteen);
        ItemStack left = canteen.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(left == canteen, "the canteen stays in the hand");
        assertValueEqual(helper, CanteenItem.sips(canteen), 7, "sips after one drink");
        helper.assertTrue(thirst(player).getThirst() >= 6, "pure water with its bonus, thirst " + thirst(player).getThirst());
        helper.succeed();
    }

    private static ItemStack craft(GameTestHelper helper, ItemStack canteen, ItemStack drink)
    {
        CraftingContainer input = crafting(2, 1, canteen, drink);
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(recipe -> {
                    helper.assertTrue(recipe.getRemainingItems(input).get(1).is(Items.GLASS_BOTTLE), "crafting returns the bottle");
                    return recipe.assemble(input, helper.getLevel().registryAccess());
                }).orElse(ItemStack.EMPTY);
    }

    @GameTest(template = "empty")
    public static void craftingFillsTheCanteenOneDrinkAtATime(GameTestHelper helper)
    {
        ItemStack canteen = new ItemStack(DropletsItems.CANTEEN.get());
        ItemStack juice = new ItemStack(DropletsItems.ABC_JUICE.get());
        for (int sips = 1; sips <= 8; sips++)
        {
            canteen = craft(helper, canteen, juice);
            assertValueEqual(helper, CanteenItem.sips(canteen), sips, "sips after crafting");
        }
        helper.assertTrue(craft(helper, canteen, juice).isEmpty(), "a ninth drink does not fit");
        CraftingContainer full = crafting(3, 3, new ItemStack(DropletsItems.CANTEEN.get()), juice, juice, juice, juice, juice, juice, juice, juice);
        ItemStack atOnce = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, full, helper.getLevel()).orElseThrow().assemble(full, helper.getLevel().registryAccess());
        assertValueEqual(helper, CanteenItem.sips(atOnce), 8, "eight drinks at once");
        CraftingContainer mixed = crafting(3, 1, new ItemStack(DropletsItems.CANTEEN.get()), juice, new ItemStack(DropletsItems.COLA.get()));
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, mixed, helper.getLevel()).isEmpty(), "mixed drinks at once");
        ItemStack water = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        helper.assertTrue(craft(helper, canteen, water).isEmpty(), "water does not mix with juice");
        helper.assertTrue(craft(helper, canteen, new ItemStack(DropletsItems.COLA.get())).isEmpty(), "one kind of drink at a time");
        ItemStack watered = craft(helper, new ItemStack(DropletsItems.CANTEEN.get()), water);
        assertValueEqual(helper, CanteenItem.sips(watered), 1, "water goes in the tank");
        helper.assertTrue(craft(helper, watered, juice).isEmpty(), "juice does not mix with water");
        CraftingContainer bread = crafting(2, 1, new ItemStack(DropletsItems.CANTEEN.get()), new ItemStack(Items.BREAD));
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, bread, helper.getLevel()).isEmpty(), "bread is no drink");
        ServerPlayer player = player(helper);
        thirst(player).setThirst(0);
        player.setItemInHand(InteractionHand.MAIN_HAND, canteen);
        canteen.finishUsingItem(helper.getLevel(), player);
        assertValueEqual(helper, CanteenItem.sips(canteen), 7, "sips after one drink");
        helper.assertTrue(thirst(player).getThirst() >= 10, "thirst from the juice " + thirst(player).getThirst());
        helper.assertTrue(player.hasEffect(DropletsMobEffects.HYDRATED.get()), "the sip gives the juice effects");
        assertValueEqual(helper, player.getInventory().countItem(Items.GLASS_BOTTLE), 0, "no bottle from the sip");
        helper.succeed();
    }

    private static ItemStack click(GameTestHelper helper, ServerPlayer player, BlockPos pos, ItemStack stack)
    {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockState state = helper.getLevel().getBlockState(pos);
        state.use(helper.getLevel(), player, InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        return player.getItemInHand(InteractionHand.MAIN_HAND);
    }

    @GameTest(template = "empty")
    public static void theAmphoraKeepsWaterWithItsPurity(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.AMPHORA.get().defaultBlockState());
        IFluidHandler tank = TestSupport.fluidHandler(helper, pos, Direction.UP);
        helper.assertTrue(tank != null, "the amphora has a fluid capability");
        helper.assertFalse(helper.getLevel().getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).isPresent(), "the amphora takes no items");
        ServerPlayer player = player(helper);
        ItemStack bottle = WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 5);
        ItemStack left = click(helper, player, pos, bottle);
        helper.assertTrue(left.is(Items.GLASS_BOTTLE), "pouring leaves the empty bottle, got " + left);
        assertValueEqual(helper, tank.getFluidInTank(0).getAmount(), 250, "water poured");
        ItemStack dirty = WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 1);
        ItemStack filled = click(helper, player, pos, new ItemStack(Items.GLASS_BOTTLE));
        helper.assertTrue(filled.is(Items.POTION), "the bottle comes back full, got " + filled);
        assertValueEqual(helper, WaterPurity.getPurity(filled), 5, "purity kept");
        helper.assertTrue(click(helper, player, pos, dirty).is(Items.GLASS_BOTTLE), "dirty water goes in");
        helper.assertTrue(click(helper, player, pos, WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 5)).is(Items.GLASS_BOTTLE), "water of another purity mixes");
        assertValueEqual(helper, tank.getFluidInTank(0).getAmount(), 500, "both waters are inside");
        assertValueEqual(helper, WaterPurity.getPurity(tank.getFluidInTank(0)), 1, "the mix takes the dirtiest purity");
        assertValueEqual(helper, tank.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 9000), 3), IFluidHandler.FluidAction.EXECUTE), 3500, "four buckets");
        helper.getLevel().destroyBlock(pos, false);
        helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.AMPHORA.get().defaultBlockState());
        helper.assertTrue(TestSupport.fluidHandler(helper, pos, Direction.UP).getFluidInTank(0).isEmpty(), "breaking it loses the water");
        helper.succeed();
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID, timeoutTicks = 200)
    public static void aBrokenAmphoraSpillsWithoutASource(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.AMPHORA.get().defaultBlockState());
        TestSupport.fluidHandler(helper, pos, Direction.UP).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.getLevel().destroyBlock(pos, false);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(helper.getLevel().getFluidState(pos).is(FluidTags.WATER), "the amphora spills water");
            helper.assertFalse(helper.getLevel().getFluidState(pos).isSource(), "the spill leaves no source");
        });
        helper.runAfterDelay(120, () -> {
            helper.assertTrue(helper.getLevel().getFluidState(pos).isEmpty(), "the spill dries up");
            helper.succeed();
        });
    }

    private static IFluidHandler amphora(GameTestHelper helper, BlockPos pos)
    {
        helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.AMPHORA.get().defaultBlockState());
        return TestSupport.fluidHandler(helper, pos, Direction.UP);
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void filtersPullFromAboveAndPurifyInStages(GameTestHelper helper)
    {
        BlockPos bottom = helper.absolutePos(new BlockPos(1, 2, 1));
        IFluidHandler out = amphora(helper, bottom);
        helper.getLevel().setBlockAndUpdate(bottom.above(), DropletsBlocks.CHARCOAL_FILTER.get().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(bottom.above(2), DropletsBlocks.CHARCOAL_FILTER.get().defaultBlockState());
        IFluidHandler in = amphora(helper, bottom.above(3));
        in.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 500), 1), IFluidHandler.FluidAction.EXECUTE);
        helper.succeedWhen(() -> {
            helper.assertFalse(out.getFluidInTank(0).isEmpty(), "no water reached the bottom yet");
            assertValueEqual(helper, WaterPurity.getPurity(out.getFluidInTank(0)), 3, "two filters purify two levels");
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void filtersIgnoreWaterWithoutPurity(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.CHARCOAL_FILTER.get().defaultBlockState());
        IFluidHandler in = amphora(helper, pos.above());
        in.fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE);
        helper.runAfterDelay(60, () -> {
            assertValueEqual(helper, in.getFluidInTank(0).getAmount(), 500, "plain water stays above");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void filtersDrainCauldronsLevelByLevel(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(1, 2, 1));
        helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.CHARCOAL_FILTER.get().defaultBlockState());
        helper.getLevel().setBlockAndUpdate(pos.above(), Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2));
        helper.succeedWhen(() -> assertValueEqual(helper, helper.getLevel().getBlockState(pos.above()).getOptionalValue(LayeredCauldronBlock.LEVEL).orElse(0), 1, "cauldron level"));
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID, timeoutTicks = 40)
    public static void anEmptyHandDropsTheAmphoraWithItsWater(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        amphora(helper, pos).fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 3000), 5), IFluidHandler.FluidAction.EXECUTE);
        ServerPlayer player = player(helper);
        player.gameMode.destroyBlock(pos);
        helper.assertTrue(helper.getLevel().isEmptyBlock(pos), "the block is gone");
        helper.runAfterDelay(3, () -> {
            ItemStack held = droppedAmphora(helper, pos);
            helper.assertTrue(held.is(DropletsItems.AMPHORA.get()), "the amphora drops, got " + held);
            helper.assertTrue(player.getInventory().isEmpty(), "it does not go straight to the inventory");
            helper.assertItemEntityNotPresent(Items.BRICK, new BlockPos(2, 2, 2), 3.0);
            helper.assertTrue(held.getUseAnimation() == UseAnim.NONE, "a carried amphora cannot be drunk from");
            helper.assertTrue(helper.getLevel().getFluidState(pos).isEmpty(), "carrying it spills nothing");
            helper.getLevel().setBlockAndUpdate(pos, DropletsBlocks.AMPHORA.get().defaultBlockState());
            BlockItem.updateCustomBlockEntityTag(helper.getLevel(), null, pos, held);
            IFluidHandler placed = TestSupport.fluidHandler(helper, pos, Direction.UP);
            helper.assertTrue(placed != null, "the amphora was placed back");
            assertValueEqual(helper, placed.getFluidInTank(0).getAmount(), 3000, "water kept");
            assertValueEqual(helper, WaterPurity.getPurity(placed.getFluidInTank(0)), 5, "purity kept");
            helper.succeed();
        });
    }

    private static ItemStack droppedAmphora(GameTestHelper helper, BlockPos pos)
    {
        for (ItemEntity item : helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3.0)))
            if (item.getItem().is(DropletsItems.AMPHORA.get()))
                return item.getItem();
        return ItemStack.EMPTY;
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID, timeoutTicks = 40)
    public static void aToolShattersTheAmphora(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        amphora(helper, pos).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        ServerPlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        player.gameMode.destroyBlock(pos);
        helper.runAfterDelay(2, () -> {
            helper.assertItemEntityNotPresent(DropletsItems.AMPHORA.get(), new BlockPos(2, 2, 2), 3.0);
            helper.assertItemEntityPresent(Items.BRICK, new BlockPos(2, 2, 2), 3.0);
            helper.assertTrue(helper.getLevel().getFluidState(pos).is(FluidTags.WATER), "the water spills");
            helper.succeed();
        });
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID, timeoutTicks = 100)
    public static void aFallingAnvilShattersTheAmphora(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        amphora(helper, pos);
        FallingBlockEntity.fall(helper.getLevel(), pos.above(3), Blocks.ANVIL.defaultBlockState());
        helper.succeedWhen(() -> helper.assertFalse(helper.getLevel().getBlockState(pos).is(DropletsBlocks.AMPHORA.get()), "the amphora still stands"));
    }

    @GameTest(template = "empty")
    public static void waterTakesTheDirtiestPurityInAnyTank(GameTestHelper helper)
    {
        FluidTank tank = new FluidTank(1000);
        tank.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 400), 5), IFluidHandler.FluidAction.EXECUTE);
        assertValueEqual(helper, tank.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 100), 1), IFluidHandler.FluidAction.SIMULATE), 100, "a simulation accepts it");
        assertValueEqual(helper, WaterPurity.getPurity(tank.getFluid()), 5, "a simulation changes nothing");
        assertValueEqual(helper, tank.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 100), 1), IFluidHandler.FluidAction.EXECUTE), 100, "dirty water goes in");
        assertValueEqual(helper, WaterPurity.getPurity(tank.getFluid()), 1, "the whole tank goes down");
        tank.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 100), 5), IFluidHandler.FluidAction.EXECUTE);
        assertValueEqual(helper, WaterPurity.getPurity(tank.getFluid()), 1, "pure water does not clean it");
        assertValueEqual(helper, tank.fill(new FluidStack(Fluids.LAVA, 100), IFluidHandler.FluidAction.EXECUTE), 0, "other fluids still do not mix");
        ItemStack canteen = new ItemStack(DropletsItems.CANTEEN.get());
        IFluidHandlerItem skin = canteen.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
        skin.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 250), 5), IFluidHandler.FluidAction.EXECUTE);
        assertValueEqual(helper, skin.fill(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 250), 2), IFluidHandler.FluidAction.EXECUTE), 250, "the canteen mixes too");
        assertValueEqual(helper, WaterPurity.getPurity(skin.getFluidInTank(0)), 2, "the canteen goes down");
        assertValueEqual(helper, CanteenItem.sips(canteen), 2, "two sips");
        helper.succeed();
    }

    private static CraftingContainer input(ItemStack water)
    {
        return crafting(2, 2, new ItemStack(Items.APPLE), new ItemStack(Items.BEETROOT), new ItemStack(Items.CARROT), water);
    }

    private static CraftingContainer crafting(int width, int height, ItemStack... stacks)
    {
        CraftingContainer container = new TransientCraftingContainer(new NoMenu(), width, height);
        for (int slot = 0; slot < stacks.length; slot++)
            container.setItem(slot, stacks[slot]);
        return container;
    }

    private static final class NoMenu extends AbstractContainerMenu
    {
        private NoMenu()
        {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int slot)
        {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player)
        {
            return false;
        }
    }
}
