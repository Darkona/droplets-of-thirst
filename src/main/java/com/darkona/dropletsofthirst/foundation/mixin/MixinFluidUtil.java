package com.darkona.dropletsofthirst.foundation.mixin;

import com.darkona.dropletsofthirst.purity.PouredWater;
import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FluidUtil.class, remap = false)
public abstract class MixinFluidUtil
{
    @ModifyReturnValue(method = "tryPlaceFluid(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/core/BlockPos;Lnet/neoforged/neoforge/fluids/capability/IFluidHandler;Lnet/neoforged/neoforge/fluids/FluidStack;)Z", at = @At("RETURN"))
    private static boolean droplets_of_thirst$registerPouredWater(boolean placed, @Nullable Player player, Level level, InteractionHand hand, BlockPos pos, IFluidHandler source, FluidStack resource)
    {
        if (placed && level != null && !level.isClientSide() && resource.is(FluidTags.WATER))
            PouredWater.poured(level, pos, WaterPurity.getPurity(resource));
        return placed;
    }
}
