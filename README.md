# Project O.R.B.

Project ORB (Optimized Recipe Book) aims to improve the performance of the recipe book without removing its functionality.

Features

- defer slow search index rebuild/refresh to a background thread
- improve creative search menu performance when searching
- improve search performance in Recipe Book Menu
- isolate recipe book data from player data
	- recipe book data is now serialized to/from a dedicated folder called `recipe_book`
	- player data (`/data get entity @s`) will no longer contain recipe book data
- [ ] improved server join performance by overhauling how recipes are synchronized to clients (eventual consistency)

> [!WARNING]
> The mod is fundamentally incompatible with mods such
> as NERB and should be considered a replacement/upgrade for it.

## Development

The mod uses neoforges [ModDevGradle Legacy](https://github.com/neoforged/ModDevGradle) plugin.

Documentation can be found [here](https://github.com/neoforged/ModDevGradle/blob/main/LEGACY.md).
