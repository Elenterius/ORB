package com.github.elenterius.orb.core;

import com.github.elenterius.orb.ORBMod;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;

@Mod.EventBusSubscriber(modid = ORBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientHandler {

	private static final SearchTreeUpdater SEARCH_TREE_UPDATER = new SearchTreeUpdater();
	private static final RecipeBookPageUpdater RECIPE_BOOK_PAGE_UPDATER = new RecipeBookPageUpdater();

	private ClientHandler() {}

	@SubscribeEvent
	public static void onClientSetup(final FMLClientSetupEvent event) {
		// unnecessary as the JVM should terminate, this is just for the sake of sanity
		// note: shutdown hooks only work on the client side
		Runtime.getRuntime().addShutdownHook(new Thread(SEARCH_TREE_UPDATER::shutdown));
		Runtime.getRuntime().addShutdownHook(new Thread(RECIPE_BOOK_PAGE_UPDATER::shutdown));
	}

	public static <T> void startSearchTreeRebuild(SearchRegistry.TreeEntry<T> treeEntry, List<T> values) {
		SEARCH_TREE_UPDATER.submitRebuild(treeEntry, values);
	}

	public static <T> void startSearchTreeRefresh(SearchRegistry.TreeEntry<T> treeEntry) {
		SEARCH_TREE_UPDATER.submitRefresh(treeEntry);
	}

	public static void asyncUpdateRecipeBookPage(RecipeBookComponent recipeBookComponent, boolean resetPageNumber, boolean updateTabs) {
		RECIPE_BOOK_PAGE_UPDATER.asyncUpdate(recipeBookComponent, resetPageNumber, updateTabs);
	}

}
