package com.github.elenterius.orb.mixin.client;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.function.Consumer;

@Mixin(RecipeBookPage.class)
public interface RecipeBookPageAccessor {

	@Accessor
	List<RecipeCollection> getRecipeCollections();

	@Invoker
	void callListButtons(Consumer<AbstractWidget> consumer);

}
