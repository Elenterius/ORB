package com.github.elenterius.orb.neoforge;

import com.github.elenterius.orb.core.Orb;
import com.github.elenterius.orb.core.OrbClient;
import com.github.elenterius.orb.dev.DevEnvironment;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(Orb.MOD_ID)
public class NeoForgeOrbMod {

	public NeoForgeOrbMod(IEventBus modBus, ModContainer container, Dist dist) {
		if (!FMLEnvironment.production) {
			Dev.init(modBus);
		}

		if (dist.isClient()) {
			Client.init();
		}
	}

	private static class Client {
		private static void init() {
			OrbClient.registerShutdownHooks();
		}
	}

	private static class Dev {
		private static void init(IEventBus modBus) {
			DevEnvironment.init(modBus);
		}
	}

}
