package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
import com.github.elenterius.orb.dev.DevEnvironment;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = ORBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonHandler {

	public static void onModInit() {
		if (!FMLEnvironment.production) {
			Dev.initEnvironment();
		}
	}

	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event) {
	}

	private static class Dev {
		private static void initEnvironment() {
			DevEnvironment.init();
		}
	}

}
