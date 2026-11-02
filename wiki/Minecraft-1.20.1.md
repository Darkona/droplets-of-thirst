# Minecraft 1.20.1

Droplets of Thirst for Minecraft 1.20.1 is one jar, `droplets-of-thirst-1.20.1-1.1.0.jar` (branch `1.20.1`), that runs on **Forge 47.1.3 or newer** and on **NeoForge 47.1**. The rest of this wiki describes 1.21.1. Thirst, water purity, effects, config files, data maps and commands work the same here. This page lists what is different. What each mod does is on [Compatibility](Compatibility), which marks what is missing on 1.20.1.

Some mods need a newer Forge than 47.1 on their own (Serene Seasons and GlitchCore ask for 47.3), so they do not run on NeoForge 47.1.

---

## Players

- Thirst Was Taken or Thirst Was Reclaimed next to Droplets of Thirst stops the game at the loading screen. Tough As Nails, Legendary Survival Overhaul, Homeostatic and Survive only show a warning, and both thirst systems run.
- A vanilla bucket that a full dispenser drops after filling comes out with no purity. Buckets that go back into the dispenser keep it.

---

## Configuration

The config files have the same keys as on 1.21.1 (see [Minecraft 1.21.1](Minecraft-1.21.1#configuration)). Two keys behave differently:

- `gameplay.toml` `loot.enabled` is checked on every loot roll, so a change applies at once, without `/reload`.
- `gameplay.toml` `effects.quenchnessPotion` is read each time a potion brews.

---

## Modpack makers

### Purity is NBT

Purity on items and fluids is the NBT key `droplets_of_thirst:purity` (0-5), the same name as the component of later versions:

```
/give @s minecraft:potion{Potion:"minecraft:water","droplets_of_thirst:purity":5}
```

### Data

- **Pack format**: `"pack_format": 15`. Folders are plural, as in vanilla 1.20.1: `recipes/`, `loot_tables/`, `tags/items/`, `tags/blocks/`, `tags/fluids/`. Effect tags stay in `tags/mob_effect/`.
- **Data maps**: `droplets_of_thirst:drinks`, `hydrating_blocks`, `biome_water` and `dimension_water` read the same files as on 1.21.1 (`data/<namespace>/data_maps/...`), with the same fields, `replace`, `remove` and tags. Conditions on an entry can be `neoforge:conditions` or `forge:conditions`, with the Forge condition types (`mod_loaded`, `not`, `and`, `or`, `item_exists`, `tag_empty`, `true`, `false`).
- **Biome tags**: the default `biome_water` uses the 1.20.1 tags `#forge:is_desert`, `#forge:is_swamp`, `#forge:is_snowy`, `#forge:is_mountain` and the vanilla `#minecraft:is_jungle`, `#minecraft:is_taiga`, `#minecraft:is_badlands`, `#minecraft:is_mountain`. There is no windswept tag, so it lists the four windswept biomes by id. The purity of every vanilla biome is the same as on 1.21.1.
- **Recipes**: a purity ingredient is `{"type": "forge:partial_nbt", "item": "minecraft:potion", "nbt": {"Potion": "minecraft:water", "droplets_of_thirst:purity": 3}}`. Water with no purity stored is `{"type": "forge:nbt", "item": "minecraft:potion", "nbt": {"Potion": "minecraft:water"}}` (for a bucket or a bowl, `forge:nbt` without `nbt`). Results take `"nbt"`, and recipe conditions go in `"conditions"` with `forge:` types, for example `{"type": "droplets_of_thirst:purity_enabled"}` or `{"type": "forge:not", "value": {"type": "forge:mod_loaded", "modid": "create"}}`.

  ```json
  {
    "conditions": [{ "type": "droplets_of_thirst:purity_enabled" }],
    "type": "minecraft:smelting",
    "ingredient": { "type": "forge:partial_nbt", "item": "minecraft:potion", "nbt": { "Potion": "minecraft:water", "droplets_of_thirst:purity": 0 } },
    "result": { "item": "minecraft:potion", "count": 1, "nbt": { "Potion": "minecraft:water", "droplets_of_thirst:purity": 2 } },
    "cookingtime": 200
  }
  ```

- **Create fluid ingredients** match NBT as "at least these keys": `{"fluid": "minecraft:water", "amount": 250, "nbt": {"droplets_of_thirst:purity": 3}}`.
- **Brewing**: the Potion of Quenchness is brewed by a Forge brewing recipe in code, not data.
- **Chest loot**: the modifiers are `droplets_of_thirst:add_table`, listed in `data/forge/loot_modifiers/global_loot_modifiers.json`, with the loot condition `droplets_of_thirst:loot_config`. Drinks of optional mods use the loot entry type `droplets_of_thirst:optional_item`, which gives nothing when the item does not exist. The purity of a loot bottle is a `minecraft:set_nbt` function: `{"function": "minecraft:set_nbt", "tag": "{\"droplets_of_thirst:purity\":3}", "conditions": [{"condition": "droplets_of_thirst:purity_enabled"}]}`.
- **Attributes**: items carry `droplets_of_thirst:thirst_drain` modifiers in their `AttributeModifiers` NBT. Enchantments are not data in 1.20.1, so a datapack cannot give them the attribute.
- **Cauldrons**: pumps and pipes of other mods cannot drain a water cauldron. Buckets, bottles, bowls and Create open pipe ends can.
- The built-in datapacks have the same ids as on 1.21.1: `mod/droplets_of_thirst:datapacks/<name>`.

---

## Mod developers

- The API has the same classes and methods, with Forge types: events are `@Cancelable` Forge events on `MinecraftForge.EVENT_BUS`, `FluidStack` is `net.minecraftforge.fluids.FluidStack`, names use `ResourceLocation`, and `DropletsAPI.registerWaveEffect` takes a `MobEffect`.
- Declare the dependency in `mods.toml` with `mandatory = false` (FML 47 has no `type`).
- The thirst bar is the GUI overlay `droplets_of_thirst:thirst_level`, right above `food_level`. Cancel it with `RenderGuiOverlayEvent.Pre`.
- The Maven version is `1.20.1-1.1.0`.
