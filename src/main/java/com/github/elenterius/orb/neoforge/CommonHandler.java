package com.github.elenterius.orb.neoforge;

import com.github.elenterius.orb.core.IncompatibleMods;
import com.github.elenterius.orb.core.Orb;
import com.github.elenterius.orb.dev.DevEnvironment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Orb.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class CommonHandler {

	public static void onModInit() {
		validateMods();

		if (!FMLEnvironment.production) {
			Dev.initEnvironment();
		}
	}

	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event) {
		validateMods();
	}

	static void validateMods() {
		List<IncompatibleMods> fatalIncompatibilities = new ArrayList<>();

		for (IncompatibleMods incompatibility : IncompatibleMods.values()) {
			if (ModList.get().isLoaded(incompatibility.modId)) {
				if (incompatibility.isFatal) {
					fatalIncompatibilities.add(incompatibility);
				}
				Orb.LOGGER.warn("Orb mod has detected minor incompatible mod: {} ({})", incompatibility.modId, incompatibility.modName);
			}
		}

		if (!fatalIncompatibilities.isEmpty()) {
			String plural = fatalIncompatibilities.size() > 1 ? "s" : "";
			String mods = fatalIncompatibilities.stream().map(x -> "%s (%s)".formatted(x.modId, x.modName)).collect(Collectors.joining(","));
			throw new RuntimeException("Orb mod has detected fatally incompatible mod%s: %s".formatted(plural, mods));
		}
	}

	private static class Dev {
		private static void initEnvironment() {
			DevEnvironment.init();
		}
	}

}
