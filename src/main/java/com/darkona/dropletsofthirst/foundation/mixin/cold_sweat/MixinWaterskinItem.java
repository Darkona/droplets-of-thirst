package com.darkona.dropletsofthirst.foundation.mixin.cold_sweat;

import com.darkona.dropletsofthirst.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.momosoftworks.coldsweat.common.item.WaterskinItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = WaterskinItem.class, remap = false)
public abstract class MixinWaterskinItem
{
    @Unique
    private static int droplets_of_thirst$tankPurity = -1;

    @WrapOperation(method = "lambda$useOn$0", at = @At(value = "INVOKE", target = "Lcom/momosoftworks/coldsweat/common/item/WaterskinItem;handleFillWaterskin(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/core/BlockPos;)V"))
    private static void droplets_of_thirst$fromTank(Player player, ItemStack empty, InteractionHand hand, BlockPos pos, Operation<Void> original, @Local(ordinal = 1) FluidStack drained)
    {
        if (player.level().isClientSide())
        {
            original.call(player, empty, hand, pos);
            return;
        }
        droplets_of_thirst$tankPurity = WaterPurity.getPurity(drained);
        try
        {
            original.call(player, empty, hand, pos);
        }
        finally
        {
            droplets_of_thirst$tankPurity = -1;
        }
    }

    @ModifyReturnValue(method = "getFilledItem", at = @At("RETURN"))
    private static ItemStack droplets_of_thirst$purity(ItemStack filled, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos)
    {
        if (level.isClientSide())
            return filled;
        int purity = droplets_of_thirst$tankPurity >= 0 ? droplets_of_thirst$tankPurity
                : level.getBlockState(pos).getBlock() instanceof AbstractCauldronBlock ? WaterPurity.cauldronPurity(level, pos)
                : WaterPurity.getBlockPurity(level, pos);
        return WaterPurity.addPurity(filled, purity);
    }
}
