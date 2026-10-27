package com.darkona.dropletsofthirst.purity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record BiomeWater(Optional<Integer> base, int delta, int max)
{
    public static final Codec<BiomeWater> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("base").forGetter(BiomeWater::base),
            Codec.intRange(-WaterPurity.MAX_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("delta", 0).forGetter(BiomeWater::delta),
            Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("max", WaterPurity.MAX_PURITY).forGetter(BiomeWater::max)
    ).apply(instance, BiomeWater::new));
}
