package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.SearchTreeUpdater;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(SearchRegistry.class)
public abstract class SearchRegistryMixin implements SearchTreeUpdater.SearchRegistryExtension {

	@Shadow
	@Final
	private Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> searchTrees;

	@Shadow
	protected abstract <T> SearchRegistry.TreeEntry<T> getSupplier(SearchRegistry.Key<T> key);

	@Override
	public @NonNull Map<SearchRegistry.Key<?>, SearchRegistry.TreeEntry<?>> ORB$getSearchTrees() {
		return searchTrees;
	}

	@Inject(method = "getTree", at = @At(value = "HEAD"), cancellable = true)
	private <T> void onGetTree(SearchRegistry.Key<T> key, CallbackInfoReturnable<SearchTree<T>> cir) {
		SearchRegistry.TreeEntry<T> entry = getSupplier(key);
		if (entry instanceof SearchTreeUpdater.TreeEntryExtension) {
			//noinspection unchecked
			SearchTreeUpdater.TreeEntryExtension<@NonNull T> treeEntry = (SearchTreeUpdater.TreeEntryExtension<@NonNull T>) entry;
			cir.setReturnValue(treeEntry.ORB$AtomicTree().get());
		}
	}

}
