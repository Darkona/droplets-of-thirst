package com.darkona.dropletsofthirst.compat.coldsweat;

import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.CanteenItem;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.momosoftworks.coldsweat.api.event.common.item.WaterskinEvent;
import com.momosoftworks.coldsweat.api.temperature.modifier.FoodTempModifier;
import com.momosoftworks.coldsweat.api.temperature.modifier.WaterskinTempModifier;
import com.momosoftworks.coldsweat.api.util.placement.Placement;
import com.momosoftworks.coldsweat.api.util.Temperature;
import com.momosoftworks.coldsweat.api.util.placement.Matcher;
import com.momosoftworks.coldsweat.common.item.WaterskinItem;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import com.momosoftworks.coldsweat.core.init.ModItemComponents;
import com.momosoftworks.coldsweat.core.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.neoforged.neoforge.common.NeoForge;

final class ColdSweatBridge
{
    private static final double CANTEEN_LIMIT = 50.0;

    private ColdSweatBridge() {}

    static void listen()
    {
        NeoForge.EVENT_BUS.addListener(ColdSweatBridge::filled);
    }

    private static void filled(WaterskinEvent.Fill event)
    {
        Level level = event.getLevel();
        if (level.isClientSide())
            return;
        BlockPos pos = event.getSourcePos();
        int purity = event.getSource().has(DropletsDataComponents.PURITY) ? WaterPurity.getPurity(event.getSource())
                : level.getBlockState(pos).getBlock() instanceof AbstractCauldronBlock ? CauldronPurity.at(level, pos)
                : WaterPurity.getBlockPurity(level, pos);
        event.setResultWaterskin(WaterPurity.addPurity(event.getResultWaterskin(), purity));
    }

    static void fillCanteen(ItemStack canteen, Level level, BlockPos pos)
    {
        Double temperature = WaterskinItem.getFilledItem(new ItemStack(ModItems.WATERSKIN.value()), level, pos).get(ModItemComponents.WATER_TEMPERATURE);
        if (temperature != null)
            canteen.set(ModItemComponents.WATER_TEMPERATURE, temperature);
    }

    static double canteenTemperature(ItemStack canteen)
    {
        return canteen.getOrDefault(ModItemComponents.WATER_TEMPERATURE, 0.0);
    }

    static boolean warmCanteen(ItemStack canteen, double delta)
    {
        double temperature = canteenTemperature(canteen);
        double warmed = Mth.clamp(temperature + delta, -CANTEEN_LIMIT, CANTEEN_LIMIT);
        if (warmed == temperature)
            return false;
        canteen.set(ModItemComponents.WATER_TEMPERATURE, warmed);
        return true;
    }

    static void sipCanteen(ItemStack canteen, LivingEntity entity)
    {
        double amount = canteenTemperature(canteen) * ConfigSettings.WATERSKIN_CONSUME_STRENGTH.get() / 50.0;
        if (amount != 0)
            Temperature.addModifier(entity, new WaterskinTempModifier(amount / (100.0 * CanteenItem.MAX_SIPS)).expires(100), Temperature.Trait.CORE, Placement.LAST);
    }

    static void emptyCanteen(ItemStack canteen)
    {
        canteen.remove(ModItemComponents.WATER_TEMPERATURE);
    }

    static boolean isWaterskin(ItemStack stack)
    {
        return stack.is(ModItems.FILLED_WATERSKIN);
    }

    static boolean isEmptyWaterskin(ItemStack stack)
    {
        return stack.is(ModItems.WATERSKIN);
    }

    static void cool(Player player, double amount, int ticks)
    {
        if (ticks <= 0)
        {
            Temperature.add(player, Temperature.Trait.CORE, -amount);
            return;
        }
        FoodTempModifier modifier = new FoodTempModifier(-amount);
        modifier.getNBT().putString("item", "droplets_of_thirst:water");
        modifier.expires(ticks);
        Temperature.replaceOrAddModifier(player, modifier, Temperature.Trait.BASE, Matcher.EQUALS);
    }
}
