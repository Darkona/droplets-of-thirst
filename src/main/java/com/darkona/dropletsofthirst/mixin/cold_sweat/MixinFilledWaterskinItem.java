package com.darkona.dropletsofthirst.mixin.cold_sweat;

import com.darkona.dropletsofthirst.purity.ThirstComponent;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.momosoftworks.coldsweat.common.item.FilledWaterskinItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FilledWaterskinItem.class, remap = false)
public abstract class MixinFilledWaterskinItem
{
    @ModifyReturnValue(method = "getCraftingRemainingItem", at = @At("RETURN"))
    private ItemStack droplets_of_thirst$dropPurity(ItemStack empty)
    {
        if (!(empty.getItem() instanceof FilledWaterskinItem))
            empty.remove(ThirstComponent.PURITY);
        return empty;
    }
}
