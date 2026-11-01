package com.darkona.dropletsofthirst.content.thirst;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
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

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity)
    {
        ItemStack left = super.finishUsingItem(stack, level, entity);
        if (left.isEmpty())
            return new ItemStack(Items.GLASS_BOTTLE);
        if (entity instanceof Player player && !player.getAbilities().instabuild && !player.getInventory().add(new ItemStack(Items.GLASS_BOTTLE)))
            player.drop(new ItemStack(Items.GLASS_BOTTLE), false);
        return left;
    }
}
