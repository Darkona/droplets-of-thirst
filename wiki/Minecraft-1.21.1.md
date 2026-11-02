# Minecraft 1.21.1

Droplets of Thirst for Minecraft 1.21.1 runs on **Minecraft 1.21.1 and NeoForge 21.1.252 or newer** (branch `1.21.1`, jar `droplets-of-thirst-1.21.1-1.1.0.jar`). The rest of this wiki describes this version. This page has what belongs to it: the config keys and defaults for other mods, the built-in datapacks, KubeJS and the API version. What each mod does for the player is on [Compatibility](Compatibility).

---

## Configuration

### `compat.toml`

| Key | Default | Meaning |
|---|---|---|
| `create.sandFilterFiltrationAmount` | `2` | Purity levels gained in a Sand Filter (0-5) |
| `create.sandFilterMbPerTick` | `10` | Millibuckets filtered per tick (1-1000) |
| `create.sandFilterMaxPurity` | `5` | Highest purity a Sand Filter raises water to (0-5). Purer water passes unchanged |
| `create.openEndedPipePurity` | `true` | Water an open pipe end takes from the world or a cauldron keeps its purity. `false` = it reads as `defaultPurity` |
| `coldsweat.useBodyTemperature` | `true` | The climate factor comes from Cold Sweat's body temperature instead of the biome |
| `coldsweat.bodyTemperatureCurve` | `["-100,0.8", "0,1.0", "50,1.3", "100,2.0", "150,3.0"]` | `"bodyTemperature,multiplier"` points in ascending order (Cold Sweat units: 0 comfortable, 100 burning, -100 freezing), times `depletion.multiplier` |
| `coldsweat.drinkCooling` | `0.0` | How much drinking water cools the body, in Cold Sweat units. `0` = off. Cold Sweat's own waterskin is left alone |
| `coldsweat.drinkCoolingTicks` | `0` | `0`: the body cools once and drifts back. More: the base temperature drops for that many ticks, like Cold Sweat's cold foods |
| `delight.kettleMinPurity` | `2` | Lowest purity kettles take (0-5): the HerbalBrews tea kettle, the Brewery brewing stations and any block in `droplets_of_thirst:rejects_dirty_water`. `0` = any water |
| `delight.worldPurityWaterSources` | `true` | Taps and sinks of Extra Delight, the Let's Do sink and the Farm & Charm timber well give water with the world's purity. `false` = no purity, read as `defaultPurity` |
| `reliquary.emperorChaliceCooldown` | `0` | Ticks before the Emperor's Chalice can be used again after a drink. `0` = no cooldown |
| `vampirism.thirstPerBlood` | `1.0` | Thirst per point of blood drunk (0-20). `1.0` = a full blood refill fills the thirst bar |
| `vampirism.hydrationPerBlood` | `1.0` | Hydration per point of blood, times the blood's saturation (0.3 poor to 1.0 rich; a blood bottle is 0.45) |

`sereneseasons.*` does nothing while Cold Sweat is installed.

### Other files

| Key | Default | Meaning |
|---|---|---|
| `purity.toml` `hotDirtyWater.useColdSweat` | `true` | With Cold Sweat installed, its body temperature also counts as hot |
| `purity.toml` `hotDirtyWater.coldSweatMinBodyTemp` | `50.0` | Cold Sweat body temperature above which the player is hot (-150 to 150) |
| `client.toml` `Bar Colors.vampire` | `#B3121B` | Droplet colour for vampires (Vampirism, Supernatural). It wins over every other colour |
| `gameplay.toml` `effects.quenchnessPotion` | `true` | Turns the brewing mixes on or off. Keep the same value on the server and the clients |

---

## Modpack makers

### Defaults with other mods

| Tag | Default on 1.21.1 |
|---|---|
| `droplets_of_thirst:purity_containers` (item) | Create builder's tea, Collector's Reap teas, Cold Sweat filled waterskin, Miner's Delight water cup |
| `droplets_of_thirst:salty` (item) | The four vanilla items, plus the salty, pickled and strong alcoholic items of the Delight addons |
| `droplets_of_thirst:blood` (item) | `#supernatural:blood`. The only items that hydrate a vampire (Vampirism, Supernatural), with their drinks data map values |
| `droplets_of_thirst:carries_purity` (fluid) | `#minecraft:water`, Create tea. Fluids made in a Create basin from water keep the water's purity |
| `droplets_of_thirst:rejects_dirty_water` (block) | Brewery wooden, copper and netherite brewing stations. Blocks filled by clicking with water that boil it: water below `delight.kettleMinPurity` is refused |
| `droplets_of_thirst:pauses_thirst` (mob_effect) | Farmer's Delight Nourishment, Let's Do Bakery Stuffed, Let's Do Brewery Saturated |

