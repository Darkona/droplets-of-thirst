package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.purity.PurityTint;
import com.darkona.dropletsofthirst.config.ClientConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemColors.class)
public class MixinItemColors{

    @ModifyReturnValue(method = "getColor", at = @At("RETURN"))
    private int tintWaterByPurity(int original, ItemStack stack, int tintIndex){
        return ClientConfig.TINT_WATER_BY_PURITY.get() ? PurityTint.color(stack, tintIndex, original) : original;
    }
}
