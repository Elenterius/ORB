package com.github.elenterius.orb.core;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;

public final class OrbServer {

	private static final RecipeUnlockedTriggerManager RECIPE_UNLOCKED_TRIGGERS = new RecipeUnlockedTriggerManager();

	private OrbServer() {}

	public static void onServerTick(MinecraftServer server) {
		RECIPE_UNLOCKED_TRIGGERS.tick(server);
	}

	public static void enqueueRecipeUnlockedTrigger(ServerPlayer player, Recipe<?> recipe) {
		RECIPE_UNLOCKED_TRIGGERS.enqueue(player, recipe);
	}

}
