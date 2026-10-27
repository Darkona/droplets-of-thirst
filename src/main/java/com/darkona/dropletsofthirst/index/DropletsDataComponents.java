package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.config.PurityConfig;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.mojang.serialization.Codec;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DropletsDataComponents {
    private static final DeferredRegister<DataComponentType<?>> DR = DeferredRegister
            .create(Registries.DATA_COMPONENT_TYPE, DropletsOfThirst.ID);

    private static final int UNLOADED_DEFAULT_PURITY = PurityLevel.ACCEPTABLE.level();

    private DropletsDataComponents() {
    }

    public static void register(IEventBus modBus) {
        DR.register(modBus);
    }

    public static final DataComponentType<Integer> PURITY = register("purity",
            builder -> builder.persistent(Codec.INT.xmap(DropletsDataComponents::bounded, Function.identity()))
                    .networkSynchronized(ByteBufCodecs.VAR_INT.map(DropletsDataComponents::bounded, Function.identity())));

    public static final DataComponentType<Integer> LEGACY_PURITY = register("legacy_purity",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static int bounded(int purity)
    {
        if (purity >= WaterPurity.MIN_PURITY && purity <= WaterPurity.MAX_PURITY)
            return purity;
        return PurityConfig.SPEC.isLoaded() ? WaterPurity.defaultPurity() : UNLOADED_DEFAULT_PURITY;
    }

    private static <T> DataComponentType<T> register(String name, Consumer<DataComponentType.Builder<T>> customizer) {
        var builder = DataComponentType.<T>builder();
        customizer.accept(builder);
        var componentType = builder.build();
        DR.register(name, () -> componentType);
        return componentType;
    }
}
