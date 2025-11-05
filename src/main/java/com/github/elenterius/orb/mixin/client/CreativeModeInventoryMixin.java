package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.ORBMod;
import com.github.elenterius.orb.core.SearchTreeUpdater;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.time.Duration;
import java.util.Collection;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryMixin<T> {

	@WrapOperation(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;clear()V"))
	private void measureClearItems(NonNullList<ItemStack> instance, Operation<Void> original) {
		long startTime = System.nanoTime();

		original.call(instance);

		Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
		ORBMod.LOGGER.debug(SearchTreeUpdater.SEARCH_MARKER, "Clearing item list took {}", duration);
	}

	@WrapOperation(method = "refreshSearchResults", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/NonNullList;addAll(Ljava/util/Collection;)Z"))
	private boolean measureAddItems(NonNullList<ItemStack> instance, Collection<ItemStack> collection, Operation<Boolean> original) {
		long startTime = System.nanoTime();

		original.call(instance, collection);

		Duration duration = Duration.ofNanos(System.nanoTime() - startTime);
		ORBMod.LOGGER.debug(SearchTreeUpdater.SEARCH_MARKER, "Adding items to list took {}", duration);
		return false;
	}

}
