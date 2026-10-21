package com.darkona.dropletsofthirst.compat.supernatural;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.foundation.gui.ThirstBarStyles;
import net.minecraft.world.entity.player.Player;
import dev.darkona.jdlib.api.compat.Bridges;

public final class SupernaturalCompat
{
    public static final boolean LOADED = Bridges.isLoaded("supernatural");

    private SupernaturalCompat() {}

    public static void initClient()
    {
        if (LOADED)
            ThirstBarStyles.register(DropletsOfThirst.asResource("supernatural_vampire"), SupernaturalBridge::hasVampirism, ThirstBarStyles.VAMPIRE_COLOR, ThirstBarStyles.VAMPIRE_PRIORITY);
    }

    public static boolean isVampire(Player player)
    {
        return LOADED && (player.level().isClientSide ? SupernaturalBridge.hasVampirism(player) : SupernaturalBridge.isVampire(player));
    }
}
