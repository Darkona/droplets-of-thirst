package com.darkona.dropletsofthirst.client;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.api.ThirstValues;
import com.darkona.dropletsofthirst.compat.appleskin.AppleSkinCompat;
import com.darkona.dropletsofthirst.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.thirst.ThirstHelper;
import com.darkona.dropletsofthirst.thirst.ModAttachment;
import com.darkona.dropletsofthirst.config.ClientConfig;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.darkona.jdlib.api.hud.HudFrame;
import dev.darkona.jdlib.api.hud.HudGate;
import dev.darkona.jdlib.api.hud.HudSide;
import dev.darkona.jdlib.api.hud.JDHudBars;
import dev.darkona.jdlib.api.hud.VanillaLayer;
import dev.darkona.jdlib.core.hud.ConsumablePreview;
import dev.darkona.jdlib.core.hud.VanillaIconMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class ThirstBarRenderer
{
    public static final ResourceLocation LAYER = DropletsOfThirst.asResource("thirst_level");
    public static final ResourceLocation THIRST_ICONS = DropletsOfThirst.asResource("textures/gui/thirst_icons.png");
    public static final ResourceLocation THIRST_MASK = DropletsOfThirst.asResource("textures/gui/thirst_icons_mask.png");
    public static final ResourceLocation QUENCHED_ICONS = DropletsOfThirst.asResource("textures/gui/quenched_icons.png");
    private static final ResourceLocation QUENCHED_MASK = DropletsOfThirst.asResource("textures/gui/quenched_icons_mask.png");
    public static final float SALTY_RED = 0.9f, SALTY_GREEN = 0.2f, SALTY_BLUE = 0.2f;
    private static final RandomSource random = RandomSource.create();
    private static final int ROW_HEIGHT = 10;
    private static final ConsumablePreview PREVIEW = new ConsumablePreview();
    private static int lastNotFullTick;

    private ThirstBarRenderer() {}

    public static void registerLayer()
    {
        JDHudBars.register(new JDHudBars.Spec(LAYER, VanillaLayer.FOOD_LEVEL, JDHudBars.Order.ABOVE, HudSide.RIGHT, 0,
                HudGate.standard().and(HudGate.ridingHidesVehicleHealth()), () -> true,
                ClientConfig.THIRST_BAR_X_OFFSET::get, ClientConfig.THIRST_BAR_Y_OFFSET::get, 0, ThirstBarRenderer::paint));
    }

    private static int paint(HudFrame frame)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = frame.player();
        PlayerThirst thirst = player.getData(ModAttachment.PLAYER_THIRST);
        if (player.isAlive() && !thirst.getShouldTickThirst() || shouldHideBar(minecraft, player, thirst))
            return 0;

        GuiGraphics guiGraphics = frame.graphics();
        minecraft.getProfiler().push("thirst");
        int tint = ThirstBarStyles.resolve(player);
        float red = (tint >> 16 & 255) / 255f;
        float green = (tint >> 8 & 255) / 255f;
        float blue = (tint & 255) / 255f;
        ResourceLocation fill = tint < 0 ? THIRST_ICONS : THIRST_MASK;
        ResourceLocation outline = tint < 0 ? QUENCHED_ICONS : QUENCHED_MASK;
        int right = frame.edgeX();
        int top = frame.topY();

        int ticks = minecraft.gui.getGuiTicks();
        int level = thirst.getThirst();
        int quenched = thirst.getQuenched();
        boolean shake = quenched <= 0 && VanillaIconMotion.jitters(ticks, level);
        int wave = ClientConfig.BUFF_WAVE.get() && ThirstBarStyles.waves(player) ? VanillaIconMotion.raisedIcon(ticks, PlayerThirst.MAX_THIRST) : -1;
        boolean showQuenched = ClientConfig.SHOW_QUENCHED_OVERLAY.get() && AppleSkinCompat.quenchedOverlay();

        ThirstValues gain = ClientConfig.SHOW_DRINK_PREVIEW.get() ? heldDrink(player) : null;
        if (gain != null)
            PREVIEW.show(level, quenched, PlayerThirst.MAX_THIRST, gain.thirst(), gain.quenched());
        else
            PREVIEW.clear();
        float flash = gain != null ? ConsumablePreview.pulse(ticks) : 0;
        int newLevel = PREVIEW.valueAfter();
        int newQuenched = PREVIEW.bufferAfter();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        if (ClientConfig.SHOW_EXHAUSTION_UNDERLAY.get() && AppleSkinCompat.exhaustionUnderlay())
        {
            int width = (int) (Mth.clamp(thirst.getExhaustion() / GameplayConfig.EXHAUSTION_PER_POINT.get().floatValue(), 0f, 1f) * 81);
            guiGraphics.setColor(1f, 1f, 1f, 0.75f);
            guiGraphics.blit(QUENCHED_ICONS, right - width, top, 81 - width, 18, width, 9);
        }

        for (int i = 0; i < 10; ++i)
        {
            int idx = i * 2 + 1;
            int x = right - i * 8 - 9;
            int y = shake ? top + random.nextInt(3) - 1 : top;
            if (i == wave)
                y -= VanillaIconMotion.WAVE_RISE;

            guiGraphics.setColor(1f, 1f, 1f, 1f);
            guiGraphics.blit(THIRST_ICONS, x, y, 0, 0, 9, 9, 25, 9);

            guiGraphics.setColor(red, green, blue, 1f);
            if (idx <= level)
                guiGraphics.blit(fill, x, y, idx < level ? 16 : 8, 0, 9, 9, 25, 9);
            if (showQuenched && quenched > i * 2)
                guiGraphics.blit(outline, x, y, outlineU(quenched, i), 0, 9, 9);

            if (!PREVIEW.active())
                continue;
            guiGraphics.setColor(red, green, blue, flash);
            if (PREVIEW.fillsValue(i))
                guiGraphics.blit(fill, x, y, idx < newLevel ? 16 : 8, 0, 9, 9, 25, 9);
            if (PREVIEW.fillsBuffer(i))
                guiGraphics.blit(outline, x, y, outlineU(newQuenched, i), 0, 9, 9);

            if (PREVIEW.emptiesValue(i))
            {
                guiGraphics.setColor(SALTY_RED, SALTY_GREEN, SALTY_BLUE, flash);
                guiGraphics.blit(THIRST_MASK, x, y, idx < level ? 16 : 8, 0, 9, 9, 25, 9);
                if (PREVIEW.endsValue(i))
                {
                    guiGraphics.setColor(red, green, blue, 1f);
                    guiGraphics.blit(fill, x, y, 8, 0, 9, 9, 25, 9);
                }
            }
            if (PREVIEW.emptiesBuffer(i))
            {
                guiGraphics.setColor(SALTY_RED, SALTY_GREEN, SALTY_BLUE, flash);
                guiGraphics.blit(QUENCHED_MASK, x, y, outlineU(quenched, i), 0, 9, 9);
            }
        }
        guiGraphics.setColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        minecraft.getProfiler().pop();
        return ROW_HEIGHT;
    }

    private static int outlineU(int quenched, int i)
    {
        float bar = quenched / 2f - i;
        return bar >= 1 ? 27 : bar > 0.5f ? 18 : bar > 0.25f ? 9 : 0;
    }

    private static @Nullable ThirstValues heldDrink(Player player)
    {
        ThirstValues values = drinkValues(player, player.getMainHandItem());
        if (values != null)
            return AppleSkinCompat.drinkPreview(false) ? values : null;
        return AppleSkinCompat.drinkPreview(true) ? drinkValues(player, player.getOffhandItem()) : null;
    }

    private static @Nullable ThirstValues drinkValues(Player player, ItemStack stack)
    {
        if (stack.isEmpty() || !ThirstHelper.playerRestoresThirst(stack, player))
            return null;
        FoodProperties food = stack.getFoodProperties(player);
        if (food != null && !player.canEat(food.canAlwaysEat()))
            return null;
        return ThirstHelper.drinkValuesOf(stack);
    }

    private static boolean shouldHideBar(Minecraft minecraft, Player player, PlayerThirst thirst)
    {
        if (!ClientConfig.HIDE_BAR_WHEN_FULL.get())
            return false;

        int ticks = minecraft.gui.getGuiTicks();
        if (thirst.getThirst() < 20
                || ThirstHelper.itemRestoresThirst(player.getMainHandItem())
                || ThirstHelper.itemRestoresThirst(player.getOffhandItem())
                || ticks < lastNotFullTick)
        {
            lastNotFullTick = ticks;
            return false;
        }

        return ticks - lastNotFullTick >= ClientConfig.HIDE_BAR_DELAY_TICKS.get();
    }
}
