package com.github.elenterius.orb.core;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

public class OrbClient {

	private static final SearchTreeUpdater SEARCH_TREE_UPDATER = new SearchTreeUpdater();
	private static final RecipeBookPageUpdater RECIPE_BOOK_PAGE_UPDATER = new RecipeBookPageUpdater();

	public static final long DEBOUNCE_DELAY_MS = 200;

	//public static final ResourceLocation LOADING_TEXTURE = NeoForgeOrbMod.rl("textures/gui/loading.png");

	public static final Component[] LOADING_MESSAGES = Stream.of(
			"Recalibrating crafting grids for symmetry",
			"Cross-checking recipe costs with villager trade inflation",
			"Teaching Sniffers to sort recipes by smell",
			"Trying to locate the meaning of crafting",
			"Synchronizing recipe data with villager gossip",
			"Finalizing crafting topology... almost table",
			"Verifying that crafting tables are still made of wood",
			"Debugging villagers who think emeralds are food",
			"Verifying that all bread is legally bread and not toast",
			"Updating indexes for user happiness",
			"Counting crafting tables. There are too many."
	).map(Component::literal).toArray(Component[]::new);

	private OrbClient() {}

	public static void registerShutdownHooks() {
		// unnecessary as the JVM should terminate, this is just for the sake of sanity
		// note: shutdown hooks only work on the client side
		Runtime.getRuntime().addShutdownHook(new Thread(SEARCH_TREE_UPDATER::shutdown));
		Runtime.getRuntime().addShutdownHook(new Thread(RECIPE_BOOK_PAGE_UPDATER::shutdown));
	}

	public static <T> void asyncCreateSearchTree(SearchRegistry.TreeEntry<T> treeEntry, List<T> values) {
		SEARCH_TREE_UPDATER.submitRebuild(treeEntry, values);
	}

	public static <T> void asyncRefreshSearchTree(SearchRegistry.TreeEntry<T> treeEntry) {
		SEARCH_TREE_UPDATER.submitRefresh(treeEntry);
	}

	public static IntSupplier getIndexingProgress(SearchRegistry.Key<?> key) {
		return SEARCH_TREE_UPDATER.getProgressTacker(key);
	}

	public static void asyncUpdateRecipeBookPage(RecipeBookComponent recipeBookComponent, boolean resetPageNumber, boolean updateTabs) {
		RECIPE_BOOK_PAGE_UPDATER.asyncUpdate(recipeBookComponent, resetPageNumber, updateTabs);
	}

}
