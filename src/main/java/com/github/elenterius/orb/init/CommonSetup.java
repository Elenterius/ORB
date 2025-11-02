package com.github.elenterius.orb.init;

import com.github.elenterius.orb.ORBMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = ORBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonSetup {

	public static void onModInit() {
		if (!FMLEnvironment.production) {
			//noinspection removal
			DevEnvironment.init(FMLJavaModLoadingContext.get().getModEventBus());
		}
	}

	@SubscribeEvent
	public static void onCommonSetup(final FMLCommonSetupEvent event) {
	}

}
