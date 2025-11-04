package com.github.elenterius.orb.mixin.client;

import net.minecraft.client.searchtree.FullTextSearchTree;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FullTextSearchTree.class)
public abstract class FullTextSearchTreeMixin {

	//	@Inject(method = "refresh", at = @At(value = "HEAD"))
	//	private void onPreRefresh(CallbackInfo ci, @Share("startTime") LocalLongRef argRef) {
	//		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Refreshing Internal SearchTree...");
	//		argRef.set(System.nanoTime());
	//	}
	//
	//	@Inject(method = "refresh", at = @At(value = "TAIL"))
	//	private void onPostRefresh(CallbackInfo ci, @Share("startTime") LocalLongRef argRef) {
	//		long elapsedNanos = System.nanoTime() - argRef.get();
	//		ORBMod.LOGGER.info(SearchTreeUpdater.LOG_MARKER, "Internal SearchTree Refresh took: {}", Duration.ofNanos(elapsedNanos));
	//	}

}
