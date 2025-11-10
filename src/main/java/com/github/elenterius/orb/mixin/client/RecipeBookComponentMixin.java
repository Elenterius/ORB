package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.OrbClient;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.IntSupplier;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin {

	@Unique
	private static final int CONTAINER_WIDTH = 25 * 5;
	@Unique
	private static final int CONTAINER_HEIGHT = 25 * 4;

	@Unique
	private float time = (float) Math.random();

	@Unique
	private final IntSupplier indexingProgress = OrbClient.getIndexingProgress(SearchRegistry.RECIPE_COLLECTIONS);

	@Unique
	private long ORB$LastTime = 0;

	@Unique
	private boolean ORB$IsSearchUpdateDebounced = false;

	@Shadow
	@Final
	private RecipeBookPage recipeBookPage;

	@Shadow
	protected abstract void checkSearchStringUpdate();

	@Shadow
	protected Minecraft minecraft;

	@Shadow
	@Nullable
	private EditBox searchBox;

	@WrapOperation(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(Z)V"))
	private void onInitVisuals(RecipeBookComponent instance, boolean resetPageNumber, Operation<Void> original) {
		// update with empty placeholder collections to prevent NPE and Divide By Zero errors with recipe page buttons
		recipeBookPage.updateCollections(List.of(), resetPageNumber);

		OrbClient.asyncUpdateRecipeBookPage(ORB$self(), resetPageNumber, true);
		//important: we need to make sure to update the tabs as well otherwise there won't be any tabs visible
	}

	@WrapMethod(method = "updateCollections")
	private void onUpdateCollections(boolean resetPageNumber, Operation<Void> original) {
		OrbClient.asyncUpdateRecipeBookPage(ORB$self(), resetPageNumber, false);
	}

	@WrapMethod(method = "checkSearchStringUpdate")
	private void onSearchStringUpdate(Operation<Void> original) {
		long elapsedTime = System.currentTimeMillis() - ORB$LastTime;

		//debounce to mitigate excessive updating of the recipe pages
		if (elapsedTime >= OrbClient.DEBOUNCE_DELAY_MS) {
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

	@WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;mouseClicked(DDIIIII)Z"))
	private boolean onMouseClicked(RecipeBookPage instance, double mouseX, double mouseY, int button, int x, int y, int width, int height, Operation<Boolean> original) {
		boolean hasQuery = searchBox != null && !searchBox.getValue().isEmpty();
		if (hasQuery && indexingProgress.getAsInt() < 100) {
			return false;
		}

		return original.call(instance, mouseX, mouseY, button, x, y, width, height);
	}

	@WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
	private void onRenderTooltip(RecipeBookPage instance, GuiGraphics guiGraphics, int mouseX, int mouseY, Operation<Void> original) {
		boolean hasQuery = searchBox != null && !searchBox.getValue().isEmpty();
		if (hasQuery && indexingProgress.getAsInt() < 100) {
			return;
		}

		original.call(instance, guiGraphics, mouseX, mouseY);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;render(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"))
	private void onRender(RecipeBookPage instance, GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		//		boolean hasQuery = searchBox != null && !searchBox.getValue().isEmpty();

		//		if (!hasQuery || indexingProgress.getAsInt() >= 100) {
		if (indexingProgress.getAsInt() >= 100) {
			original.call(instance, guiGraphics, x, y, mouseX, mouseY, partialTick);
			return;
		}

		int x1 = x + 11 + CONTAINER_WIDTH / 2;
		int y1 = y + 31 + CONTAINER_HEIGHT / 2;

		//guiGraphics.blit(TEXTURE, x1, y1 - minecraft.font.lineHeight / 2 - TEXTURE_V_HEIGHT, 0, 0, TEXTURE_U_WIDTH, TEXTURE_V_HEIGHT, 128, 128);

		guiGraphics.drawCenteredString(minecraft.font, "Updating Search Index", x1, y1 - minecraft.font.lineHeight / 2, 0xFF_FAFAFA);

		time += partialTick;
		Component message = OrbClient.LOADING_MESSAGES[Mth.floor(time / 25f) % OrbClient.LOADING_MESSAGES.length];
		List<FormattedText> lines = minecraft.font.getSplitter().splitLines(message, CONTAINER_WIDTH, Style.EMPTY);
		int y2 = y1 + 2 + minecraft.font.lineHeight / 2;

		for (int j = 0; j < lines.size(); j++) {
			FormattedText line = lines.get(j);
			guiGraphics.drawWordWrap(minecraft.font, line, x + 11, y2 + j * (2 + minecraft.font.lineHeight), CONTAINER_WIDTH, 0xFF_9F9F9F);
		}
	}

	@Unique
	private RecipeBookComponent ORB$self() {
		return (RecipeBookComponent) (Object) this;
	}

}
