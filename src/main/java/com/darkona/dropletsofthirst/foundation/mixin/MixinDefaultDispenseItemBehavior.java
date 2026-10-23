package com.darkona.dropletsofthirst.foundation.mixin;

import com.darkona.dropletsofthirst.purity.WaterPurity;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.DispenserBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DefaultDispenseItemBehavior.class)
public abstract class MixinDefaultDispenseItemBehavior
{
    @Inject(method = "consumeWithRemainder", at = @At("HEAD"))
    private void droplets_of_thirst$worldWaterPurity(BlockSource source, ItemStack stack, ItemStack remainder, CallbackInfoReturnable<ItemStack> cir)
    {
        if (WaterPurity.enabled() && source.state().hasProperty(DispenserBlock.FACING) && WaterPurity.isWaterFilledContainer(remainder) && !WaterPurity.hasPurity(remainder))
            WaterPurity.addPurity(remainder, WaterPurity.takenWaterPurity(source.level(), source.pos().relative(source.state().getValue(DispenserBlock.FACING))));
    }
}
