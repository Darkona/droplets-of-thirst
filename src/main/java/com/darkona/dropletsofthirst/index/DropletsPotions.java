package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.DropletsOfThirst;
import com.darkona.dropletsofthirst.config.GameplayConfig;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class DropletsPotions
{
    private static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, DropletsOfThirst.ID);
    public static final RegistryObject<Potion> QUENCHNESS_POTION = POTIONS.register("quenchness", () -> new Potion("quenchness", new MobEffectInstance(DropletsMobEffects.QUENCHNESS.get(), 900)));
    public static final RegistryObject<Potion> LONG_QUENCHNESS_POTION = POTIONS.register("long_quenchness", () -> new Potion("quenchness", new MobEffectInstance(DropletsMobEffects.QUENCHNESS.get(), 1800)));
    public static final RegistryObject<Potion> STRONG_QUENCHNESS_POTION = POTIONS.register("strong_quenchness", () -> new Potion("quenchness", new MobEffectInstance(DropletsMobEffects.QUENCHNESS.get(), 450, 1)));

    private DropletsPotions() {}

    public static void register(IEventBus modBus)
    {
        POTIONS.register(modBus);
    }

    public static void registerBrewing()
    {
        BrewingRecipeRegistry.addRecipe(new QuenchnessBrewing());
    }
    private static final class QuenchnessBrewing implements IBrewingRecipe {
        @Override
        public boolean isInput(ItemStack input) {
            Item item = input.getItem();
            if (item != Items.POTION && item != Items.SPLASH_POTION && item != Items.LINGERING_POTION)
                return false;
            Potion potion = PotionUtils.getPotion(input);
            return potion == Potions.AWKWARD || potion == QUENCHNESS_POTION.get();
        }

        @Override
        public boolean isIngredient(ItemStack ingredient) {
            return ingredient.is(Items.PRISMARINE_CRYSTALS) || ingredient.is(Items.REDSTONE) || ingredient.is(Items.GLOWSTONE_DUST);
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (!GameplayConfig.QUENCHNESS_POTION.get() || !isInput(input))
                return ItemStack.EMPTY;
            Potion potion = PotionUtils.getPotion(input);
            Potion result = null;
            if (potion == Potions.AWKWARD && ingredient.is(Items.PRISMARINE_CRYSTALS))
                result = QUENCHNESS_POTION.get();
            else if (potion == QUENCHNESS_POTION.get() && ingredient.is(Items.REDSTONE))
                result = LONG_QUENCHNESS_POTION.get();
            else if (potion == QUENCHNESS_POTION.get() && ingredient.is(Items.GLOWSTONE_DUST))
                result = STRONG_QUENCHNESS_POTION.get();
            return result == null ? ItemStack.EMPTY : PotionUtils.setPotion(new ItemStack(input.getItem()), result);
        }
    }
}
