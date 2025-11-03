package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public final class SearchTreeUpdater {

	public static final Marker LOG_MARKER = MarkerFactory.getMarker("SearchTreeUpdater");

	private final ExecutorService executor = Executors.newSingleThreadExecutor(target -> {
		Thread thread = new Thread(target, "SearchTreeUpdater");
		thread.setDaemon(true); //jvm should terminate this thread when shutting down
		return thread;
	});

	public SearchTreeUpdater() {}

	public <T> void submitRebuild(TreeEntryExtension<T> treeEntry, List<T> values) {
		executor.submit(new RebuildRequest<>(treeEntry, values));
	}

	public <T> void submitRefresh(TreeEntryExtension<T> treeEntry) {
		executor.submit(new RefreshRequest<>(treeEntry));
	}

	public void shutdown() {
		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Shutting down...");
		executor.shutdown();
		executor.shutdownNow();
	}

	public interface TreeEntryExtension<T> {

		AtomicReference<RefreshableSearchTree<T>> ORB$AtomicTree();

		SearchRegistry.TreeBuilderSupplier<T> ORB$getTreeFactory();

	}

	private record RebuildRequest<T>(TreeEntryExtension<T> treeEntry, List<T> values) implements Runnable {
		@Override
		public void run() {
			ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Async (Re-)Building SearchTree...");
			long startTime = System.nanoTime();

			RefreshableSearchTree<T> tree = treeEntry.ORB$getTreeFactory().apply(values);
			tree.refresh();
			treeEntry.ORB$AtomicTree().set(tree);

			long elapsedNanos = System.nanoTime() - startTime;
			ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Async SearchTree (re-)build took: {}", Duration.ofNanos(elapsedNanos));
		}
	}

	private record RefreshRequest<T>(TreeEntryExtension<T> treeEntry) implements Runnable {
		@Override
		public void run() {
			ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Async Refreshing SearchTree...");
			long startTime = System.nanoTime();

			AtomicReference<RefreshableSearchTree<T>> reference = treeEntry.ORB$AtomicTree();
			RefreshableSearchTree<T> tree = reference.getAndSet(RefreshableSearchTree.empty());
			tree.refresh();
			reference.set(tree);

			long elapsedNanos = System.nanoTime() - startTime;
			ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Async SearchTree refresh took: {}", Duration.ofNanos(elapsedNanos));
		}
	}

}
