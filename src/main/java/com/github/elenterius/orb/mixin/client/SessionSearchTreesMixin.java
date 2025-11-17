package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.OrbClient;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(value = SessionSearchTrees.class, priority = 9999)
public abstract class SessionSearchTreesMixin {

	@Shadow
	@Final
	public static SessionSearchTrees.Key RECIPE_COLLECTIONS;

	@WrapMethod(method = "recipes")
	private SearchTree<RecipeCollection> onGetRecipes(Operation<SearchTree<RecipeCollection>> original) {
		AtomicInteger progress = OrbClient.getProgressTracker(RECIPE_COLLECTIONS);
		progress.set(0);
		SearchTree<RecipeCollection> searchTree = original.call();
		progress.set(100);
		return searchTree;
	}

	@WrapMethod(method = "updateCreativeTags(Ljava/util/List;Lnet/minecraft/client/multiplayer/SessionSearchTrees$Key;)V")
	private void onUpdateCreativeTags(List<ItemStack> items, SessionSearchTrees.Key key, Operation<Void> original) {
		AtomicInteger progress = OrbClient.getProgressTracker(key);
		progress.set(0);
		original.call(items, key);
	}

	@WrapMethod(method = "updateCreativeTooltips(Lnet/minecraft/core/HolderLookup$Provider;Ljava/util/List;Lnet/minecraft/client/multiplayer/SessionSearchTrees$Key;)V")
	private void onUpdateCreativeTooltips(HolderLookup.Provider registries, List<ItemStack> items, SessionSearchTrees.Key key, Operation<Void> original) {
		AtomicInteger progress = OrbClient.getProgressTracker(key);
		progress.set(0);
		original.call(registries, items, key);
	}

}
