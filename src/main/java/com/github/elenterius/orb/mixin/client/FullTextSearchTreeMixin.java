package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.ORBMod;
import com.github.elenterius.orb.core.SearchTreeUpdater;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.searchtree.FullTextSearchTree;
import org.spongepowered.asm.mixin.Mixin;

import java.time.Duration;
import java.util.List;

@Mixin(FullTextSearchTree.class)
public abstract class FullTextSearchTreeMixin<T> {

	@WrapMethod(method = "searchPlainText")
	private List<T> measureSearchPlainText(String query, Operation<List<T>> original) {
		long startTime = System.nanoTime();

		List<T> result = original.call(query);

		Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
		ORBMod.LOGGER.debug(SearchTreeUpdater.SEARCH_MARKER, "Plain text search for '{}' took {}", query, duration);

		return result;
	}

	@WrapMethod(method = "searchResourceLocation")
	private List<T> measureSearchResourceLocation(String namespace, String path, Operation<List<T>> original) {
		long startTime = System.nanoTime();

		List<T> result = original.call(namespace, path);

		Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
		ORBMod.LOGGER.debug(SearchTreeUpdater.SEARCH_MARKER, "Resource location search for '{}:{}' took {}", namespace, path, duration);

		return result;
	}

}
