package com.github.elenterius.orb.core;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

public class OrbClient {

	private static final SearchTreeUpdater SEARCH_TREE_UPDATER = new SearchTreeUpdater();
	private static final IntSupplier FULL_PROGRESS = () -> 100;

	private static final RecipeBookPageUpdater RECIPE_BOOK_PAGE_UPDATER = new RecipeBookPageUpdater();
	private static final Runnable EMPTY_CALLBACK = () -> {};

	public static final long DEBOUNCE_DELAY_MS = 200;

	public static final Component INDEXING_TITLE = Orb.translatable("msg", "indexing");
	public static final Component INITIALIZING_TITLE = Orb.translatable("msg", "initializing");
	public static final Component[] LOADING_MESSAGES = IntStream.range(0, 34).mapToObj(i -> Orb.translatable("msg", "loading." + i)).toArray(Component[]::new);
	public static final float LOADING_MESSAGE_DURATION = 30f;

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

	public static IntSupplier getIndexingProgress(SearchRegistry.@Nullable Key<?> key) {
		return key != null ? SEARCH_TREE_UPDATER.getProgressTacker(key) : FULL_PROGRESS;
	}

	public static void asyncUpdateRecipeBookPage(RecipeBookComponent recipeBookComponent, boolean resetPageNumber) {
		RECIPE_BOOK_PAGE_UPDATER.asyncUpdate(recipeBookComponent, resetPageNumber, EMPTY_CALLBACK);
	}

	public static void asyncUpdateRecipeBookPage(RecipeBookComponent recipeBookComponent, boolean resetPageNumber, final Runnable postUpdateCallback) {
		RECIPE_BOOK_PAGE_UPDATER.asyncUpdate(recipeBookComponent, resetPageNumber, postUpdateCallback);
	}

}
