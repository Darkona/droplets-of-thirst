package com.darkona.dropletsofthirst.content.purity;

import com.darkona.dropletsofthirst.core.StrictFields;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Optional;

public record DimensionWater(Optional<Integer> base, Optional<Float> thirstMultiplier)
{
    public static final Codec<DimensionWater> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StrictFields.optional(Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY), "base").forGetter(DimensionWater::base),
            StrictFields.optional(Codec.floatRange(0.0F, 10.0F), "thirst_multiplier").forGetter(DimensionWater::thirstMultiplier)
    ).apply(instance, DimensionWater::new));
}
