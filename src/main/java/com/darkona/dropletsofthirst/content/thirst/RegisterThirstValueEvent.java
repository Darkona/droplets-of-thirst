package com.darkona.dropletsofthirst.content.thirst;

import com.darkona.dropletsofthirst.content.purity.ContainerWithPurity;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.Event;

import java.util.List;
import java.util.Map;

@Deprecated
@SuppressWarnings("unused")
public class RegisterThirstValueEvent extends Event {
    private final Map<Item, int[]> drinks;
    private final Map<Item, int[]> foods;
    private final List<ContainerWithPurity> containers;

    public RegisterThirstValueEvent(Map<Item, int[]> drinks, Map<Item, int[]> foods, List<ContainerWithPurity> containers){
        this.drinks = drinks;
        this.foods = foods;
        this.containers = containers;
    }

    public void addFood(Item item, int thirst, int quenched)
    {
        foods.putIfAbsent(item, new int[]{thirst, quenched, -1});
    }

    public void addDrink(Item item, int thirst, int quenched)
    {
        drinks.putIfAbsent(item, new int[]{thirst, quenched, -1});
    }

    public void addContainer(ContainerWithPurity container){
        containers.add(container);
    }

    public void addContainer(Item item){
        containers.add(new ContainerWithPurity(item));
    }
}
