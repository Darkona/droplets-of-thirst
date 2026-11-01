package com.darkona.dropletsofthirst.mixin.supplementaries;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.index.DropletsDataComponents;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.api.fluids.FluidOffer;
import net.mehvahdjukaar.moonlight.api.fluids.MLBuiltinSoftFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.mehvahdjukaar.supplementaries.common.block.faucet.LiquidBlockInteraction", remap = false)
public abstract class MixinLiquidBlockInteraction
{
    @ModifyReturnValue(
            method = "getProvidedFluid(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Lnet/mehvahdjukaar/moonlight/api/fluids/FluidOffer;",
            at = @At("RETURN"),
            remap = false)
    private FluidOffer droplets_of_thirst$addPurity(FluidOffer offer, Level level, BlockPos pos, Direction direction, FluidState source)
    {
        if (offer != null && WaterPurity.enabled() && offer.fluid().is(MLBuiltinSoftFluids.WATER) && !offer.fluid().getComponents().has(DropletsDataComponents.PURITY))
            offer.fluid().set(DropletsDataComponents.PURITY, WaterPurity.getWaterPurity(level, pos, source.isSource()));
        return offer;
    }
}
