package com.github.elenterius.orb.core;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

public class RecipeUnlockedTriggerManager {

	private static final int LIMIT_PER_TICK = 500; // 10,000 triggers in 20 ticks
	private static final long CACHE_DURATION = Duration.ofMinutes(10).toMillis();

	private static final Map<UUID, LinkedList<ResourceLocation>> PLAYER_TO_RECIPE_QUEUE = new HashMap<>();
	private static final Object2LongMap<UUID> LAST_MODIFIED = new Object2LongOpenHashMap<>();

	public static void enqueue(ServerPlayer player, Recipe<?> recipe) {
		UUID uuid = player.getUUID();
		PLAYER_TO_RECIPE_QUEUE.computeIfAbsent(uuid, id -> new LinkedList<>()).addLast(recipe.getId());
		LAST_MODIFIED.put(uuid, System.currentTimeMillis());
	}

	static void tick(MinecraftServer server) {
		if (PLAYER_TO_RECIPE_QUEUE.isEmpty()) return;

		Set<UUID> onlineIds = PLAYER_TO_RECIPE_QUEUE.keySet().stream()
				.filter(uuid -> server.getPlayerList().getPlayer(uuid) != null)
				.collect(Collectors.toSet());

		// remove expired caches for offline players
		final long currentTime = System.currentTimeMillis();
		List<UUID> expiredIds = PLAYER_TO_RECIPE_QUEUE.keySet().stream()
				.filter(uuid -> !onlineIds.contains(uuid) && currentTime - LAST_MODIFIED.getLong(uuid) >= CACHE_DURATION)
				.toList();
		for (UUID uuid : expiredIds) {
			PLAYER_TO_RECIPE_QUEUE.remove(uuid);
			LAST_MODIFIED.removeLong(uuid);
		}

		LinkedList<ServerPlayer> availablePlayers = onlineIds.stream()
				.map(uuid -> server.getPlayerList().getPlayer(uuid))
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(LinkedList::new));

		RecipeManager recipeManager = server.getRecipeManager();
		int counter = 0;

		while (counter < LIMIT_PER_TICK && !PLAYER_TO_RECIPE_QUEUE.isEmpty() && !availablePlayers.isEmpty()) {
			ServerPlayer player = availablePlayers.removeFirst();
			UUID playerUUID = player.getUUID();

			if (PLAYER_TO_RECIPE_QUEUE.containsKey(playerUUID)) {
				LinkedList<ResourceLocation> recipeQueue = PLAYER_TO_RECIPE_QUEUE.get(playerUUID);

				if (recipeQueue.isEmpty()) {
					PLAYER_TO_RECIPE_QUEUE.remove(playerUUID);
					LAST_MODIFIED.removeLong(playerUUID);
					continue;
				}

				ResourceLocation recipeId = recipeQueue.removeFirst();
				Optional<? extends Recipe<?>> optionalRecipe = recipeManager.byKey(recipeId);
				if (optionalRecipe.isPresent()) {
					CriteriaTriggers.RECIPE_UNLOCKED.trigger(player, optionalRecipe.get());
					counter++;
				}

				if (!recipeQueue.isEmpty()) {
					availablePlayers.addLast(player);
					LAST_MODIFIED.put(playerUUID, System.currentTimeMillis());
				}
				else {
					PLAYER_TO_RECIPE_QUEUE.remove(playerUUID);
					LAST_MODIFIED.removeLong(playerUUID);
				}
			}
		}
	}

}
