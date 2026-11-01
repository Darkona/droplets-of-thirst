package com.darkona.dropletsofthirst.index;

import com.darkona.dropletsofthirst.content.thirst.BottledDrinkItem;
import com.darkona.dropletsofthirst.content.thirst.CanteenFluidHandler;
import com.darkona.dropletsofthirst.content.thirst.CanteenItem;
import com.darkona.dropletsofthirst.content.thirst.MilkBottleItem;
import com.darkona.dropletsofthirst.content.thirst.TerracottaBowlItem;
import com.darkona.dropletsofthirst.content.thirst.DrinkableItem;
import com.darkona.dropletsofthirst.DropletsOfThirst;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class DropletsItems {
    private static final DeferredRegister.Items ITEMS;
    public static final DeferredItem<Item> CLAY_BOWL;
    public static final DeferredItem<Item>  TERRACOTTA_BOWL;
    public static final DeferredItem<Item>  TERRACOTTA_WATER_BOWL;
    public static final DeferredItem<Item> APPLE_JUICE;
    public static final DeferredItem<Item> MELON_JUICE;
    public static final DeferredItem<Item> BERRY_JUICE;
    public static final DeferredItem<Item> ABC_JUICE;
    public static final DeferredItem<Item> SODA_WATER;
    public static final DeferredItem<Item> COLA;
    public static final DeferredItem<Item> MILK_BOTTLE;
    public static final DeferredItem<Item> CANTEEN;
    public static final DeferredItem<BlockItem> AMPHORA;
    public static final DeferredItem<BlockItem> CHARCOAL_FILTER;

    private static FoodProperties.Builder bottled(int nutrition) {
        return new FoodProperties.Builder().nutrition(nutrition).saturationModifier(0.2F).alwaysEdible().usingConvertsTo(Items.GLASS_BOTTLE);
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        modBus.addListener(DropletsItems::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(Capabilities.FluidHandler.ITEM, (stack, context) -> new CanteenFluidHandler(stack), CANTEEN.get());
    }

    static {
        ITEMS = DeferredRegister.createItems(DropletsOfThirst.ID);
        CLAY_BOWL = ITEMS.register("clay_bowl", () -> new Item((new Item.Properties())
                .stacksTo(64)
        ));
        TERRACOTTA_BOWL = ITEMS.register("terracotta_bowl", () -> new TerracottaBowlItem((new Item.Properties())
                .stacksTo(64)
        ));
        TERRACOTTA_WATER_BOWL = ITEMS.register("terracotta_water_bowl", () -> (new DrinkableItem())
                .setContainer(TERRACOTTA_BOWL.get())
        );
        APPLE_JUICE = ITEMS.register("apple_juice", () -> new BottledDrinkItem(bottled(2)
                .effect(() -> new MobEffectInstance(DropletsMobEffects.HYDRATED, 1200), 1.0F).build()));
        MELON_JUICE = ITEMS.register("melon_juice", () -> new BottledDrinkItem(bottled(2)
                .effect(() -> new MobEffectInstance(DropletsMobEffects.HYDRATED, 2400), 1.0F).build()));
        BERRY_JUICE = ITEMS.register("berry_juice", () -> new BottledDrinkItem(bottled(2)
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400), 1.0F).build()));
        ABC_JUICE = ITEMS.register("abc_juice", () -> new BottledDrinkItem(bottled(4)
                .effect(() -> new MobEffectInstance(DropletsMobEffects.HYDRATED, 3600), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100), 1.0F).build()));
        SODA_WATER = ITEMS.register("soda_water", () -> new BottledDrinkItem(bottled(0)
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300), 1.0F).build()));
        COLA = ITEMS.register("cola", () -> new BottledDrinkItem(bottled(1)
                .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800), 1.0F)
                .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 800), 1.0F).build()));
        MILK_BOTTLE = ITEMS.register("milk_bottle", () -> new MilkBottleItem(bottled(1).build()));
        CANTEEN = ITEMS.register("canteen", CanteenItem::new);
        AMPHORA = ITEMS.registerSimpleBlockItem(DropletsBlocks.AMPHORA, new Item.Properties().stacksTo(1));
        CHARCOAL_FILTER = ITEMS.registerSimpleBlockItem(DropletsBlocks.CHARCOAL_FILTER);
    }
}