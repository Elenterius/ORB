# Project O.R.B.

Project ORB (Optimized Recipe Book) aims to improve the performance of the recipe book without removing its functionality.

> [!WARNING]
> This mod is fundamentally incompatible with mods like NERB and should be considered a replacement/upgrade for it!

## Features

- defer slow search index rebuild/refresh to a separate thread
- improve performance of Creative Search Menu
- improve performance of Recipe Book Menu
	- show progress bar when the search index isn't ready yet
- isolate recipe book data from player data
	- recipe book data is now serialized to/from a dedicated folder called `recipe_book`
	- player nbt data (`/data get entity Dev`) will no longer contain recipe book data
- significantly improve performance when awarding/unlocking a large amount of recipes (e.g. unlocking all recipes with the command `\recipe give Dev *`)
- add command `\recipe get Dev` that returns the recipe book data

## Development

**You need to run datagen (data run configuration) to generate the unlockable recipes needed for the test items!**
