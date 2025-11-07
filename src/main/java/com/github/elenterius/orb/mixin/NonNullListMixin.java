package com.github.elenterius.orb.mixin;

import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@Mixin(NonNullList.class)
public abstract class NonNullListMixin<E> {

	@Shadow
	@Final
	private List<E> list;

	@Shadow
	@Final
	@Nullable
	private E defaultValue;

	@Inject(method = "clear", at = @At(value = "HEAD"), cancellable = true)
	private void onClear(CallbackInfo ci) {
		if (defaultValue == null && ORB$fastClear(list)) {
			ci.cancel(); //cancel expensive clear operation
		}
	}

	/**
	 * This makes clearing of mutable NonNullList significantly faster for many elements in Creative Search Menu
	 */
	@Unique
	private static boolean ORB$fastClear(List<?> list) {
		if (list instanceof ArrayList<?>) {
			list.clear(); // up to 99% faster
			return true;
		}

		try {
			list.clear();
			return true;
		}
		catch (UnsupportedOperationException ignored) {
			return false;
		}
	}

}
