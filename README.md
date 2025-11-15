# ORB – Optimized Recipe Book

ORB aims to improve the performance of the recipe book without removing its functionality.

> [!WARNING]
> This mod is fundamentally incompatible with mods like NERB!
> ORB is a replacement/upgrade for it!

## Features

- async rebuild/refresh of the search index
- improves performance of Recipe Book Menu
	- shows progress messages when the search index isn't ready yet
	- async checking if the recipes in the book are craftable
- improves performance of Creative Search Menu
	- shows progress messages when the search index isn't ready yet
- isolates recipe book data from player data
	- recipe book data is now serialized to/from a dedicated folder called `recipe_book`
	- player nbt data (`/data get entity Dev`) will no longer contain recipe book data
- significantly improves performance when awarding/unlocking a large amount of recipes (i.e. unlocking all recipes with the command `\recipe give Dev *`)
	- Warning: while recipe unlocking is quick, the advancement triggering will take several seconds to complete
- expand the recipe command with `\recipe get Dev` to return the recipe book data

## Compatibilities

- Compatible with **FastWorkbench** (don't forget to enable the recipe book button in the configs)
- Compatible with **CraftingTweaks** (don't forget to enable the recipe book button in the configs)
- Compatible with **ModernFix**
	- Note: When ModernFix is installed for **Minecraft 1.20.1**, ORB will no longer rebuild the search index asynchronously.
	  ModernFix handles the process instead, so you will not see the "updating search index" messages in the Recipe Book or Creative Search menu.

## Incompatibilities

The mod is incompatible with all mods that prevent the server from sending recipe book data to the client.

- Incompatible with NERB

## Development

**You need to run datagen to generate the unlockable recipes needed for the test items!**
