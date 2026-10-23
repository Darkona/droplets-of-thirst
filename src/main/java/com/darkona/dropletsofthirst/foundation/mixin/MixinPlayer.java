package com.darkona.dropletsofthirst.foundation.mixin;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.thirst.PlayerThirstManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer
{
    @Inject(method = "eat", at = @At("HEAD"))
    public void onEatDrink(Level level, ItemStack food, FoodProperties foodProperties, CallbackInfoReturnable<ItemStack> cir)
    {
        Player player = (Player) (Object) this;
        if (!WaterPurity.isWaterFilledContainer(food))
            PlayerThirst.consume(food, player);
    }

    @Inject(method = "attack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;causeFoodExhaustion(F)V"))
    private void droplets_of_thirst$attackLanded(Entity target, CallbackInfo ci)
    {
        if ((Object) this instanceof ServerPlayer player)
            PlayerThirstManager.attackLanded(player);
    }

}