package com.github.elenterius.orb.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

	@Shadow
	protected abstract void checkSearchStringUpdate();

	@Unique
	private static final long ORB$DEBOUNCE_DELAY_MS = 250;

	@Unique
	private long ORB$LastTime = 0;

	@Unique
	private boolean ORB$IsSearchUpdateDebounced = false;

	@WrapMethod(method = "checkSearchStringUpdate")
	private void onSearchStringUpdate(Operation<Void> original) {
		long elapsedTime = System.currentTimeMillis() - ORB$LastTime;

		//debounce to mitigate calling of expensive method `updateCollections() -> StackedContents#canCraft()`
		if (elapsedTime >= ORB$DEBOUNCE_DELAY_MS) {
			original.call();
			ORB$IsSearchUpdateDebounced = false;
			ORB$LastTime = System.currentTimeMillis();
		}
		else {
			ORB$IsSearchUpdateDebounced = true;
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/components/EditBox;tick()V"))
	private void onVisibleTabTick(CallbackInfo ci) {
		if (ORB$IsSearchUpdateDebounced) {
			checkSearchStringUpdate();
		}
		else {
			ORB$LastTime = System.currentTimeMillis();
		}
	}

}
