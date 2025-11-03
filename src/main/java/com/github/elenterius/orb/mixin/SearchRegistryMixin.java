package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.core.SearchTreeUpdater;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.searchtree.SearchTree;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SearchRegistry.class)
public abstract class SearchRegistryMixin {

	@Shadow
	protected abstract <T> SearchRegistry.TreeEntry<T> getSupplier(SearchRegistry.Key<T> key);

	@Inject(method = "getTree", at = @At(value = "HEAD"), cancellable = true)
	private <T> void onGetTree(SearchRegistry.Key<T> key, CallbackInfoReturnable<SearchTree<T>> cir) {
		SearchRegistry.TreeEntry<T> entry = getSupplier(key);
		if (entry instanceof SearchTreeUpdater.TreeEntryExtension) {
			//noinspection unchecked
			SearchTreeUpdater.TreeEntryExtension<T> treeEntry = (SearchTreeUpdater.TreeEntryExtension<T>) entry;
			cir.setReturnValue(treeEntry.ORB$AtomicTree().get());
		}
	}

}
