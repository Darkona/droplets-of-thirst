package com.darkona.dropletsofthirst.compat.appleskin;

import com.darkona.dropletsofthirst.config.ClientConfig;
import dev.darkona.jdlib.api.hud.AppleSkinOptions;
import dev.darkona.jdlib.api.hud.HudCompat;
import net.minecraft.client.gui.screens.Screen;

public final class AppleSkinCompat
{
    private AppleSkinCompat() {}

    private static AppleSkinOptions followed()
    {
        if (!ClientConfig.FOLLOW_APPLESKIN.get())
            return null;
        AppleSkinOptions options = HudCompat.appleSkin();
        return options.present() ? options : null;
    }

    public static boolean quenchedOverlay()
    {
        AppleSkinOptions options = followed();
        return options == null || options.saturationOverlay();
    }

    public static boolean drinkPreview(boolean offhand)
    {
        AppleSkinOptions options = followed();
        return options == null || options.foodValuesOverlay(offhand);
    }

    public static boolean exhaustionUnderlay()
    {
        AppleSkinOptions options = followed();
        return options == null || options.exhaustionUnderlay();
    }

    public static boolean tooltip()
    {
        AppleSkinOptions options = followed();
        return options == null || options.foodTooltip(Screen.hasShiftDown());
    }
}
