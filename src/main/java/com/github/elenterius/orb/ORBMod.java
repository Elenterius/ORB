package com.github.elenterius.orb;

import com.github.elenterius.orb.core.CommonHandler;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(ORBMod.MOD_ID)
public class ORBMod {

	public static final String MOD_ID = "optimized_recipe_book";

	public static final Logger LOGGER = LogUtils.getLogger();

	public ORBMod() {
		CommonHandler.onModInit();
    }

	public static ResourceLocation rl(String path) {
		//noinspection removal
		return new ResourceLocation(MOD_ID, path);
    }

}
