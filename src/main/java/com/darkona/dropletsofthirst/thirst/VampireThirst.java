package com.darkona.dropletsofthirst.thirst;

import com.darkona.dropletsofthirst.compat.supernatural.SupernaturalCompat;
import com.darkona.dropletsofthirst.compat.vampirism.VampirismCompat;
import com.darkona.dropletsofthirst.data.DropletsTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class VampireThirst
{
    private VampireThirst() {}

    public static boolean isVampire(Player player)
    {
        return VampirismCompat.isVampire(player) || SupernaturalCompat.isVampire(player);
    }

    public static boolean isBlood(ItemStack stack)
    {
        return stack.is(DropletsTags.BLOOD);
    }

    public static boolean canHydrate(ItemStack stack, Player player)
    {
        return !isVampire(player) || isBlood(stack);
    }
}
