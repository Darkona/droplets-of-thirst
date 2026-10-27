package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

public record DrinkValues(int thirst, int quenched, Optional<Integer> purity)
{
    public static final Codec<DrinkValues> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(-PlayerThirst.MAX_THIRST, PlayerThirst.MAX_THIRST).fieldOf("thirst").forGetter(DrinkValues::thirst),
            Codec.intRange(-PlayerThirst.MAX_THIRST, Integer.MAX_VALUE).fieldOf("quenched").forGetter(DrinkValues::quenched),
            Codec.intRange(WaterPurity.MIN_PURITY, WaterPurity.MAX_PURITY).optionalFieldOf("purity").forGetter(DrinkValues::purity)
    ).apply(instance, DrinkValues::new));
}
