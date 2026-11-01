package com.darkona.dropletsofthirst.mixin.cold_sweat;

import com.darkona.dropletsofthirst.api.PurityLevel;
import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.darkona.dropletsofthirst.content.thirst.CanteenItem;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.momosoftworks.coldsweat.common.blockentity.BoilerBlockEntity;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BoilerBlockEntity.class, remap = false)
public abstract class MixinBoilerBlockEntity
{
    @Unique
    private static final int droplets_of_thirst$CAP = PurityLevel.ACCEPTABLE.level();

    @Shadow
    protected boolean hasDrinkables;

    @Shadow
    protected boolean hasWaterskins;

    @Unique
    private static boolean droplets_of_thirst$boil(ItemStack stack)
    {
        return stack.is(DropletsItems.CANTEEN.get()) ? CanteenItem.boil(stack, droplets_of_thirst$CAP) : WaterPurity.boil(stack, droplets_of_thirst$CAP);
    }

    @Unique
    private static boolean droplets_of_thirst$dirty(ItemStack stack)
    {
        return droplets_of_thirst$boil(stack.copy());
    }

    @Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void droplets_of_thirst$purify(Level level, BlockState state, BlockPos pos, CallbackInfo ci)
    {
        BoilerBlockEntity boiler = (BoilerBlockEntity) (Object) this;
        if (level.isClientSide() || boiler.getFuel() <= 0)
            return;
        if (boiler.ticksExisted % (int) (20 / Math.max(1, ConfigSettings.TEMP_RATE.get())) == 0)
            for (int slot = 1; slot < 10; slot++)
                if (boiler.getItem(slot).is(DropletsItems.CANTEEN.get()) && CanteenItem.holdsWater(boiler.getItem(slot)))
                    hasWaterskins |= ColdSweatCompat.warmCanteen(boiler.getItem(slot), 1.0);
        if (boiler.ticksExisted % (200 / Math.max(1, ConfigSettings.TEMP_RATE.get())) != 0)
            return;
        boolean boiled = false;
        for (int slot = 1; slot < 10; slot++)
            boiled |= droplets_of_thirst$boil(boiler.getItem(slot));
        if (boiled)
            boiler.setChanged();
        droplets_of_thirst$watch(boiler);
    }

    @Inject(method = "checkForItems", at = @At("TAIL"))
    private void droplets_of_thirst$findDirtyWater(CallbackInfo ci)
    {
        droplets_of_thirst$watch((BoilerBlockEntity) (Object) this);
    }

    @Unique
    private void droplets_of_thirst$watch(BoilerBlockEntity boiler)
    {
        for (int slot = 1; slot < 10; slot++)
            hasDrinkables |= droplets_of_thirst$dirty(boiler.getItem(slot));
    }
}
