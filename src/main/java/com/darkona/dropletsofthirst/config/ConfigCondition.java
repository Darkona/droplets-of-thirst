package com.darkona.dropletsofthirst.config;

import com.mojang.serialization.MapCodec;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.NotNull;

public record ConfigCondition(BooleanSupplier flag) implements ICondition
{
    private static final Map<BooleanSupplier, MapCodec<ConfigCondition>> CODECS = new HashMap<>();

    public static MapCodec<ConfigCondition> codec(BooleanSupplier flag)
    {
        return CODECS.computeIfAbsent(flag, key -> MapCodec.unit(new ConfigCondition(key)).stable());
    }

    @Override
    public boolean test(ICondition.@NotNull IContext context)
    {
        return flag.getAsBoolean();
    }

    @Override
    public @NotNull MapCodec<? extends ICondition> codec()
    {
        return CODECS.get(flag);
    }
}
