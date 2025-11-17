package com.github.elenterius.orb.core;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Worker for triggering 'recipe unlocked' criteria spread out over time.
 * <p>
 * We only hold items in the queue for a limited amount of time.
 * Should it fail to complete in the given time frame the items are discarded.
 * (e.g. Player leaves the server/world too early)
 */
public class RecipeUnlockedTriggerManager {

	public static final int LIMIT_PER_TICK = 500;
	private static final long CACHE_DURATION = Duration.ofMinutes(10).toMillis();

	private final Map<UUID, LinkedList<ResourceLocation>> playerRecipeQueue = new HashMap<>();
	private final Object2LongMap<UUID> lastModified = new Object2LongOpenHashMap<>();

	public void enqueue(ServerPlayer player, RecipeHolder<?> recipe) {
		UUID uuid = player.getUUID();
		playerRecipeQueue.computeIfAbsent(uuid, id -> new LinkedList<>()).addLast(recipe.id());
		lastModified.put(uuid, System.currentTimeMillis());
	}

	public void tick(MinecraftServer server) {
		if (playerRecipeQueue.isEmpty()) return;
		if (server.getTickCount() % 10 == 0) return;

		Set<UUID> onlineIds = playerRecipeQueue.keySet().stream()
				.filter(uuid -> server.getPlayerList().getPlayer(uuid) != null)
				.collect(Collectors.toSet());

		// remove expired caches for offline players
		final long currentTime = System.currentTimeMillis();
		List<UUID> expiredIds = playerRecipeQueue.keySet().stream()
				.filter(uuid -> !onlineIds.contains(uuid) && currentTime - lastModified.getLong(uuid) >= CACHE_DURATION)
				.toList();
		for (UUID uuid : expiredIds) {
			playerRecipeQueue.remove(uuid);
			lastModified.removeLong(uuid);
		}

		LinkedList<ServerPlayer> availablePlayers = onlineIds.stream()
				.map(uuid -> server.getPlayerList().getPlayer(uuid))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedList::new));

		RecipeManager recipeManager = server.getRecipeManager();
		int counter = 0;

		while (counter < LIMIT_PER_TICK && !playerRecipeQueue.isEmpty() && !availablePlayers.isEmpty()) {
			ServerPlayer player = availablePlayers.removeFirst();
			UUID playerUUID = player.getUUID();

			if (playerRecipeQueue.containsKey(playerUUID)) {
				LinkedList<ResourceLocation> recipeQueue = playerRecipeQueue.get(playerUUID);

				if (recipeQueue.isEmpty()) {
					playerRecipeQueue.remove(playerUUID);
					lastModified.removeLong(playerUUID);
					continue;
				}

				ResourceLocation recipeId = recipeQueue.removeFirst();
				Optional<RecipeHolder<?>> optionalRecipe = recipeManager.byKey(recipeId);
				if (optionalRecipe.isPresent()) {
					CriteriaTriggers.RECIPE_UNLOCKED.trigger(player, optionalRecipe.get());
					counter++;
				}

				if (!recipeQueue.isEmpty()) {
					availablePlayers.addLast(player);
					lastModified.put(playerUUID, System.currentTimeMillis());
				}
				else {
					playerRecipeQueue.remove(playerUUID);
					lastModified.removeLong(playerUUID);
				}
			}
		}
	}

}
