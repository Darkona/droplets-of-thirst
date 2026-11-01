package com.darkona.dropletsofthirst.mixin.cold_sweat;

import com.darkona.dropletsofthirst.compat.coldsweat.ColdSweatCompat;
import com.darkona.dropletsofthirst.content.thirst.CanteenItem;
import com.darkona.dropletsofthirst.index.DropletsItems;
import com.momosoftworks.coldsweat.common.blockentity.IceboxBlockEntity;
import com.momosoftworks.coldsweat.config.ConfigSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = IceboxBlockEntity.class, remap = false)
public abstract class MixinIceboxBlockEntity
{
    @Shadow
    private boolean hasItemStacks;

    @Inject(method = "tick(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)V", at = @At("TAIL"))
    private void droplets_of_thirst$chill(Level level, BlockState state, BlockPos pos, CallbackInfo ci)
    {
        IceboxBlockEntity icebox = (IceboxBlockEntity) (Object) this;
        if (level.isClientSide() || icebox.getFuel() <= 0 || icebox.ticksExisted % (int) (20 / Math.max(1, ConfigSettings.TEMP_RATE.get())) != 0)
            return;
        for (int slot = 1; slot < 10; slot++)
            if (icebox.getItem(slot).is(DropletsItems.CANTEEN.get()) && CanteenItem.holdsWater(icebox.getItem(slot)))
                hasItemStacks |= ColdSweatCompat.warmCanteen(icebox.getItem(slot), -1.0);
    }
}