The `droplets_of_thirst:drinks` data map ships entries for the food mods on [Compatibility](Compatibility#farmers-delight-and-its-addons), Create, Cold Sweat and Reliquary, each with a `neoforge:mod_loaded` condition.

On 1.21.1 the tag folders are singular: `tags/item/`, `tags/block/`, `tags/fluid/`, `tags/mob_effect/`.

### Built-in datapacks

| Pack id | Default | Recipes |
|---|---|---|
| `mod/droplets_of_thirst:datapacks/purify_smelting` | enabled | Furnace: two levels up per cook, up to clean (4) with Create and up to pure (5) without it |
| `mod/droplets_of_thirst:datapacks/purify_campfire` | enabled | Campfire: one level up per cook, with the same caps |
| `mod/droplets_of_thirst:datapacks/purify_smoking` | disabled | Smoker: as the furnace, twice as fast |
| `mod/droplets_of_thirst:datapacks/purify_cooking_pot` | enabled | Farmer's Delight cooking pot and Miner's Delight copper pot: one level up per cook, with the campfire caps, for bottles, buckets, terracotta bowls and, with Cold Sweat, waterskins. Loads only with Farmer's Delight |
| `mod/droplets_of_thirst:datapacks/clean_water_cooking` | enabled | Recipes of Farmer's Delight addons that use water accept only water of a minimum purity: 2 for boiled water, 3 for cold. It replaces the addon recipes under their own ids (`extradelight:vat/kimchi_item`, …), each loaded only with its mod |

- Recipes that change with Create have ids ending in `_with_create` (result capped at 4, loads only with Create) or `_without_create` (loads only without Create, reaches 5), as in `water_bottle_from_smelting_to_clean_with_create`. The `pure` recipes exist only without Create and have no suffix. The `purify_cooking_pot` recipes end in `_manual_only`.
- The Create recipes are in `droplets_of_thirst:compat/create/...`: fan washing (`create:splashing`), heated mixing (`create:mixing`, `heat_requirement: heated`), cactus compacting and the Item Drain for terracotta bowls. They load only with Create and with purity on. `droplets_of_thirst:compat/create/cactus_without_purity` loads with purity off and gives plain water.
- The cooking packs also purify Cold Sweat's filled waterskin, with recipes that load only with Cold Sweat. A waterskin filled with Droplets of Thirst installed always has a purity.

---

---

## KubeJS

Modpacks can use the API from KubeJS 7 scripts, with no Java. Load the classes with `Java.loadClass` and listen to the events with `NativeEvents.onEvent`. Working scripts are in [`examples/kubejs`](https://github.com/Darkona/droplets-of-thirst/tree/1.21.1/examples/kubejs) on the `1.21.1` branch, tested with KubeJS 2101.7.2.

```js
var DropletsAPI = Java.loadClass('com.darkona.dropletsofthirst.api.DropletsAPI')
var PurityLevel = Java.loadClass('com.darkona.dropletsofthirst.api.PurityLevel')

// Read and change thirst (server scripts)
ItemEvents.rightClicked('minecraft:clock', event => {
  let thirst = DropletsAPI.view(event.player)
  event.player.tell(`Thirst ${thirst.thirst()}/${thirst.maxThirst()}`)
  if (event.player.shiftKeyDown) DropletsAPI.setThirst(event.player, 20)
})

// Hydrate when eating
ItemEvents.foodEaten('minecraft:golden_carrot', event => {
  if (event.entity.isPlayer()) DropletsAPI.drink(event.entity, 4, 2)
})

// Events: nested classes are loaded with a $
var DrinkPre = Java.loadClass('com.darkona.dropletsofthirst.api.event.DrinkEvent$Pre')
NativeEvents.onEvent(DrinkPre, event => {
  if (event.purity <= PurityLevel.DIRTY.level() && event.entity.level.dimension.toString() == 'minecraft:the_nether')
    event.canceled = true
})

// Register a drink in a startup script, after the item exists
StartupEvents.postInit(() => {
  DropletsAPI.registerDrink(Item.of('kubejs:lemonade').item, 6, 4, PurityLevel.CLEAN.level())
})
```

- Use `getItemPurity`, `getFluidPurity`, `withItemPurity` and `withFluidPurity` instead of the `getPurity` and `withPurity` overloads. KubeJS cannot choose between an `ItemStack` and a `FluidStack` and fails with "the choice of Java method is ambiguous".
- All scripts of one type share their top level `const` and `let`, so declare the loaded classes with `var` when several files load the same one.
- Registered drinks belong in `startup_scripts` (`StartupEvents.postInit`, when the items exist). `items.toml`, the `droplets_of_thirst:drinks` data map and `#droplets_of_thirst:no_thirst` still win over them.
- Exhaustion modifiers take a JavaScript function and a string id: `DropletsAPI.registerExhaustionModifier('kubejs:night', (player, multiplier) => player.level.isNight() ? multiplier * 0.75 : multiplier)`.
- In KubeJS, `entity.level` and `level.dimension` are properties, not methods.

---

---

## Mod developers

- Fluids go through `IFluidHandler` and `FluidStack`. A water cauldron seen through NeoForge's fluid capability reports its water with the cauldron purity.
- `EatEvent` is also posted when another mod calls `Player#eat` directly.
- The vampire bar style has priority 400.
- The Maven version is `1.21.1-1.1.0`. Declare the dependency in `neoforge.mods.toml` with `type = "optional"`.
