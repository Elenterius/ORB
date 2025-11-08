package com.github.elenterius.orb.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(RecipeCollection.class)
public interface RecipeCollectionAccessor {

	@Accessor
	Set<Recipe<?>> getCraftable();

	@Mutable
	@Accessor
	void setCraftable(final Set<Recipe<?>> recipes);

	@Accessor
	Set<Recipe<?>> getFitsDimensions();

	@Mutable
	@Accessor
	void setFitsDimensions(final Set<Recipe<?>> recipes);

}
