package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import static com.darkona.dropletsofthirst.gametest.TestSupport.player;

import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;

@PrefixGameTestTemplate(false)
public class MinersDelightTests
{
    private static final int CONTAMINATED = PurityLevel.CONTAMINATED.level();

    static Item item(String id)
    {
        return BuiltInRegistries.ITEM.get(new ResourceLocation("miners_delight:" + id));
    }

    private static ItemStack useFromAbove(ServerPlayer player, BlockPos water, ItemStack stack)
    {
        player.moveTo(water.getX() + 0.5, water.getY() + 1, water.getZ() + 0.5, 0.0F, 90.0F);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        return stack.use(player.level(), player, InteractionHand.MAIN_HAND).getObject();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void waterCupIsAPurityContainer(GameTestHelper helper)
    {
        helper.assertTrue(WaterPurity.isWaterFilledContainer(new ItemStack(item("water_cup"))), "the water cup is not a water container");
        helper.assertFalse(WaterPurity.isWaterFilledContainer(new ItemStack(item("copper_cup"))), "the empty copper cup is a water container");
        helper.assertFalse(WaterPurity.isWaterFilledContainer(new ItemStack(item("milk_cup"))), "the milk cup is a water container");
        helper.succeed();
    }

    @GameTest(template = "box", templateNamespace = DropletsOfThirst.ID)
    public static void cupFilledFromTheWorldTakesItsPurity(GameTestHelper helper)
    {
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        helper.getLevel().setBlockAndUpdate(pos, Blocks.WATER.defaultBlockState());
        int expected = WaterPurity.getBlockPurity(helper.getLevel(), pos);

        ItemStack cup = useFromAbove(player(helper), pos, new ItemStack(item("copper_cup")));
        helper.assertTrue(cup.is(item("water_cup")), "the copper cup was not filled: " + cup);
        helper.assertTrue(WaterPurity.hasPurity(cup), "a cup filled from the world has no purity");
        assertValueEqual(helper, WaterPurity.getPurity(cup), expected, "purity of a cup filled from world water");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void cupFilledFromACauldronTakesItsPurity(GameTestHelper helper)
    {
        BlockPos rel = new BlockPos(1, 2, 1);
        helper.setBlock(rel, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        BlockPos pos = helper.absolutePos(rel);
        ServerPlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item("copper_cup")));
        helper.getBlockState(rel).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        ItemStack cup = player.getMainHandItem();
        helper.assertTrue(cup.is(item("water_cup")), "the copper cup was not filled at the cauldron: " + cup);
        helper.assertTrue(WaterPurity.hasPurity(cup), "a cup filled from a cauldron has no purity");
        assertValueEqual(helper, WaterPurity.getPurity(cup), CauldronPurity.at(helper.getLevel(), pos), "purity of a cup filled from a cauldron");
        helper.assertTrue(CauldronInteraction.WATER.containsKey(item("copper_cup")), "no cauldron interaction for the copper cup");
        helper.succeed();
    }
}
