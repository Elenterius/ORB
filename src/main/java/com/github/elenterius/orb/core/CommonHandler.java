package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
import com.github.elenterius.orb.dev.DevEnvironment;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = ORBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonHandler {

	private CommonHandler() {}

	public static void onModInit() {
		if (!FMLEnvironment.production) {
			Dev.initEnvironment();
		}
	}

	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event) {
	}

	public static void onServerTick(MinecraftServer server) {
		RecipeUnlockedTriggerManager.tick(server);
	}

	private static class Dev {
		private static void initEnvironment() {
			DevEnvironment.init();
		}
	}

}
