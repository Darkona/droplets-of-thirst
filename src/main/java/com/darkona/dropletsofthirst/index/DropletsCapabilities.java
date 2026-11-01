package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DropletsCapabilities
{
    public static final Capability<PlayerThirst> PLAYER_THIRST = CapabilityManager.get(new CapabilityToken<>() {});

    public static final ResourceLocation PLAYER_THIRST_ID = DropletsOfThirst.asResource("player_thirst");

    private DropletsCapabilities() {}

    public static void register(IEventBus modBus)
    {
        modBus.addListener(DropletsCapabilities::registerCapabilities);
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, DropletsCapabilities::attach);
        MinecraftForge.EVENT_BUS.addListener(DropletsCapabilities::copyOnClone);
    }

    public static PlayerThirst thirst(Player player)
    {
        PlayerThirst thirst = player.getCapability(PLAYER_THIRST).orElse(null);
        return thirst != null ? thirst : new PlayerThirst();
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.register(PlayerThirst.class);
    }

    private static void attach(AttachCapabilitiesEvent<Entity> event)
    {
        if (!(event.getObject() instanceof Player))
            return;
        event.addCapability(PLAYER_THIRST_ID, new ThirstProvider());
    }

    private static void copyOnClone(PlayerEvent.Clone event)
    {
        Player original = event.getOriginal();
        original.reviveCaps();
        PlayerThirst old = original.getCapability(PLAYER_THIRST).orElse(null);
        PlayerThirst now = event.getEntity().getCapability(PLAYER_THIRST).orElse(null);
        if (old != null && now != null)
            now.deserializeNBT(old.serializeNBT());
        original.invalidateCaps();
    }

    private static final class ThirstProvider implements ICapabilitySerializable<CompoundTag>
    {
        private final PlayerThirst thirst = new PlayerThirst();
        private final LazyOptional<PlayerThirst> optional = LazyOptional.of(() -> thirst);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side)
        {
            return PLAYER_THIRST.orEmpty(cap, optional);
        }

        @Override
        public CompoundTag serializeNBT()
        {
            return thirst.serializeNBT();
        }

        @Override
        public void deserializeNBT(CompoundTag nbt)
        {
            thirst.deserializeNBT(nbt);
        }
    }
}
