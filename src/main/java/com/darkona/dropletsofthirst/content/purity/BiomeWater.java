package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.core.StrictFields;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record BiomeWater(Optional<Integer> base, int delta, int max)
{
    public static final Codec<BiomeWater> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StrictFields.optional(Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY), "base").forGetter(BiomeWater::base),
            StrictFields.optional(Codec.intRange(-WaterPurity.MAX_PURITY, WaterPurity.MAX_PURITY), "delta", 0).forGetter(BiomeWater::delta),
            StrictFields.optional(Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY), "max", WaterPurity.MAX_PURITY).forGetter(BiomeWater::max)
    ).apply(instance, BiomeWater::new));
}
