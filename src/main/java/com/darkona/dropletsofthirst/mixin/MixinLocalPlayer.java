package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public class MixinLocalPlayer{

    @ModifyExpressionValue(method = "hasEnoughFoodToStartSprinting", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/food/FoodData;getFoodLevel()I"))
    private int hasEnoughThirstToStartSprinting(int food){
        PlayerThirst thirst = DropletsCapabilities.thirst((LocalPlayer) (Object) this);
        if(!thirst.isSprintBlocked() || thirst.getThirst() > thirst.sprintMinThirst())
            return food;
        return 0;
    }
}
