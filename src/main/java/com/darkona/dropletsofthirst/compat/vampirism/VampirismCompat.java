package com.darkona.dropletsofthirst.compat.vampirism;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.config.CompatConfig;
import com.darkona.dropletsofthirst.content.thirst.ThirstBarStyles;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import dev.darkona.jdlib.api.compat.Bridges;

public final class VampirismCompat
{
    public static final boolean LOADED = Bridges.isLoaded("vampirism");

    private VampirismCompat() {}

    public static void init()
    {
        if (LOADED)
            VampirismBridge.init();
    }

    public static void initClient()
    {
        if (LOADED)
            ThirstBarStyles.register(DropletsOfThirst.asResource("vampirism_vampire"), VampirismBridge::isVampire, ThirstBarStyles.VAMPIRE_COLOR, ThirstBarStyles.VAMPIRE_PRIORITY);
    }

    public static boolean isVampire(Player player)
    {
        return LOADED && VampirismBridge.isVampire(player);
    }

    static void bloodDrunk(Player player, ItemStack source, int blood, float saturation)
    {
        PlayerThirst.drinkBlood(player, source, (float) (blood * CompatConfig.VAMPIRISM_THIRST_PER_BLOOD.get()),
                (float) (blood * saturation * CompatConfig.VAMPIRISM_QUENCHED_PER_BLOOD.get()));
    }
}
