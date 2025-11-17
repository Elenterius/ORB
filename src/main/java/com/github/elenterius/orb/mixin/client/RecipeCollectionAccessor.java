package com.github.elenterius.orb.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(RecipeCollection.class)
public interface RecipeCollectionAccessor {

	@Accessor
	Set<RecipeHolder<?>> getCraftable();

	@Mutable
	@Accessor
	void setCraftable(final Set<RecipeHolder<?>> recipes);

	@Accessor
	Set<RecipeHolder<?>> getFitsDimensions();

	@Mutable
	@Accessor
	void setFitsDimensions(final Set<RecipeHolder<?>> recipes);

}
