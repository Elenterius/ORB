package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.core.ClientHandler;
import com.github.elenterius.orb.core.SearchTreeUpdater;
import net.minecraft.client.searchtree.RefreshableSearchTree;
import net.minecraft.client.searchtree.SearchRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

//@Mixin(targets = {"net.minecraft.client.searchtree.SearchRegistry$TreeEntry"})
@Mixin(SearchRegistry.TreeEntry.class)
public abstract class TreeEntryMixin<T> implements SearchTreeUpdater.TreeEntryExtension<T> {

	@Shadow
	@Final
	private SearchRegistry.TreeBuilderSupplier<T> factory;

	@Unique
	private final AtomicReference<RefreshableSearchTree<T>> ORB$AtomicTree = new AtomicReference<>(RefreshableSearchTree.empty());

	@Override
	public AtomicReference<RefreshableSearchTree<T>> ORB$AtomicTree() {
		return ORB$AtomicTree;
	}

	@Override
	public SearchRegistry.TreeBuilderSupplier<T> ORB$getTreeFactory() {
		return factory;
	}

	@Inject(method = "populate", at = @At(value = "HEAD"), cancellable = true)
	private void onPopulate(List<T> values, CallbackInfo ci) {
		ClientHandler.startSearchTreeRebuild(this, values);
		ci.cancel();
	}

	@Inject(method = "refresh", at = @At(value = "HEAD"), cancellable = true)
	private void onRefresh(CallbackInfo ci) {
		ClientHandler.startSearchTreeRefresh(this);
		ci.cancel();
	}

	//	@Inject(method = "populate", at = @At(value = "HEAD"))
	//	private void onPrePopulate(CallbackInfo ci, @Share("startTime") LocalLongRef argRef) {
	//		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "(Re-)Building SearchTreeEntry...");
	//		argRef.set(System.nanoTime());
	//	}
	//
	//	@Inject(method = "populate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/searchtree/RefreshableSearchTree;refresh()V"))
	//	private void onPosPopulate(CallbackInfo ci, @Share("startTime") LocalLongRef argRef) {
	//		long elapsedNanos = System.nanoTime() - argRef.get();
	//		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "SearchTreeEntry (re-)build took: {}", Duration.ofNanos(elapsedNanos));
	//	}
	//
	//	@Inject(method = "refresh", at = @At(value = "HEAD"))
	//	private void onPreRefresh(CallbackInfo ci, @Share("startTime") LocalLongRef argRef) {
	//		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "ResourceManagerReloaded has reloaded. Refreshing SearchTreeEntry...");
	//		argRef.set(System.nanoTime());
	//	}
	//
	//	@Inject(method = "refresh", at = @At(value = "TAIL"))
	//	private void onPostRefresh(CallbackInfo ci, @Share("startTime") LocalLongRef argRef) {
	//		long elapsedNanos = System.nanoTime() - argRef.get();
	//		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "SearchTreeEntry Refresh took: {}", Duration.ofNanos(elapsedNanos));
	//	}

}
