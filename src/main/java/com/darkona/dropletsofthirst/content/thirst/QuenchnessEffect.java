package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.api.event.ThirstChangeEvent;
import com.darkona.dropletsofthirst.index.DropletsCapabilities;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class QuenchnessEffect extends MobEffect {
    public QuenchnessEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player player && VampireThirst.canHydrate(ItemStack.EMPTY, player))
            DropletsCapabilities.thirst(player).hydrate(player, amplifier + 1, amplifier + 1, false, ThirstChangeEvent.Cause.DRINK);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % GameplayConfig.QUENCHNESS_INTERVAL_TICKS.get() == 0;
    }
}
