package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.loading.LogMarkers;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;

public class RecipeBookManager {

	public static final Marker LOG_MARKER = MarkerFactory.getMarker("RecipeBookManager");
	public static final LevelResource DATA_DIR = new LevelResource("recipe_books");

	public static void saveRecipeBook(MinecraftServer server, ServerPlayer player) {
		long startTime = System.nanoTime();

		Path path = server.getWorldPath(DATA_DIR);
		String playerUUID = player.getStringUUID();
		CompoundTag recipeBookData = player.getRecipeBook().toNbt();

		try {
			FileUtil.createDirectoriesSafe(path);

			File tempFile = File.createTempFile(playerUUID + "-", ".dat", path.toFile());
			NbtIo.writeCompressed(recipeBookData, tempFile);

			Path newFile = path.resolve(playerUUID + ".dat");
			Path backupFile = path.resolve(playerUUID + ".dat_old");
			Util.safeReplaceFile(newFile, tempFile.toPath(), backupFile);

			Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
			ORBMod.LOGGER.info(LOG_MARKER, "Saving recipe book data for Player{name={}, uuid={}} took {}", player.getName().getString(), playerUUID, duration);
		}
		catch (Exception e) {
			ORBMod.LOGGER.error(LOG_MARKER, "Failed to save recipe book data for Player{name={}, uuid={}} to {}", player.getName().getString(), playerUUID, path, e);
		}
	}

	public static void loadRecipeBook(MinecraftServer server, ServerPlayer player) {
		long startTime = System.nanoTime();

		Path path = server.getWorldPath(DATA_DIR);
		String playerUUID = player.getStringUUID();

		CompoundTag recipeBookData = null;

		try {
			File file = path.resolve(playerUUID + ".dat").toFile();
			if (file.exists() && file.isFile()) {
				recipeBookData = NbtIo.readCompressed(file);
			}
		}
		catch (Exception exception) {
			ORBMod.LOGGER.warn(LOG_MARKER, "Failed to read recipe book data from disk for Player{name={}, uuid={}}", player.getName().getString(), playerUUID);
		}

		if (recipeBookData != null) {
			player.getRecipeBook().fromNbt(recipeBookData, server.getRecipeManager());

			Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
			ORBMod.LOGGER.info(LOG_MARKER, "Loading recipe book data for Player{name={}, uuid={}} took {}", player.getName().getString(), playerUUID, duration);
		}
		else {
			ORBMod.LOGGER.warn(LOG_MARKER, "Failed to load recipe book data for Player{name={}, uuid={}}", player.getName().getString(), playerUUID);
		}
	}

}
