package com.github.elenterius.orb.mixin.client;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {

	@Accessor
	RecipeBookMenu<?> getMenu();

	@Accessor
	RecipeBookTabButton getSelectedTab();

	@Accessor
	EditBox getSearchBox();

	@Accessor
	ClientRecipeBook getBook();

	@Accessor
	RecipeBookPage getRecipeBookPage();

	@Accessor
	StackedContents getStackedContents();

	@Invoker
	void callUpdateTabs();

}
