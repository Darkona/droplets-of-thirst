package com.darkona.dropletsofthirst.network.client;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.util.RandomSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import com.darkona.dropletsofthirst.network.HandDrankMessage;
import com.darkona.dropletsofthirst.network.DrinkByHandMessage;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.darkona.dropletsofthirst.config.SyncedValues;
import com.darkona.dropletsofthirst.network.PlayerThirstSyncMessage;
import com.darkona.dropletsofthirst.network.ThirstValuesSyncMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientPayloadHandlers
{
    private static final int SPLASH_COUNT = 6;
    private static final double SPLASH_SPREAD_XZ = 0.15;
    private static final double SPLASH_SPREAD_Y = 0.05;
    private static final double SPLASH_SPEED = 0.1;

    private ClientPayloadHandlers() {}

    public static void thirst(PlayerThirstSyncMessage message)
    {
        Player player = Minecraft.getInstance().player;
        if (player == null)
            return;
        PlayerThirst cap = DropletsCapabilities.thirst(player);
        cap.setThirst(message.thirst());
        cap.setHydration(message.hydration());
        cap.setExhaustion(message.exhaustion());
        cap.setSyncedRules(message.flags());
    }

    public static void values(ThirstValuesSyncMessage message)
    {
        ThirstHelper.useServerTables(message.drinks(), message.foods(), message.containers(), message.estimated());
        SyncedValues.useServerValues(message.defaultPurity(), message.waterBottleStackSize(), message.purityEnabled(), message.canFillFromFlowingWater(),
                message.pureThirstBonus(), message.pureHydrationBonus());
    }

    public static void handDrank(HandDrankMessage message)
    {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && level.getEntity(message.playerId()) instanceof Player drinker)
            splash(level, drinker);
    }

    private static void splash(ClientLevel level, Player drinker)
    {
        BlockHitResult hit = DrinkByHandMessage.pickFluid(drinker, ClipContext.Fluid.ANY);
        if (hit.getType() != HitResult.Type.BLOCK || !level.getFluidState(hit.getBlockPos()).is(FluidTags.WATER))
            return;
        RandomSource random = level.getRandom();
        for (int i = 0; i < SPLASH_COUNT; i++)
            level.addParticle(ParticleTypes.SPLASH,
                    hit.getLocation().x + random.nextGaussian() * SPLASH_SPREAD_XZ,
                    hit.getLocation().y + random.nextGaussian() * SPLASH_SPREAD_Y,
                    hit.getLocation().z + random.nextGaussian() * SPLASH_SPREAD_XZ,
                    random.nextGaussian() * SPLASH_SPEED,
                    random.nextGaussian() * SPLASH_SPEED,
                    random.nextGaussian() * SPLASH_SPEED);
    }
}
