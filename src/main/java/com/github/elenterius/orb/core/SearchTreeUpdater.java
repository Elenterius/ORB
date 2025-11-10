package com.github.elenterius.orb.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.CreativeModeTabSearchRegistry;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public final class SearchTreeUpdater {

	public static final Marker UPDATE_MARKER = MarkerFactory.getMarker("SearchTreeUpdater");
	public static final Marker SEARCH_MARKER = MarkerFactory.getMarker("SearchTree");

	private final Map<SearchRegistry.TreeEntry<?>, String> treeNames = new HashMap<>();
	private final Map<SearchRegistry.TreeEntry<?>, SearchRegistry.Key<?>> treeKeys = new HashMap<>();

	private final ExecutorService executor = Executors.newSingleThreadExecutor(target -> {
		Thread thread = new Thread(target, "SearchTreeUpdater");
		thread.setDaemon(true); //jvm should terminate this thread when shutting down
		return thread;
	});

	private final Map<SearchRegistry.Key<?>, AtomicInteger> progressTrackers = new HashMap<>();

	public SearchTreeUpdater() {}

	private void populateTreeMaps() {
		if (!treeNames.isEmpty()) return;

		Map<CreativeModeTab, SearchRegistry.Key<ItemStack>> nameSearchKeys = CreativeModeTabSearchRegistry.getNameSearchKeys();
		Map<CreativeModeTab, SearchRegistry.Key<ItemStack>> tagSearchKeys = CreativeModeTabSearchRegistry.getTagSearchKeys();

		if (Minecraft.getInstance().getSearchTreeManager() instanceof SearchRegistryExtension registry) {
			Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees = registry.ORB$getSearchTrees();
			for (Map.Entry<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> entry : searchTrees.entrySet()) {
				SearchRegistry.Key<?> key = entry.getKey();
				SearchRegistry.TreeEntry<?> tree = entry.getValue();

				treeKeys.put(tree, key);

				if (key == SearchRegistry.CREATIVE_NAMES) {
					treeNames.putIfAbsent(tree, "Creative Names");
				}
				else if (key == SearchRegistry.CREATIVE_TAGS) {
					treeNames.putIfAbsent(tree, "Creative Tags");
				}
				else if (key == SearchRegistry.RECIPE_COLLECTIONS) {
					treeNames.putIfAbsent(tree, "Recipe Collections");
				}
				else {
					for (Map.Entry<CreativeModeTab, SearchRegistry.Key<ItemStack>> nameEntry : nameSearchKeys.entrySet()) {
						if (key == nameEntry.getValue()) {
							treeNames.putIfAbsent(tree, nameEntry.getKey().getDisplayName().getString());
							break;
						}
					}
					for (Map.Entry<CreativeModeTab, SearchRegistry.Key<ItemStack>> tagEntry : tagSearchKeys.entrySet()) {
						if (key == tagEntry.getValue()) {
							treeNames.putIfAbsent(tree, tagEntry.getKey().getDisplayName().getString());
							break;
						}
					}
				}
			}
		}
	}

	public <T> String getName(SearchRegistry.TreeEntry<T> treeEntry) {
		populateTreeMaps();
		return treeNames.getOrDefault(treeEntry, "Unknown");
	}

	public <T> SearchRegistry.Key<?> getKey(SearchRegistry.TreeEntry<T> treeEntry) {
		return treeKeys.get(treeEntry);
	}

	public <T> void submitRebuild(SearchRegistry.TreeEntry<T> treeEntry, List<T> values) {
		String treeName = getName(treeEntry);
		SearchRegistry.Key<?> key = getKey(treeEntry);

		AtomicInteger progressTracker = progressTrackers.computeIfAbsent(key, k -> new AtomicInteger(100));

		//noinspection unchecked
		executor.submit(new RebuildRequest<>(treeName, (TreeEntryExtension<T>) treeEntry, values, progressTracker::set));
	}

	public <T> void submitRefresh(SearchRegistry.TreeEntry<T> treeEntry) {
		String treeName = getName(treeEntry);
		SearchRegistry.Key<?> key = getKey(treeEntry);

		AtomicInteger progressTracker = progressTrackers.computeIfAbsent(key, k -> new AtomicInteger(100));

		//noinspection unchecked
		executor.submit(new RefreshRequest<>(treeName, (TreeEntryExtension<T>) treeEntry, progressTracker::set));
	}

	public IntSupplier getProgressTacker(SearchRegistry.Key<?> key) {
		AtomicInteger progressTracker = progressTrackers.computeIfAbsent(key, k -> new AtomicInteger(100));
		return progressTracker::get;
	}

	public void shutdown() {
		Orb.LOGGER.info(SearchTreeUpdater.UPDATE_MARKER, "Shutting down...");
		executor.shutdown();
		executor.shutdownNow();
	}

	public interface SearchRegistryExtension {
		Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> ORB$getSearchTrees();
	}

	public interface TreeEntryExtension<T> {

		AtomicReference<RefreshableSearchTree<T>> ORB$AtomicTree();

		SearchRegistry.TreeBuilderSupplier<T> ORB$getTreeFactory();

	}

	private record RebuildRequest<T>(String treeName, TreeEntryExtension<T> treeEntry, List<T> values, IntConsumer progressConsumer) implements Runnable {
		@Override
		public void run() {
			progressConsumer.accept(0);

			long startTime = System.nanoTime();

			RefreshableSearchTree<T> tree = treeEntry.ORB$getTreeFactory().apply(values);
			progressConsumer.accept(50);
			tree.refresh();
			treeEntry.ORB$AtomicTree().set(tree);

			long elapsedNanos = System.nanoTime() - startTime;
			Orb.LOGGER.debug(SearchTreeUpdater.UPDATE_MARKER, "Rebuild of {} index took {}", treeName, Duration.ofNanos(elapsedNanos));

			progressConsumer.accept(100);
		}
	}

	private record RefreshRequest<T>(String treeName, TreeEntryExtension<T> treeEntry, IntConsumer progressConsumer) implements Runnable {
		@Override
		public void run() {
			progressConsumer.accept(0);

			long startTime = System.nanoTime();

			AtomicReference<RefreshableSearchTree<T>> reference = treeEntry.ORB$AtomicTree();
			RefreshableSearchTree<T> tree = reference.getAndSet(RefreshableSearchTree.empty());
			tree.refresh();
			reference.set(tree);

			long elapsedNanos = System.nanoTime() - startTime;
			Orb.LOGGER.debug(SearchTreeUpdater.UPDATE_MARKER, "Refresh of {} index took {}", treeName, Duration.ofNanos(elapsedNanos));

			progressConsumer.accept(100);
		}
	}

}
