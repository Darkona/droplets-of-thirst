package com.darkona.dropletsofthirst.gametest;

import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;

final class TestSupport
{
    private TestSupport() {}

    static ServerPlayer player(GameTestHelper helper)
    {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "droplets-test"));
        player.moveTo(helper.absoluteVec(Vec3.ZERO));
        return player;
    }

    static <T> void assertValueEqual(GameTestHelper helper, T actual, T expected, String name)
    {
        if (!Objects.equals(actual, expected))
            throw new GameTestAssertException("Expected " + name + " to be " + expected + ", but was " + actual);
    }

    static <T> void set(ForgeConfigSpec.ConfigValue<T> value, T newValue)
    {
        try
        {
            if (CACHED_VALUE == null)
            {
                Field field = ForgeConfigSpec.ConfigValue.class.getDeclaredField("cachedValue");
                field.setAccessible(true);
                CACHED_VALUE = field;
            }
            CACHED_VALUE.set(value, newValue);
        }
        catch (ReflectiveOperationException e)
        {
            throw new IllegalStateException("Cannot override config value " + value.getPath(), e);
        }
    }

    private static Field CACHED_VALUE;

    static @Nullable IFluidHandler fluidHandler(GameTestHelper helper, BlockPos pos, @Nullable Direction side)
    {
        BlockEntity entity = helper.getLevel().getBlockEntity(pos);
        return entity == null ? null : entity.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
    }

    static PlayerThirst thirst(ServerPlayer player)
    {
        return DropletsCapabilities.thirst(player);
    }
}
