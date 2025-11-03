package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = ORBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {

	private static final SearchTreeUpdater SEARCH_TREE_UPDATER = new SearchTreeUpdater();

	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		// unnecessary
		Runtime.getRuntime().addShutdownHook(new Thread(SEARCH_TREE_UPDATER::shutdown)); //note: shutdown hooks only work on the client side
	}

	public static <T> void startSearchTreeRebuild(SearchTreeUpdater.TreeEntryExtension<T> treeEntry, List<T> values) {
		SEARCH_TREE_UPDATER.submitRebuild(treeEntry, values);
	}

	public static <T> void startSearchTreeRefresh(SearchTreeUpdater.TreeEntryExtension<T> treeEntry) {
		SEARCH_TREE_UPDATER.submitRefresh(treeEntry);
	}

}
