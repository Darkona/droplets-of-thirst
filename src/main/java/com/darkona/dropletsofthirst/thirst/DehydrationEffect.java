package com.darkona.dropletsofthirst.thirst;

import com.darkona.dropletsofthirst.config.GameplayConfig;
import com.darkona.dropletsofthirst.index.DropletsAttachmentTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class DehydrationEffect extends MobEffect {
    public DehydrationEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            float amount = (float) (PlayerThirst.HUNGER_EXHAUSTION_PER_LEVEL * (amplifier + 1) * GameplayConfig.DEHYDRATION_MULTIPLIER.get());
            player.getData(DropletsAttachmentTypes.PLAYER_THIRST).addScaledExhaustion(player, amount);
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
