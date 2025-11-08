package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.ClientHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

	@Unique
	private static final long ORB$DEBOUNCE_DELAY_MS = 200;

	@Unique
	private long ORB$LastTime = 0;

	@Unique
	private boolean ORB$IsSearchUpdateDebounced = false;

	@Shadow
	@Final
	private RecipeBookPage recipeBookPage;

	@Shadow
	protected abstract void checkSearchStringUpdate();

	@WrapMethod(method = "checkSearchStringUpdate")
	private void onSearchStringUpdate(Operation<Void> original) {
		long elapsedTime = System.currentTimeMillis() - ORB$LastTime;

		//debounce to mitigate excessive updating of the recipe pages
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

	@WrapOperation(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(Z)V"))
	private void onInitVisuals(RecipeBookComponent instance, boolean resetPageNumber, Operation<Void> original) {
		// update with empty placeholder collections to prevent NPE and Divide By Zero errors with recipe page buttons
		recipeBookPage.updateCollections(List.of(), resetPageNumber);

		ClientHandler.asyncUpdateRecipeBookPage(ORB$self(), resetPageNumber, true);
		//important: we need to make sure to update the tabs as well otherwise there won't be any tabs visible
	}

	@WrapMethod(method = "updateCollections")
	private void onUpdateCollections(boolean resetPageNumber, Operation<Void> original) {
		ClientHandler.asyncUpdateRecipeBookPage(ORB$self(), resetPageNumber, false);
	}

	@Unique
	private RecipeBookComponent ORB$self() {
		return (RecipeBookComponent) (Object) this;
	}

}
