package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.config.SyncedValues;
import com.darkona.dropletsofthirst.content.purity.PurityTint;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.darkona.dropletsofthirst.index.DropletsDataMaps;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

public class CanteenItem extends Item
{
    public static final int SIP = 250;
    public static final int CAPACITY = 2000;
    public static final int MAX_SIPS = CAPACITY / SIP;

    private static final int DRINK_BAR_COLOR = 0xE8B03A;

    public CanteenItem()
    {
        super(new Properties().stacksTo(1));
    }

    public static ItemStack heldDrink(ItemStack canteen)
    {
        return canteen.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyOne();
    }

    public static boolean drinkable(FluidStack fluid)
    {
        return fluid.is(FluidTags.WATER) || fluid.getFluidHolder().getData(DropletsDataMaps.DRINK_FLUIDS) != null;
    }

    public static int sips(ItemStack canteen)
    {
        ItemStack drink = heldDrink(canteen);
        return drink.isEmpty() ? new CanteenFluidHandler(canteen).getFluid().getAmount() / SIP : drink.getCount();
    }

    private static void holdDrink(ItemStack canteen, ItemStack drink)
    {
        if (drink.isEmpty())
            canteen.remove(DataComponents.CONTAINER);
        else
            canteen.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(List.of(drink)));
    }

    private static ItemStack takeSip(ItemStack canteen)
    {
        ItemStack drink = heldDrink(canteen);
        if (drink.isEmpty())
            return bottled(new CanteenFluidHandler(canteen).drain(SIP, IFluidHandler.FluidAction.EXECUTE));
        holdDrink(canteen, drink.copyWithCount(drink.getCount() - 1));
        return drink.copyWithCount(1);
    }

    private static ItemStack bottled(FluidStack fluid)
    {
        ItemStack drink = fluid.is(FluidTags.WATER) ? PotionContents.createItemStack(Items.POTION, Potions.WATER)
                : new ItemStack(fluid.getFluidHolder().getData(DropletsDataMaps.DRINK_FLUIDS));
        return fluid.has(DropletsDataComponents.PURITY) ? WaterPurity.addPurity(drink, WaterPurity.getPurity(fluid)) : drink;
    }

    private static FluidStack water(int amount, int purity)
    {
        return WaterPurity.addPurity(new FluidStack(Fluids.WATER, amount), purity);
    }

    public static ItemStack emptied(ItemStack drink)
    {
        FoodProperties food = drink.getFoodProperties(null);
        if (food != null && food.usingConvertsTo().isPresent())
            return food.usingConvertsTo().get().copy();
        if (drink.is(Items.POTION))
            return new ItemStack(Items.GLASS_BOTTLE);
        ItemStack bowl = WaterPurity.emptyContainer(drink);
        return bowl.isEmpty() ? drink.getCraftingRemainingItem() : bowl;
    }

    public static boolean fill(ItemStack canteen, ItemStack drink)
    {
        CanteenFluidHandler tank = new CanteenFluidHandler(canteen);
        if (WaterPurity.isWaterFilledContainer(drink))
        {
            FluidStack water = water(SIP, WaterPurity.getPurity(drink));
            if (tank.fill(water, IFluidHandler.FluidAction.SIMULATE) < SIP)
                return false;
            tank.fill(water, IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        ItemStack held = heldDrink(canteen);
        boolean fits = held.isEmpty() ? tank.getFluid().isEmpty() : ItemStack.isSameItemSameComponents(held, drink) && held.getCount() < MAX_SIPS;
        if (!fits || !drink.is(Tags.Items.DRINKS))
            return false;
        holdDrink(canteen, drink.copyWithCount(held.getCount() + 1));
        return true;
    }

    public static boolean holdsWater(ItemStack canteen)
    {
        return new CanteenFluidHandler(canteen).getFluid().is(FluidTags.WATER);
    }

    public static boolean boil(ItemStack canteen, int cap)
    {
        FluidStack fluid = new CanteenFluidHandler(canteen).getFluid();
        if (!fluid.is(FluidTags.WATER) || !fluid.has(DropletsDataComponents.PURITY) || WaterPurity.getPurity(fluid) >= cap)
            return false;
        canteen.set(DropletsDataComponents.CANTEEN_FLUID, SimpleFluidContent.copyOf(WaterPurity.addPurity(fluid.copy(), WaterPurity.getPurity(fluid) + 1)));
        return true;
    }

    @Override
    public @NotNull InteractionResult useOn(@NotNull UseOnContext context)
    {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (player == null || level.getCapability(Capabilities.FluidHandler.BLOCK, pos, context.getClickedFace()) == null)
            return InteractionResult.PASS;
        return FluidUtil.interactWithFluidHandler(player, context.getHand(), level, pos, context.getClickedFace())
                ? InteractionResult.sidedSuccess(level.isClientSide()) : InteractionResult.PASS;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand)
    {
        ItemStack canteen = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, SyncedValues.canFillFromFlowingWater() ? ClipContext.Fluid.ANY : ClipContext.Fluid.SOURCE_ONLY);
        CanteenFluidHandler tank = new CanteenFluidHandler(canteen);
        if (hit.getType() == HitResult.Type.BLOCK && level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER) && level.mayInteract(player, hit.getBlockPos())
                && heldDrink(canteen).isEmpty() && tank.getFluid().getAmount() < CAPACITY)
        {
            if (!level.isClientSide())
            {
                tank.fill(water(CAPACITY - tank.getFluid().getAmount(), WaterPurity.takenWaterPurity(level, hit.getBlockPos())), IFluidHandler.FluidAction.EXECUTE);
                ColdSweatCompat.fillCanteen(canteen, level, hit.getBlockPos());
            }
            level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
            return InteractionResultHolder.sidedSuccess(canteen, level.isClientSide());
        }
        return sips(canteen) > 0 ? ItemUtils.startUsingInstantly(level, player, hand) : InteractionResultHolder.pass(canteen);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack canteen, @NotNull Level level, @NotNull LivingEntity entity)
    {
        if (!level.isClientSide() && sips(canteen) > 0)
        {
            if (heldDrink(canteen).isEmpty())
                ColdSweatCompat.sipCanteen(canteen, entity);
            ItemStack sip = takeSip(canteen);
            ItemStack drunk = sip.copy();
            EventHooks.onItemUseFinish(entity, drunk, 0, sip.finishUsingItem(level, entity));
            if (sips(canteen) == 0)
                ColdSweatCompat.emptyCanteen(canteen);
        }
        return canteen;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack stack, @NotNull LivingEntity user)
    {
        return 32;
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack)
    {
        return UseAnim.DRINK;
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack canteen)
    {
        return !heldDrink(canteen).isEmpty() || !new CanteenFluidHandler(canteen).getFluid().isEmpty();
    }

    @Override
    public int getBarWidth(@NotNull ItemStack canteen)
    {
        ItemStack drink = heldDrink(canteen);
        int amount = drink.isEmpty() ? new CanteenFluidHandler(canteen).getFluid().getAmount() : drink.getCount() * SIP;
        return Mth.ceil(13.0F * amount / CAPACITY);
    }

    @Override
    public int getBarColor(@NotNull ItemStack canteen)
    {
        FluidStack fluid = new CanteenFluidHandler(canteen).getFluid();
        return fluid.has(DropletsDataComponents.PURITY) ? PurityTint.colorOf(WaterPurity.getPurity(fluid)) & 0xFFFFFF : DRINK_BAR_COLOR;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack canteen, @NotNull TooltipContext context, @NotNull List<Component> lines, @NotNull TooltipFlag flag)
    {
        ItemStack drink = heldDrink(canteen);
        FluidStack fluid = new CanteenFluidHandler(canteen).getFluid();
        if (drink.isEmpty() && fluid.isEmpty())
            return;
        Component name = drink.isEmpty() ? fluid.getHoverName() : drink.getHoverName();
        lines.add(Component.translatable("item.droplets_of_thirst.canteen.sips", name, sips(canteen)).withStyle(ChatFormatting.GRAY));
        if (fluid.has(DropletsDataComponents.PURITY))
            lines.add(Component.translatable(PurityLevel.values()[WaterPurity.getPurity(fluid)].translationKey()).withStyle(ChatFormatting.GRAY));
        double temperature = ColdSweatCompat.canteenTemperature(canteen);
        if (temperature != 0)
            lines.add(Component.translatable(temperature > 0 ? "item.droplets_of_thirst.canteen.hot" : "item.droplets_of_thirst.canteen.cold").withStyle(temperature > 0 ? ChatFormatting.RED : ChatFormatting.AQUA));
    }
}
