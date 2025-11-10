package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.OrbClient;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.IntSupplier;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

	@Unique
	private static final int CONTAINER_WIDTH = 25 * 5;
	@Unique
	private static final int CONTAINER_HEIGHT = 25 * 4;

	@Unique
	private final IntSupplier orb$IndexingProgress = OrbClient.getIndexingProgress(SearchRegistry.RECIPE_COLLECTIONS);

	@Unique
	private boolean orb$initialized = false;

	@Unique
	private float orb$Time = (float) Math.random() * OrbClient.LOADING_MESSAGES.length;

	@Unique
	private long orb$SearchTimestamp = 0;

	@Unique
	private boolean orb$IsSearchDebounced = false;

	@Shadow
	@Final
	private RecipeBookPage recipeBookPage;

	@Shadow
	protected abstract void checkSearchStringUpdate();

	@Shadow
	protected Minecraft minecraft;

	@Shadow
	protected abstract void updateTabs();

	@WrapOperation(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(Z)V"))
	private void onInitVisuals(RecipeBookComponent instance, boolean resetPageNumber, Operation<Void> original) {
		// update with empty placeholder collections to prevent NPE and Divide By Zero errors with recipe page buttons
		recipeBookPage.updateCollections(List.of(), resetPageNumber);

		OrbClient.asyncUpdateRecipeBookPage(ORB$self(), resetPageNumber, () -> {
			updateTabs();
			orb$initialized = true;
		});
	}

	@WrapMethod(method = "updateCollections")
	private void onUpdateCollections(boolean resetPageNumber, Operation<Void> original) {
		OrbClient.asyncUpdateRecipeBookPage(ORB$self(), resetPageNumber);
	}

	@WrapMethod(method = "checkSearchStringUpdate")
	private void onSearchStringUpdate(Operation<Void> original) {
		long elapsedTime = System.currentTimeMillis() - orb$SearchTimestamp;

		//debounce to mitigate excessive updating of the recipe pages
		if (elapsedTime >= OrbClient.DEBOUNCE_DELAY_MS) {
			original.call();
			orb$IsSearchDebounced = false;
			orb$SearchTimestamp = System.currentTimeMillis();
		}
		else {
			orb$IsSearchDebounced = true;
		}
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/gui/components/EditBox;tick()V"))
	private void onVisibleTabTick(CallbackInfo ci) {
		if (orb$IsSearchDebounced) {
			checkSearchStringUpdate();
		}
		else {
			orb$SearchTimestamp = System.currentTimeMillis();
		}
	}

	@WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;mouseClicked(DDIIIII)Z"))
	private boolean onMouseClicked(RecipeBookPage instance, double mouseX, double mouseY, int button, int x, int y, int width, int height, Operation<Boolean> original) {
		if (orb$initialized && orb$IndexingProgress.getAsInt() >= 100) {
			return original.call(instance, mouseX, mouseY, button, x, y, width, height);
		}
		return false;
	}

	@WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
	private void onRenderTooltip(RecipeBookPage instance, GuiGraphics guiGraphics, int mouseX, int mouseY, Operation<Void> original) {
		if (orb$initialized && orb$IndexingProgress.getAsInt() >= 100) {
			original.call(instance, guiGraphics, mouseX, mouseY);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;render(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"))
	private void onRender(RecipeBookPage instance, GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		if (orb$initialized && orb$IndexingProgress.getAsInt() >= 100) {
			original.call(instance, guiGraphics, x, y, mouseX, mouseY, partialTick);
			return;
		}

		int x1 = x + 11 + CONTAINER_WIDTH / 2;
		int y1 = y + 31 + CONTAINER_HEIGHT / 2 - minecraft.font.lineHeight;

		//guiGraphics.blit(TEXTURE, x1, y1 - minecraft.font.lineHeight / 2 - TEXTURE_V_HEIGHT, 0, 0, TEXTURE_U_WIDTH, TEXTURE_V_HEIGHT, 128, 128);

		guiGraphics.drawCenteredString(minecraft.font, OrbClient.LOADING_TITLE, x1, y1, 0xFF_FAFAFA);

		orb$Time += partialTick;
		int y2 = y1 + minecraft.font.lineHeight + 4;

		Component message = OrbClient.LOADING_MESSAGES[Mth.floor(orb$Time / OrbClient.LOADING_MESSAGE_DURATION) % OrbClient.LOADING_MESSAGES.length];
		List<FormattedCharSequence> lines = minecraft.font.split(message, CONTAINER_WIDTH);

		for (int j = 0; j < lines.size(); j++) {
			FormattedCharSequence line = lines.get(j);
			guiGraphics.drawCenteredString(minecraft.font, line, x1, y2 + j * (minecraft.font.lineHeight + 2), 0xFF_9F9F9F);
		}
	}

	@Unique
	private RecipeBookComponent ORB$self() {
		return (RecipeBookComponent) (Object) this;
	}

}
