package com.darkona.dropletsofthirst.compat.appleskin;

import com.darkona.dropletsofthirst.config.ClientConfig;
import net.minecraftforge.fml.ModList;

public final class AppleSkinCompat
{
    public static final boolean LOADED = ModList.get().isLoaded("appleskin");

    private AppleSkinCompat() {}

    private static boolean following()
    {
        return LOADED && ClientConfig.FOLLOW_APPLESKIN.get();
    }

    public static boolean quenchedOverlay()
    {
        return !following() || AppleSkinBridge.saturationOverlay();
    }

    public static boolean drinkPreview(boolean offhand)
    {
        return !following() || AppleSkinBridge.foodValuesOverlay(offhand);
    }

    public static boolean exhaustionUnderlay()
    {
        return !following() || AppleSkinBridge.exhaustionUnderlay();
    }

    public static boolean tooltip()
    {
        return !following() || AppleSkinBridge.foodTooltip();
    }
}
