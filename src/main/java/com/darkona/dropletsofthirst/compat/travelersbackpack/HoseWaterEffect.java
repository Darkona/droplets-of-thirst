package com.darkona.dropletsofthirst.compat.travelersbackpack;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.PlayerThirst;
import com.darkona.dropletsofthirst.content.thirst.ThirstHelper;
import com.tiviacz.travelersbackpack.api.fluids.EffectFluid;
import com.tiviacz.travelersbackpack.fluids.EffectFluidRegistry;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

final class HoseWaterEffect extends EffectFluid
{
    private static final int AMOUNT = 250;

    private HoseWaterEffect()
    {
        super("droplets_of_thirst:water", Fluids.WATER, AMOUNT);
    }

    static void register()
    {
        EffectFluidRegistry.EFFECT_REGISTRY.remove("minecraft:water");
        new HoseWaterEffect();
    }

    @Override
    public void affectDrinker(FluidStack fluid, Level level, Entity entity)
    {
        if (level.isClientSide() || !(entity instanceof Player player))
            return;
        ItemStack bottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        PlayerThirst.drinkWater(player, ThirstHelper.getThirst(bottle), ThirstHelper.getQuenched(bottle), WaterPurity.getPurity(fluid));
    }

    @Override
    public boolean canExecuteEffect(FluidStack fluid, Level level, Entity entity)
    {
        return fluid.getAmount() >= AMOUNT;
    }
}
