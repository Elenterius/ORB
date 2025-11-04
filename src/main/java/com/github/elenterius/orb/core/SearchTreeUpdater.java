package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
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
import java.util.concurrent.atomic.AtomicReference;

public final class SearchTreeUpdater {

	public static final Marker LOG_MARKER = MarkerFactory.getMarker("SearchTreeUpdater");

	private final Map<SearchRegistry.TreeEntry<?>, String> treeNames = new HashMap<>();

	private final ExecutorService executor = Executors.newSingleThreadExecutor(target -> {
		Thread thread = new Thread(target, "SearchTreeUpdater");
		thread.setDaemon(true); //jvm should terminate this thread when shutting down
		return thread;
	});

	public SearchTreeUpdater() {}

	private void populateTreeNameMap() {
		if (!treeNames.isEmpty()) return;

		Map<CreativeModeTab, SearchRegistry.Key<ItemStack>> nameSearchKeys = CreativeModeTabSearchRegistry.getNameSearchKeys();
		Map<CreativeModeTab, SearchRegistry.Key<ItemStack>> tagSearchKeys = CreativeModeTabSearchRegistry.getTagSearchKeys();

		if (Minecraft.getInstance().getSearchTreeManager() instanceof SearchRegistryExtension registry) {
			Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees = registry.ORB$getSearchTrees();
			for (Map.Entry<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> entry : searchTrees.entrySet()) {
				SearchRegistry.Key<?> key = entry.getKey();
				SearchRegistry.TreeEntry<?> tree = entry.getValue();

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

	private <T> String getTreeName(SearchRegistry.TreeEntry<T> treeEntry) {
		populateTreeNameMap();
		return treeNames.getOrDefault(treeEntry, "Unknown");
	}

	public <T> void submitRebuild(SearchRegistry.TreeEntry<T> treeEntry, List<T> values) {
		String keyName = getTreeName(treeEntry);

		//noinspection unchecked
		executor.submit(new RebuildRequest<>(keyName, (TreeEntryExtension<T>) treeEntry, values));
	}

	public <T> void submitRefresh(SearchRegistry.TreeEntry<T> treeEntry) {
		String keyName = getTreeName(treeEntry);

		//noinspection unchecked
		executor.submit(new RefreshRequest<>(keyName, (TreeEntryExtension<T>) treeEntry));
	}

	public void shutdown() {
		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Shutting down...");
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

	private record RebuildRequest<T>(String treeName, TreeEntryExtension<T> treeEntry, List<T> values) implements Runnable {
		@Override
		public void run() {
			long startTime = System.nanoTime();

			RefreshableSearchTree<T> tree = treeEntry.ORB$getTreeFactory().apply(values);
			tree.refresh();
			treeEntry.ORB$AtomicTree().set(tree);

			long elapsedNanos = System.nanoTime() - startTime;
			ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Rebuild of {} index took {}", treeName, Duration.ofNanos(elapsedNanos));
		}
	}

	private record RefreshRequest<T>(String treeName, TreeEntryExtension<T> treeEntry) implements Runnable {
		@Override
		public void run() {
			long startTime = System.nanoTime();

			AtomicReference<RefreshableSearchTree<T>> reference = treeEntry.ORB$AtomicTree();
			RefreshableSearchTree<T> tree = reference.getAndSet(RefreshableSearchTree.empty());
			tree.refresh();
			reference.set(tree);

			long elapsedNanos = System.nanoTime() - startTime;
			ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Refresh of {} index took {}", treeName, Duration.ofNanos(elapsedNanos));
		}
	}

}
