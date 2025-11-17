package com.github.elenterius.orb.core;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public final class Orb {

	public static final String MOD_ID = "orb";

	public static final Logger LOGGER = LogUtils.getLogger();
	public static final Marker TIMER_MARKER = MarkerFactory.getMarker("Timer");

	private Orb() {}

	public static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}

	public static Component translatable(String prefix, String suffix) {
		return Component.translatable("%s.%s.%s".formatted(prefix, Orb.MOD_ID, suffix));
	}

}
