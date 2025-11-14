package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.RecipeBookPageUpdater;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RecipeButton.class)
public abstract class RecipeButtonMixin implements RecipeBookPageUpdater.RecipeButtonExtension {

	@Shadow
	private RecipeCollection collection;
	@Shadow
	private RecipeBook book;
	@Shadow
	private RecipeBookMenu<?> menu;

	@Unique
	private boolean orb$IsValid = false;

	@Override
	public void orb$invalidate() {
		orb$IsValid = false;
	}

	@Inject(method = "init", at = @At(value = "HEAD"))
	private void onUpdate(RecipeCollection collection, RecipeBookPage recipeBookPage, CallbackInfo ci) {
		orb$IsValid = true;
	}

	@WrapMethod(method = "getOrderedRecipes")
	private List<Recipe<?>> onGetOrderedRecipes(Operation<List<Recipe<?>>> original) {
		if (orb$IsValid) return original.call();

		List<Recipe<?>> displayRecipes = collection.getDisplayRecipes(true);

		// prevent Divide by Zero errors
		if (displayRecipes.isEmpty() || !book.isFiltering(menu)) {
			displayRecipes.addAll(collection.getDisplayRecipes(false));
		}

		return displayRecipes;
	}

}
