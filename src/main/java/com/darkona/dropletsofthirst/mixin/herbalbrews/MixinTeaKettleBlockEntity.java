package com.darkona.dropletsofthirst.mixin.herbalbrews;

import com.darkona.dropletsofthirst.compat.delight.DelightCompat;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.satisfy.herbalbrews.core.blocks.entity.TeaKettleBlockEntity;
import net.satisfy.herbalbrews.core.registry.TagsRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TeaKettleBlockEntity.class, remap = false)
public abstract class MixinTeaKettleBlockEntity
{
    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/tags/TagKey;)Z"))
    private boolean droplets_of_thirst$rejectDirtyWater(ItemStack stack, TagKey<Item> tag, Operation<Boolean> original)
    {
        return original.call(stack, tag) && !((tag == TagsRegistry.SMALL_WATER_FILL || tag == TagsRegistry.LARGE_WATER_FILL) && DelightCompat.tooDirtyForKettle(stack));
    }
}
