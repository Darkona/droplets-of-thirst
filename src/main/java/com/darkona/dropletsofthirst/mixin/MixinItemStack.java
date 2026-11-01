package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.config.SyncedValues;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class MixinItemStack
{
    @Shadow public abstract Item getItem();

    @Inject(method="getMaxStackSize", at = @At("HEAD"), cancellable = true)
    public void changeWaterBottleStackSize(CallbackInfoReturnable<Integer> cir)
    {
        if(getItem() != Items.POTION)
            return;
        if(WaterPurity.isWaterPotion((ItemStack) (Object) this))
            cir.setReturnValue(SyncedValues.waterBottleStackSize());
    }
}
