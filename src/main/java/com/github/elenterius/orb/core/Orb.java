package com.github.elenterius.orb.core;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class Orb {

	public static final String MOD_ID = "orb";

	public static final Logger LOGGER = LogUtils.getLogger();

	private Orb() {}

	public static ResourceLocation rl(String path) {
		return new ResourceLocation(MOD_ID, path);
	}

}
