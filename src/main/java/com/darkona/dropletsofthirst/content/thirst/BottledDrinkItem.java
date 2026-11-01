package com.darkona.dropletsofthirst.content.thirst;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import org.jetbrains.annotations.NotNull;

public class BottledDrinkItem extends Item
{
    public BottledDrinkItem(FoodProperties food)
    {
        super(new Properties().stacksTo(16).food(food));
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack)
    {
        return UseAnim.DRINK;
    }

    @Override
    public @NotNull SoundEvent getEatingSound()
    {
        return SoundEvents.GENERIC_DRINK;
    }
}
