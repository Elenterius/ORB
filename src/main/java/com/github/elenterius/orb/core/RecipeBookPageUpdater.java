package com.github.elenterius.orb.core;

import com.github.elenterius.orb.mixin.StackedContentsAccessor;
import com.github.elenterius.orb.mixin.client.RecipeBookAccessor;
import com.github.elenterius.orb.mixin.client.RecipeBookComponentAccessor;
import com.github.elenterius.orb.mixin.client.RecipeCollectionAccessor;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.lang.ref.WeakReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class RecipeBookPageUpdater {

	public static final Marker LOG_MARKER = MarkerFactory.getMarker("RecipeBookPageUpdater");

	private final ExecutorService executor = Executors.newSingleThreadExecutor(target -> {
		Thread thread = new Thread(target, "RecipeBookPageUpdater");
		thread.setDaemon(true); //jvm should terminate this thread when shutting down
		return thread;
	});

	public void asyncUpdate(final RecipeBookComponent recipeBookComponent, final boolean resetPageNumber, final boolean updateTabs) {
		final RecipeBookCategories updateCategory = ((RecipeBookComponentAccessor) recipeBookComponent).getSelectedTab().getCategory();
		final WeakReference<RecipeBookComponent> weakReference = new WeakReference<>(recipeBookComponent);

		CompletableFuture
				.supplyAsync(UpdateRequest.of(recipeBookComponent), executor)
				.handle((updates, throwable) -> {
					if (throwable != null) {
						Orb.LOGGER.error(LOG_MARKER, "Failed to asynchronously update of recipe collections for page {}", updateCategory.name(), throwable);
						return null;
					}

					RecipeBookComponent ref = weakReference.get();
					if (ref == null) {
						Orb.LOGGER.warn(LOG_MARKER, "Discarding update: the recipe page is no longer available");
						return null;
					}
					RecipeBookComponentAccessor accessor = (RecipeBookComponentAccessor) ref;

					long startTime = System.nanoTime();

					RecipeBookCategories currentCategory = accessor.getSelectedTab().getCategory();
					if (currentCategory != updateCategory) {
						Orb.LOGGER.warn(LOG_MARKER, "Discarding update: the current page category is different");
						return null;
					}

					ClientRecipeBook recipeBook = accessor.getBook();
					List<RecipeCollection> validCollections = new LinkedList<>(recipeBook.getCollection(currentCategory));

					validCollections.removeIf(collection -> !updates.containsKey(collection));

					for (RecipeCollection collection : validCollections) {
						RecipeCollectionUpdate update = updates.get(collection);
						RecipeCollectionAccessor collectionAccessor = (RecipeCollectionAccessor) collection;
						collectionAccessor.setFitsDimensions(update.fitsDimensions);
						collectionAccessor.setCraftable(update.craftable);
					}

					validCollections.removeIf(collection -> !collection.hasKnownRecipes());
					validCollections.removeIf(collection -> !collection.hasFitting());

					if (accessor.getBook().isFiltering(accessor.getMenu())) {
						validCollections.removeIf(collection -> !collection.hasCraftable());
					}

					accessor.getRecipeBookPage().updateCollections(validCollections, resetPageNumber);

					if (updateTabs) {
						accessor.callUpdateTabs();
					}

					Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
					Orb.LOGGER.debug(LOG_MARKER, "Updated {} recipe entries which took {}", validCollections.size(), duration);
					return updates;
				})
				.exceptionally(throwable -> {
					Orb.LOGGER.error(LOG_MARKER, "Failed to apply update to recipe page {}", updateCategory.name(), throwable);
					return null;
				});
	}

	public void shutdown() {
		Orb.LOGGER.info(SearchTreeUpdater.UPDATE_MARKER, "Shutting down...");
		executor.shutdown();
		executor.shutdownNow();
	}

	/**
	 * @param fitsDimensions needs to be mutable
	 * @param craftable      needs to be mutable
	 */
	public record RecipeCollectionUpdate(HashSet<Recipe<?>> fitsDimensions, HashSet<Recipe<?>> craftable) {}

	public static class RecipeBookUpdateException extends RuntimeException {
		public RecipeBookUpdateException(String message) {
			super(message);
		}
	}

	private record UpdateRequest(String query, List<RecipeCollection> collections, Set<ResourceLocation> knownRecipes, StackedContents stackedContents, int gridWidth,
	                             int gridHeight) implements Supplier<Map<RecipeCollection, RecipeCollectionUpdate>> {

		public static UpdateRequest of(RecipeBookComponent recipeBookComponent) {
			RecipeBookComponentAccessor accessor = (RecipeBookComponentAccessor) recipeBookComponent;
			ClientRecipeBook recipeBook = accessor.getBook();
			RecipeBookMenu<?> menu = accessor.getMenu();

			String query = accessor.getSearchBox().getValue();
			List<RecipeCollection> collections = new LinkedList<>(recipeBook.getCollection(accessor.getSelectedTab().getCategory()));
			Set<ResourceLocation> knownRecipes = Set.copyOf(((RecipeBookAccessor) recipeBook).ORB$getKnown());

			StackedContents stackedContents = new StackedContents();
			((StackedContentsAccessor) stackedContents).setContents(new Int2IntOpenHashMap(accessor.getStackedContents().contents));

			return new UpdateRequest(
					query,
					collections,
					knownRecipes,
					stackedContents,
					menu.getGridWidth(),
					menu.getGridHeight()
			);
		}

		@Override
		public Map<RecipeCollection, RecipeCollectionUpdate> get() {
			long startTime = System.nanoTime();
			Minecraft client = Minecraft.getInstance();

			if (client.level == null) throw new RecipeBookUpdateException("Level does not exists");
			if (client.player == null) throw new RecipeBookUpdateException("Player does not exists");

			//remove all collections that don't contain any matching recipes
			if (!query.isEmpty()) {
				List<RecipeCollection> result = client.getSearchTree(SearchRegistry.RECIPE_COLLECTIONS).search(query.toLowerCase(Locale.ROOT));
				ObjectSet<RecipeCollection> resultSet = new ObjectLinkedOpenHashSet<>(result);
				collections.removeIf(collection -> !resultSet.contains(collection));
			}

			var recipeManager = client.level.getRecipeManager();

			Map<RecipeCollection, RecipeCollectionUpdate> updates = new HashMap<>();

			//it's theoretically safe to work with the recipes from the collection because the list is immutable
			//if the recipes change due to datapack reload the game will replace the recipe collections in the recipe book
			//that means in the worst case the recipe collections we are working with become outdated and the result of this work will be ignored
			for (RecipeCollection collection : collections) {
				HashSet<Recipe<?>> fitsDimensions = collection.getRecipes().stream()
						.map(Recipe::getId)
						.filter(knownRecipes::contains)
						.map(recipeManager::byKey)
						.flatMap(Optional::stream)
						.filter(recipe -> recipe.canCraftInDimensions(gridWidth, gridHeight))
						.collect(Collectors.toCollection(HashSet::new));

				HashSet<Recipe<?>> craftable = fitsDimensions.stream()
						.filter(recipe -> stackedContents.canCraft(recipe, null)) //do expensive method call
						.collect(Collectors.toCollection(HashSet::new));

				updates.put(collection, new RecipeCollectionUpdate(fitsDimensions, craftable));
			}

			long elapsedNanos = System.nanoTime() - startTime;
			Orb.LOGGER.debug(LOG_MARKER, "Async update of recipe collections took {}", Duration.ofNanos(elapsedNanos));

			return updates;
		}

	}

}
