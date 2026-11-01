package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.content.purity.PurityEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class MilkBottleItem extends BottledDrinkItem
{
    public MilkBottleItem(FoodProperties food)
    {
        super(food);
    }

    @Override
    public @NotNull ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level level, @NotNull LivingEntity entity)
    {
        if (!level.isClientSide())
            PurityEffects.cure(entity);
        return super.finishUsingItem(stack, level, entity);
    }
}
