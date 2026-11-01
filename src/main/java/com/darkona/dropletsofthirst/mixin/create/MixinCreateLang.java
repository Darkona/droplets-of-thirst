package com.darkona.dropletsofthirst.mixin.create;

import com.darkona.dropletsofthirst.content.purity.WaterPurity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.FluidTags;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CreateLang.class, remap = false)
public abstract class MixinCreateLang
{
    @ModifyReturnValue(method = "fluidName", at = @At("RETURN"))
    private static LangBuilder droplets_of_thirst$purityBeforeName(LangBuilder name, FluidStack fluid)
    {
        if (!WaterPurity.enabled() || !WaterPurity.hasPurity(fluid) && !fluid.getFluid().is(FluidTags.WATER))
            return name;
        int purity = WaterPurity.getPurity(fluid);
        return CreateLang.builder().add(Component.empty()
                .append(Component.literal(WaterPurity.getPurityText(purity) + " ").withStyle(style -> style.withColor(WaterPurity.getPurityColor(purity))))
                .append(name.component()));
    }
}
