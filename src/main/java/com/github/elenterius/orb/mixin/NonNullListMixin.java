package com.github.elenterius.orb.mixin;

import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
		//this makes clearing of mutable NonNullList significantly faster (can be up to 99% faster for 50K+ elements in Creative Search Menu)
		if (defaultValue == null && list instanceof ArrayList<?>) {
			list.clear();
			ci.cancel();
		}
	}

}
