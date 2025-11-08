package com.github.elenterius.orb.mixin.client;

import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Set;

@Mixin(RecipeCollection.class)
public abstract class RecipeCollectionMixin {

	@Shadow
	@Final
	private List<Recipe<?>> recipes;

	@Shadow
	@Final
	private Set<Recipe<?>> fitsDimensions;

	@Shadow
	@Final
	private Set<Recipe<?>> craftable;

	//	/**
	//	 * @author Elenterius
	//	 * @reason performance
	//	 */
	//	@Overwrite
	//	public void canCraft(StackedContents handler, int width, int height, RecipeBook book) {
	//		fitsDimensions.clear();
	//		craftable.clear();
	//
	//		for (Recipe<?> recipe : recipes) {
	//			if (recipe.canCraftInDimensions(width, height) && book.contains(recipe)) {
	//				fitsDimensions.add(recipe);
	//
	//				if (handler.canCraft(recipe, null)) {
	//					craftable.add(recipe);
	//				}
	//			}
	//		}
	//	}

}
