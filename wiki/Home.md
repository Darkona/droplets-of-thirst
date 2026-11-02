# Droplets of Thirst

Droplets of Thirst adds a thirst bar and water purity to Minecraft. Activity and heat make you thirsty, and the water you find can make you sick: swamp or sea water is unsafe until you clean it. The mod is made for modpacks, so every number is in a config file or a datapack. It continues [Thirst Was Taken](https://github.com/ghen-git/Thirst-Mod) by ghen.

![The droplets above the food bar, with a water bottle in hand](images/hud.png)

This wiki describes **Minecraft 1.21.1 on NeoForge 21.1.252 or newer**. If you play Minecraft 1.20.1, read these pages together with [its page](Minecraft-1.20.1).

---

## Pages

- [Gameplay](Gameplay): the thirst bar, how to drink, where clean water comes from and how to purify it, effects and potions.
- [Configuration](Configuration): every config file and option, the commands, and how to play with thirst only (no water purity).
- [Modpack makers](Modpack-Makers): drink values, water purity in the world, tags, recipes, loot and datapacks.
- [Mod developers](Mod-Developers): the public API and events.

---

## Versions

Both versions have the same thirst, water purity, effects, config files and commands. They differ in the loader, in the data formats of their Minecraft version and in the other mods they work with. Minecraft 1.21.1 has the most integrations: Create, Farmer's Delight and its addons, Cold Sweat, Vampirism, Supernatural, Reliquary and KubeJS.

| Minecraft | Loader | Branch | Notable differences |
|---|---|---|---|
| [1.21.1](Minecraft-1.21.1) (this wiki) | NeoForge 21.1.252 or newer | `1.21.1` | All integrations: Create (Sand Filter), Farmer's Delight and its addons, Cold Sweat, Vampirism, Supernatural, Reliquary, KubeJS 7 |
| [1.20.1](Minecraft-1.20.1) | Forge 47.1.3 or newer, and NeoForge 47.1 (one jar) | `1.20.1` | Purity in NBT. Most 1.21.1 integrations, no KubeJS |

The [GitHub repository](https://github.com/Darkona/droplets-of-thirst) has the downloads, the changelog and the source code.
