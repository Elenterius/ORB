package com.github.elenterius.orb.core;

import net.minecraft.client.multiplayer.SessionSearchTrees;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

public final class SearchTreeUpdater {

	public static final Marker UPDATE_MARKER = MarkerFactory.getMarker("SearchTreeUpdater");
	public static final Marker SEARCH_MARKER = MarkerFactory.getMarker("SearchTree");

	private final Map<SessionSearchTrees.Key, AtomicInteger> progressTrackers = new HashMap<>();

	public SearchTreeUpdater() {}

	public IntSupplier getProgressSupplier(SessionSearchTrees.Key key) {
		AtomicInteger progressTracker = progressTrackers.computeIfAbsent(key, k -> new AtomicInteger(100));
		return progressTracker::get;
	}

	public AtomicInteger getProgressTracker(SessionSearchTrees.Key key) {
		return progressTrackers.computeIfAbsent(key, k -> new AtomicInteger(100));
	}

}
