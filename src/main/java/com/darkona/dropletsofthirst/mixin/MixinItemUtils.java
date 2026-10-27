package com.darkona.dropletsofthirst.mixin;

import com.darkona.dropletsofthirst.content.purity.CauldronPurity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemUtils.class)
public abstract class MixinItemUtils
{
    @Inject(method = "createFilledResult(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/item/ItemStack;", at = @At("HEAD"))
    private static void droplets_of_thirst$cauldronPurity(ItemStack emptyStack, Player player, ItemStack filledStack, boolean preventDuplicates, CallbackInfoReturnable<ItemStack> cir)
    {
        CauldronPurity.applyTo(filledStack);
    }
}
