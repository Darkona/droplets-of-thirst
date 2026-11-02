# Compatibility

What Droplets of Thirst does with each mod it works with, on Minecraft 1.21.1. Every mod here is optional, and each integration switches on when its mod is installed. Config keys are in [Minecraft 1.21.1](Minecraft-1.21.1#configuration) and [Configuration](Configuration), tags and data in [Modpack makers](Modpack-Makers). Links to the mods are at the [end](#links).

Minimum versions when installed: 
 - Create 6.0.6 
 - Mekanism 10.7.19 
 - Immersive Engineering 12.4.2
 - Supplementaries 4.0.2
 - Moonlight 3.7.2
 - Cold Sweat 2.4.4
 - Serene Seasons 10.1
 - Traveler's Backpack 10.1
 - Reliquary 2.0
 - JEI 19.21, EMI 1.1, Jade 15.10.6
 - Farmer's Delight 1.3
   - Brewin' and Chewin' 4.5, 
   - Extra Delight 2.6.5
   - Farm & Charm 1.1.26
   - HerbalBrews 1.1.4
   - Brewery 2.1
   - Ocean's Delight 1.0
   - Ender's Delight 1.3
   - Fruits Delight 1.2.14
   - Cultural Delights 0.18
   - Expanded Delight 0.1.4
   - Miner's Delight 1.4
   - Corn Delight 1.2.10
   - Rustic Delight 1.7
   - Crabber's Delight 1.3
   - End's Delight 2.6
   - My Nether's Delight 1.10.

---

## Technical mods

### Create

- **Pure water needs a filter.** With Create installed, cooking water stops at clean (4). Pure water (5) comes from the Sand Filter or the Charcoal Filter.
- **Sand Filter.** Pump water through it and it comes out purer, two levels per filter. Filters in a row facing the same way pass water on without pipes. Keys: `create.sandFilterFiltrationAmount`, `create.sandFilterMbPerTick`, `create.sandFilterMaxPurity`.
- **Fans:** washing raises water bottles and terracotta water bowls one level per pass, up to clean. Water buckets do not work.
- **Heated basin:** a mixer raises 250 mB of water one level per mix, up to clean.
- **Cactus:** compacting gives 250 mB of acceptable water (3) and green dye.
- **Item Drain:** empties terracotta water bowls.
- **Spouts, drains, pipes, pumps and hose pulleys** keep the purity of the water they move. An open pipe end that takes water from the world or a cauldron gets the purity the water has there (`create.openEndedPipePurity`).
- **Goggles** show the purity of water in tanks and basins.

With purity off, the Sand Filter lets water through unchanged, cactus compacting still gives plain water, and the three `create.*` purity keys do nothing.

On 1.20.1 the Sand Filter works the same, but Create's heated mixing does not purify water that has no purity stored. Water from Create's own pumps, pipes and hose pulleys always has one.

### Supplementaries and Moonlight

The Jar and the Faucet keep the purity of water. A Faucet on a water block or cauldron gives water with its purity. A Jar remembers the purity of what goes in and gives it back to the bottles and pipes it fills. A Jar that already holds water ignores the purity of a bottle emptied into it. Both mods are needed.

### Mekanism and Immersive Engineering

Mekanism's Electric Pump and Immersive Engineering's Fluid Pump give water with the purity of the place it was taken from. A pump holds one purity at a time and waits to empty before taking water of another purity. Heavy water from Mekanism has no purity.

---

## Survival and items

### Cold Sweat

- **Thirst follows body temperature** instead of the biome (`coldsweat.useBodyTemperature`, `coldsweat.bodyTemperatureCurve`). The dimension and Nether multipliers still come first.
- **A hot body counts as a hot climate** for dirty water (`purity.toml` `hotDirtyWater.useColdSweat`, `hotDirtyWater.coldSweatMinBodyTemp`, 50).
- **Drinking water can cool you** (`coldsweat.drinkCooling`, `coldsweat.drinkCoolingTicks`). Off by default. The waterskin does not use it.
- **Waterskin:** hydrates like a drink of water, with the purity of the water inside. Filling it from a source, a cauldron or a tank gives it that purity, and cooking purifies it like a bottle. It counts for the pure water bonus.
- **Canteen:** takes the temperature of the water you fill it from. The Boiler heats it, the Icebox chills it, and each sip warms or cools you. The Boiler also purifies water bottles, buckets, bowls and Canteens while it has fuel, one level every 10 seconds, up to acceptable.
- **Serene Seasons is ignored** while Cold Sweat is installed, because body temperature already follows the seasons.

### Serene Seasons

The season changes the biome temperature in the climate formula, so winter lowers thirst loss where it cools the biome (`sereneseasons.enabled`). Biomes without seasons and the Nether are left alone. Each season has a thirst multiplier, and tropical biomes have one for the dry and one for the wet season. Keys: [Configuration](Configuration#sereneseasons).

### Traveler's Backpack

Drinking water with the hose hydrates like a water bottle, with the purity of the water in the tank. It takes 250 mB. Water the hose sucks from the world keeps its purity. The hose counts for `pureWater.thirstBonus` and `coldsweat.drinkCooling`.

### Reliquary

The Emperor's Chalice hydrates like a drink of pure water (thirst 4, hydration 5, no pure water bonus). Reliquary's potions give thirst 2 and hydration 3. `reliquary.emperorChaliceCooldown` adds a cooldown in ticks after a drink (0, none).

---

## Vampires

With Vampirism or Supernatural, a vampire gets thirsty by the same rules as anyone else, and only blood hydrates it. Water, other drinks, food, rain and drinking by hand give a vampire nothing, and the HUD shows no preview for them. Blood never makes a vampire sick and never counts towards Overhydrated. The droplets are red (`client.toml` `Bar Colors.vampire`).

- **Vampirism:** every drink of blood restores thirst: bites, blood bottles, blood containers and blood food. One point of blood gives `vampirism.thirstPerBlood` thirst (1.0) and `vampirism.hydrationPerBlood` times the blood's saturation in hydration. Altars and commands that fill the blood bar do not count.
- **Supernatural:** its blood bottle gives thirst 6 and hydration 6. It has no bite, so bottles are the way to drink blood. Items in the tag `droplets_of_thirst:blood` hydrate vampires, and by default it holds `#supernatural:blood`.

On 1.20.1 Vampirism works the same. Supernatural has no blood tag there, so `droplets_of_thirst:blood` lists its bottle directly.

---

## Interface

- **JEI and EMI:** a "Hydration" page lists every item that changes thirst, and a "Water Purification" page shows the water cauldron, plain and over a heat source, and the Sand Filter with Create. Cooking recipes show in their own pages. With purity off, the "Water Purification" page is hidden.
- **Jade:** shows the purity of a water cauldron and of water in tanks. For a block that shows its contents, such as a chest, it also lists the purity and amount of the water containers inside, for example `Pure x3` (`client.toml` `Tooltip.jadeStoredWaterPurity`). Tank and stored purity need Jade on the server too.
- **AppleSkin:** Droplets of Thirst draws its own hydration outline, drink preview, exhaustion underlay and tooltip icons. With AppleSkin, each is hidden when its food counterpart is off in AppleSkin (`client.toml` `Tooltip.followAppleSkin`).

---

## Food mods

### Farmer's Delight and its addons

Covers Farmer's Delight, Farmer's Respite, Brewin' and Chewin', Extra Delight, Expanded Delight, Ocean's Delight, Ender's Delight, End's Delight, My Nether's Delight, Miner's Delight, Cultural Delights, Fruits Delight, Rustic Delight, Corn Delight, Crabber's Delight, Collector's Reap and Cook's Collection.

- **Drink values** for their drinks and food. Salty, pickled and strongly alcoholic items make you thirstier.
- **Clean water for cooking.** Recipes that use water need a minimum purity: murky (2) or better when the recipe boils the water, acceptable (3) or better for cold ones. Water with no purity stored counts as acceptable.
- **Cooking pot.** Farmer's Delight's cooking pot and Miner's Delight's copper pot purify bottles, buckets, bowls and, with Cold Sweat, waterskins, one level per cook.
- **Nourishment** pauses thirst loss while it lasts.
- **Taps and sinks** of Extra Delight give water with the purity of the world's water where they stand (`delight.worldPurityWaterSources`).
- **Miner's Delight's water cup** carries a purity like a bottle.
- **Expanded Delight:** its Juicer makes melon juice, and the melon juice recipes of Farmer's Delight and Farmer's Respite stay off.
- **Chest loot:** drinks of Brewin' and Chewin' and Farmer's Respite.

On 1.20.1, Extra Delight and Expanded Delight have no version. Brewin' and Chewin' kegs do not take water that carries a purity. Fruits Delight and Rustic Delight Create mixing recipes accept water of any purity. Miner's Delight's mod id is `miners_delight`.

### Farm & Charm, HerbalBrews and Brewery

All three have drink values. Their recipes that use water follow the clean water rule above.

- **Farm & Charm:** the timber well and the sink give water with the world's purity where they stand (`delight.worldPurityWaterSources`). Not on 1.20.1: the timber well is not in that version.
- **HerbalBrews:** the tea kettle does not take water below murky (`delight.kettleMinPurity`).
- **Brewery:** the brewing stations refuse water below `delight.kettleMinPurity` with a message.
- **Stuffed** (Bakery) and **Saturated** (Brewery) pause thirst loss, like Nourishment.

---

## Scripting

### KubeJS 7

KubeJS scripts can read and change thirst, hydrate players, listen to events and register drinks, with no Java. Examples are in `examples/kubejs` on the `1.21.1` branch, and the API notes are in [Minecraft 1.21.1](Minecraft-1.21.1#kubejs). On 1.20.1, KubeJS has a different API from the examples. The Droplets of Thirst API works from any mod.

---

## Discouraged and incompatible mods

- **Discouraged** (they have their own thirst): Tough As Nails, Legendary Survival Overhaul, Homeostatic, Survive, Thirst Was Taken 2. The game starts with a warning and both systems run until you remove one.
- **Incompatible:** Thirst Was Taken and Thirst Was Reclaimed (mod id `thirst`). Droplets of Thirst continues them, so remove them.

---

## Links

CurseForge pages of every mod on this page. Miner's Delight links to Modrinth.

### Technical mods

- Create: https://www.curseforge.com/minecraft/mc-mods/create
- Supplementaries: https://www.curseforge.com/minecraft/mc-mods/supplementaries
- Moonlight Lib: https://www.curseforge.com/minecraft/mc-mods/selene
- Mekanism: https://www.curseforge.com/minecraft/mc-mods/mekanism
- Immersive Engineering: https://www.curseforge.com/minecraft/mc-mods/immersive-engineering

### Survival and items

- Cold Sweat: https://www.curseforge.com/minecraft/mc-mods/cold-sweat
- Serene Seasons: https://www.curseforge.com/minecraft/mc-mods/serene-seasons
- Traveler's Backpack: https://www.curseforge.com/minecraft/mc-mods/travelers-backpack
- Reliquary Reincarnations: https://www.curseforge.com/minecraft/mc-mods/reliquary-reincarnations

### Vampires

- Vampirism: https://www.curseforge.com/minecraft/mc-mods/vampirism-become-a-vampire
- Supernatural: https://www.curseforge.com/minecraft/mc-mods/supernatural

### Interface

- JEI: https://www.curseforge.com/minecraft/mc-mods/jei
- EMI: https://www.curseforge.com/minecraft/mc-mods/emi
- Jade: https://www.curseforge.com/minecraft/mc-mods/jade
- AppleSkin: https://www.curseforge.com/minecraft/mc-mods/appleskin

### Food mods

- Farmer's Delight: https://www.curseforge.com/minecraft/mc-mods/farmers-delight
- Farmer's Respite: https://www.curseforge.com/minecraft/mc-mods/farmers-respite
- Brewin' and Chewin': https://www.curseforge.com/minecraft/mc-mods/brewin-and-chewin
- Extra Delight: https://www.curseforge.com/minecraft/mc-mods/extradelight
- Expanded Delight: https://www.curseforge.com/minecraft/mc-mods/expanded-delight
- Ocean's Delight: https://www.curseforge.com/minecraft/mc-mods/oceans-delight
- Ender's Delight: https://www.curseforge.com/minecraft/mc-mods/enders-delight
- End's Delight: https://www.curseforge.com/minecraft/mc-mods/ends-delight
- My Nether's Delight: https://www.curseforge.com/minecraft/mc-mods/my-nethers-delight
- Miner's Delight: https://modrinth.com/mod/miners-delight
- Cultural Delights: https://www.curseforge.com/minecraft/mc-mods/cultural-delights
- Fruits Delight: https://www.curseforge.com/minecraft/mc-mods/fruits-delight
- Rustic Delight: https://www.curseforge.com/minecraft/mc-mods/rustic-delight
- Corn Delight: https://www.curseforge.com/minecraft/mc-mods/corn-delight
- Crabber's Delight: https://www.curseforge.com/minecraft/mc-mods/crabbers-delight
- Collector's Reap: https://www.curseforge.com/minecraft/mc-mods/collectors-reap
- Cook's Collection: https://www.curseforge.com/minecraft/mc-mods/cooks-collection
- Let's Do Farm & Charm: https://www.curseforge.com/minecraft/mc-mods/lets-do-farm-charm
- Let's Do HerbalBrews: https://www.curseforge.com/minecraft/mc-mods/lets-do-herbal-brews
- Let's Do Brewery: https://www.curseforge.com/minecraft/mc-mods/lets-do-brewery-farm-charm-compat
- Let's Do Bakery: https://www.curseforge.com/minecraft/mc-mods/lets-do-bakery-farm-charm-compat

### Scripting

- KubeJS: https://www.curseforge.com/minecraft/mc-mods/kubejs

### Discouraged and incompatible mods

- Tough As Nails: https://www.curseforge.com/minecraft/mc-mods/tough-as-nails
- Legendary Survival Overhaul: https://www.curseforge.com/minecraft/mc-mods/legendary-survival-overhaul
- Homeostatic: https://www.curseforge.com/minecraft/mc-mods/homeostatic
- Survive: https://www.curseforge.com/minecraft/mc-mods/survive
- Thirst Was Taken 2: https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken-2
- Thirst Was Taken: https://www.curseforge.com/minecraft/mc-mods/thirst-was-taken
- Thirst Was Reclaimed: https://www.curseforge.com/minecraft/mc-mods/thirst-was-reclaimed
