package com.darkona.dropletsofthirst.gametest;

import static com.darkona.dropletsofthirst.gametest.TestSupport.assertValueEqual;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.purity.PurityNbt;
import net.mehvahdjukaar.moonlight.api.block.ISoftFluidTankProvider;
import net.mehvahdjukaar.supplementaries.common.block.faucet.FluidOffer;
import net.mehvahdjukaar.moonlight.api.fluids.BuiltInSoftFluids;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidStack;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidTank;
import net.mehvahdjukaar.moonlight.api.fluids.forge.SoftFluidStackImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

@PrefixGameTestTemplate(false)
public class SupplementariesTests
{
    private static final BlockPos POS = new BlockPos(2, 2, 2);

    private static SoftFluidTank jar(GameTestHelper helper)
    {
        Block jar = BuiltInRegistries.BLOCK.get(new ResourceLocation("supplementaries:jar"));
        helper.setBlock(POS, jar);
        return ((ISoftFluidTankProvider) helper.getBlockEntity(POS)).getSoftFluidTank();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void conversionsKeepPurity(GameTestHelper helper)
    {
        SoftFluidStack soft = SoftFluidStackImpl.fromForgeFluid(WaterPurity.addPurity(new FluidStack(Fluids.WATER, 250), 1));
        assertValueEqual(helper, purity(soft), 1, "purity of the water inside the jar");
        assertValueEqual(helper, WaterPurity.getPurity(SoftFluidStackImpl.toForgeFluid(soft)), 1, "purity the jar handler reports");
        helper.succeed();
    }

    private static Integer purity(SoftFluidStack fluid)
    {
        return fluid.getTag() != null && fluid.getTag().contains(PurityNbt.PURITY) ? fluid.getTag().getInt(PurityNbt.PURITY) : null;
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void jarBottlesKeepPurity(GameTestHelper helper)
    {
        SoftFluidTank tank = jar(helper);
        SoftFluidStack water = SoftFluidStack.of(BuiltInSoftFluids.WATER.getHolder(), SoftFluidTank.BOTTLE_COUNT * 2, null);
        water.getOrCreateTag().putInt(PurityNbt.PURITY, 1);
        tank.setFluid(water);
        InteractionResultHolder<ItemStack> filled = tank.fillItem(new ItemStack(Items.GLASS_BOTTLE), helper.getLevel(), helper.absolutePos(POS), false);
        helper.assertTrue(filled.getResult().consumesAction(), "the jar did not fill a bottle: " + filled.getResult());
        assertValueEqual(helper, WaterPurity.getPurity(filled.getObject()), 1, "purity of a bottle filled from the jar");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void jarTakesPurityFromBottles(GameTestHelper helper)
    {
        SoftFluidTank tank = jar(helper);
        ItemStack dirty = WaterPurity.addPurity(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER), 0);
        InteractionResultHolder<ItemStack> drained = tank.drainItem(dirty, helper.getLevel(), helper.absolutePos(POS), false);
        helper.assertTrue(drained.getResult().consumesAction(), "the jar did not take the bottle: " + drained.getResult());
        assertValueEqual(helper, purity(tank.getFluid()), 0, "purity of the jar after emptying a bottle into it");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void faucetTakesPurityFromWorldWater(GameTestHelper helper) throws ReflectiveOperationException
    {
        BlockPos source = helper.absolutePos(POS);
        helper.getLevel().setBlockAndUpdate(source, Blocks.WATER.defaultBlockState());
        Object interaction = interaction("LiquidBlockInteraction");
        Method provided = interaction.getClass().getDeclaredMethod("getProvidedFluid", net.minecraft.world.level.Level.class, BlockPos.class, Direction.class, net.minecraft.world.level.material.FluidState.class);
        provided.setAccessible(true);
        FluidOffer offer = (FluidOffer) provided.invoke(interaction, helper.getLevel(), source, Direction.DOWN, helper.getLevel().getFluidState(source));
        helper.assertTrue(offer != null, "the faucet offered nothing from a water block");
        helper.assertTrue(purity(offer.fluid()) != null, "water the faucet takes from the world has no purity");
        assertValueEqual(helper, purity(offer.fluid()), WaterPurity.getWaterPurity(helper.getLevel(), source, true), "purity of faucet water");
        helper.succeed();
    }

    @GameTest(template = "empty", templateNamespace = DropletsOfThirst.ID)
    public static void faucetTakesPurityFromCauldron(GameTestHelper helper) throws ReflectiveOperationException
    {
        BlockPos cauldron = helper.absolutePos(POS);
        helper.getLevel().setBlockAndUpdate(cauldron.below(), Blocks.CAMPFIRE.defaultBlockState());
        BlockState state = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        helper.getLevel().setBlockAndUpdate(cauldron, state);
        Object interaction = interaction("WaterCauldronInteraction");
        Method provided = interaction.getClass().getDeclaredMethod("getProvidedFluid", net.minecraft.world.level.Level.class, BlockPos.class, Direction.class, BlockState.class);
        provided.setAccessible(true);
        FluidOffer offer = (FluidOffer) provided.invoke(interaction, helper.getLevel(), cauldron, Direction.DOWN, state);
        helper.assertTrue(offer != null, "the faucet offered nothing from a water cauldron");
        assertValueEqual(helper, purity(offer.fluid()), CauldronPurity.HEATED_CAULDRON_PURITY, "purity of faucet water from a heated cauldron");
        helper.succeed();
    }

    private static Object interaction(String name) throws ReflectiveOperationException
    {
        Class<?> type = Class.forName("net.mehvahdjukaar.supplementaries.common.block.faucet." + name);
        Constructor<?> constructor = type.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
