package com.darkona.dropletsofthirst.compat.reliquary;

import com.darkona.dropletsofthirst.foundation.config.CompatConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import dev.darkona.jdlib.api.compat.Bridges;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

public final class ReliquaryCompat
{
    public static final boolean LOADED = Bridges.isLoaded("reliquary");
    public static final ResourceLocation EMPEROR_CHALICE = ResourceLocation.fromNamespaceAndPath("reliquary", "emperor_chalice");

    private ReliquaryCompat() {}

    public static void init()
    {
        if (LOADED)
            NeoForge.EVENT_BUS.addListener(ReliquaryCompat::cooldownAfterDrinking);
    }

    private static void cooldownAfterDrinking(LivingEntityUseItemEvent.Finish event)
    {
        int cooldown = CompatConfig.RELIQUARY_EMPEROR_CHALICE_COOLDOWN.get();
        ItemStack item = event.getItem();
        if (cooldown > 0 && event.getEntity() instanceof ServerPlayer player && BuiltInRegistries.ITEM.getKey(item.getItem()).equals(EMPEROR_CHALICE))
            player.getCooldowns().addCooldown(item.getItem(), cooldown);
    }
}
