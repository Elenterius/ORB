package com.github.elenterius.orb.core;

import net.minecraft.FileUtil;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.FileNameDateFormatter;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public final class ServerRecipeBookManager {

	public static final Marker LOG_MARKER = MarkerFactory.getMarker("ServerRecipeBookManager");
	public static final LevelResource DATA_DIR = new LevelResource("recipe_books");

	public static final String FILE_SUFFIX = ".dat";
	public static final String OLD_FILE_SUFFIX = ".dat_old";

	private static final DateTimeFormatter FORMATTER = FileNameDateFormatter.create();

	private ServerRecipeBookManager() {}

	public static void saveRecipeBook(MinecraftServer server, ServerPlayer player) {
		long startTime = System.nanoTime();

		Path path = server.getWorldPath(DATA_DIR);
		String playerUUID = player.getStringUUID();
		CompoundTag recipeBookData = player.getRecipeBook().toNbt();

		try {
			FileUtil.createDirectoriesSafe(path);

			Path tempFile = Files.createTempFile(path, playerUUID + "-", FILE_SUFFIX);
			NbtIo.writeCompressed(recipeBookData, tempFile);

			Path newFile = path.resolve(playerUUID + FILE_SUFFIX);
			Path backupFile = path.resolve(playerUUID + OLD_FILE_SUFFIX);
			Util.safeReplaceFile(newFile, tempFile, backupFile);

			Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
			Orb.LOGGER.debug(LOG_MARKER, "Saving recipe book data for Player{name={}, uuid={}} took {}", player.getName().getString(), playerUUID, duration);
		}
		catch (Exception e) {
			Orb.LOGGER.error(LOG_MARKER, "Failed to save recipe book data for Player{name={}, uuid={}} to {}", player.getName().getString(), playerUUID, path, e);
		}
	}

	public static void loadRecipeBook(MinecraftServer server, ServerPlayer player) {
		long startTime = System.nanoTime();

		Path path = server.getWorldPath(DATA_DIR);
		String playerUUID = player.getStringUUID();

		Optional<CompoundTag> recipeBookData = load(player, path, playerUUID, false).or(() -> {
			backupCorruptedData(player, path, playerUUID);
			return load(player, path, playerUUID, true);
		});

		if (recipeBookData.isPresent()) {
			player.getRecipeBook().fromNbt(recipeBookData.get(), server.getRecipeManager());

			Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
			Orb.LOGGER.debug(LOG_MARKER, "Loading recipe book data for Player{name={}, uuid={}} took {}", player.getName().getString(), playerUUID, duration);
		}
		else {
			Orb.LOGGER.warn(LOG_MARKER, "Failed to load recipe book data for Player{name={}, uuid={}}", player.getName().getString(), playerUUID);
		}
	}

	private static Optional<CompoundTag> load(ServerPlayer player, Path path, String playerUUID, boolean oldData) {
		try {
			String s = oldData ? OLD_FILE_SUFFIX : FILE_SUFFIX;
			File file = path.resolve(playerUUID + s).toFile();
			if (file.exists() && file.isFile()) {
				return Optional.of(NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap()));
			}
		}
		catch (Exception exception) {
			Orb.LOGGER.warn(LOG_MARKER, "Failed to read recipe book data from disk for Player{name={}, uuid={}}", player.getName().getString(), playerUUID);
		}

		return Optional.empty();
	}

	private static void backupCorruptedData(ServerPlayer player, Path path, String playerUUID) {
		Path originalFile = path.resolve(playerUUID + FILE_SUFFIX);
		Path corruptedFile = path.resolve(playerUUID + "_corrupted_" + LocalDateTime.now().format(FORMATTER) + FILE_SUFFIX);
		if (Files.isRegularFile(originalFile)) {
			try {
				Files.copy(originalFile, corruptedFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
			}
			catch (Exception exception) {
				Orb.LOGGER.warn(LOG_MARKER, "Failed to copy corrupted recipe book data for Player{name={}, uuid={}}", player.getName().getString(), playerUUID, exception);
			}
		}
	}

}