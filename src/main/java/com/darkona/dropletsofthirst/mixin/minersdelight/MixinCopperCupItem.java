package com.darkona.dropletsofthirst.mixin.minersdelight;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.sugar.Local;
import com.sammy.minersdelight.content.item.CopperCupItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = CopperCupItem.class, remap = false)
public abstract class MixinCopperCupItem
{
    @ModifyArg(method = "use", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemUtils;createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0), index = 2)
    private ItemStack droplets_of_thirst$worldWaterPurity(ItemStack filled, @Local(argsOnly = true) Level level, @Local(ordinal = 0) BlockPos pos)
    {
        if (!level.isClientSide() && WaterPurity.enabled() && WaterPurity.isWaterFilledContainer(filled) && !WaterPurity.hasPurity(filled))
            WaterPurity.addPurity(filled, WaterPurity.takenWaterPurity(level, pos));
        return filled;
    }
}
