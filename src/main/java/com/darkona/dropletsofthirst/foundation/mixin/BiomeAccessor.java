package com.darkona.dropletsofthirst.foundation.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public interface BiomeAccessor
{
    @Invoker("getTemperature")
    float droplets_of_thirst$getTemperature(BlockPos pos);
}
