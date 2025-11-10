package com.github.elenterius.orb.neoforge;

import com.github.elenterius.orb.core.Orb;
import com.github.elenterius.orb.core.OrbClient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Orb.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientHandler {

	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		OrbClient.registerShutdownHooks();
	}

}
