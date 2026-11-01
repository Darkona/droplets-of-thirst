# Art

`logo-placeholder-256.png` (logo, also `src/main/resources/logo.png`, the mod list icon) and `logo-placeholder-64.png` (small icon / avatar) are a **temporary placeholder**: a simple blue droplet generated programmatically. They carry a `TEMPORARY placeholder` comment in their PNG metadata.

They are to be replaced by artwork from a human artist. When the final logo arrives, replace it in every place the placeholder is used: `src/main/resources/logo.png`, this folder, the README and the CurseForge/Modrinth pages.

The visual sources of the mod's own blocks, edited in Blockbench: models in `models/`, textures in `textures/`. The cook turns them into the game's files, and nothing else writes those.

## Block models

### Layout

A project's path under `models/` is its model's path in the game: `models/block/amphora.bbmodel` becomes `assets/droplets_of_thirst/models/block/amphora.json`. A texture's path under `textures/` works the same way.

### Edit in Blockbench

1. Open a `.bbmodel` from `models/` (File, Open Model). They are projects of type Java Block/Item.
2. Paint or swap textures from `textures/`, never from another folder. Save with Ctrl+S: it saves the project and writes each painted texture back to `textures/`. Turn off Settings, Export, Embed Textures if it is on.
3. To start a new model, create it as Java Block/Item and save it in `models/` with the name the game will use.

The collision shapes live in the code: `AmphoraBlock.SHAPE` and `CharcoalFilterBlock.SHAPE`. If a model changes its outline, change them too.

### Cook

```sh
./gradlew --no-daemon cookArt
./gradlew --no-daemon cookArt --args='art/models/block/amphora.bbmodel'
./gradlew --no-daemon cookArt --args='--check'
```

`lintArt` lists faces that fight, cubes that cross and floating parts. `importArtModel` makes the project of a game model that has none. Commit what you changed in `art/` and what the cook wrote in `src/main/resources`.
