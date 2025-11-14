package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.OrbClient;
import com.github.elenterius.orb.core.RecipeBookPageUpdater;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.IntSupplier;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin implements RecipeBookPageUpdater.RecipeBookComponentExtension {

	@Unique
	private static final int CONTAINER_WIDTH = 25 * 5;
	@Unique
	private static final int CONTAINER_HEIGHT = 25 * 4;

	@Unique
	private final IntSupplier orb$IndexingProgress = OrbClient.getIndexingProgress(SearchRegistry.RECIPE_COLLECTIONS);

	@Unique
	private final AtomicReference<RecipeBookPageUpdater.PageUpdate> orb$AtomicPageUpdate = new AtomicReference<>();

	@Unique
	private boolean orb$IsForcedUpdateRequired = false;

	@Unique
	private boolean orb$Initialized = false;

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

	@Shadow
	@Nullable
	private EditBox searchBox;

	@Override
	public @NonNull AtomicReference<RecipeBookPageUpdater.PageUpdate> orb$getAtomicPageUpdate() {
		return orb$AtomicPageUpdate;
	}

	@WrapOperation(method = "initVisuals", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;updateCollections(Z)V"))
	private void onInitVisuals(RecipeBookComponent instance, boolean resetPageNumber, Operation<Void> original) {
		if (!orb$Initialized) {
			// do first update with invisible buttons to prevent NPE and Divide By Zero errors because RecipeButton doesn't account for no recipe collections or no craftable recipe
			((RecipeBookPageAccessor) recipeBookPage).callListButtons(widget -> widget.visible = false);
		}
		else {
			// when closing/opening the recipe book inside the gui invalidate the recipe buttons to prevent errors
			((RecipeBookPageAccessor) recipeBookPage).callListButtons(widget -> {
				if (widget instanceof RecipeBookPageUpdater.RecipeButtonExtension extension) {
					extension.orb$invalidate();
				}
			});
		}

		OrbClient.asyncUpdateRecipeBookPage(orb$Self(), resetPageNumber, () -> {
			updateTabs();
			orb$Initialized = true;
		});
	}

	@WrapMethod(method = "updateCollections")
	private void onUpdateCollections(boolean resetPageNumber, Operation<Void> original) {
		((RecipeBookPageAccessor) recipeBookPage).callListButtons(widget -> {
			if (widget instanceof RecipeBookPageUpdater.RecipeButtonExtension extension) {
				extension.orb$invalidate();
			}
		});
		if (orb$IndexingProgress.getAsInt() < 100) {
			orb$IsForcedUpdateRequired = true;
		}
		OrbClient.asyncUpdateRecipeBookPage(orb$Self(), resetPageNumber);
	}

	@WrapMethod(method = "checkSearchStringUpdate")
	private void onSearchStringUpdate(Operation<Void> original) {
		long elapsedTime = System.currentTimeMillis() - orb$SearchTimestamp;

		//debounce to mitigate excessive updating of the recipe pages
		if (elapsedTime >= OrbClient.DEBOUNCE_DELAY_MS) {
			if (orb$IndexingProgress.getAsInt() >= 100) {
				original.call();
			}
			else {
				orb$IsForcedUpdateRequired = true;
			}
			orb$IsSearchDebounced = false;
			orb$SearchTimestamp = System.currentTimeMillis();
		}
		else {
			orb$IsSearchDebounced = true;
		}
	}

	@Inject(method = "tick", at = @At(value = "HEAD"))
	private void onpPreTick(CallbackInfo ci) {
		RecipeBookPageUpdater.PageUpdate pageUpdate = orb$AtomicPageUpdate.getAndSet(null);
		if (pageUpdate != null) {
			if (orb$IndexingProgress.getAsInt() >= 100) {
				orb$IsForcedUpdateRequired = false;
			}
			pageUpdate.apply(orb$Self());
		}
		else if (orb$IsForcedUpdateRequired && orb$IndexingProgress.getAsInt() >= 100) {
			orb$IsForcedUpdateRequired = false;
			OrbClient.asyncUpdateRecipeBookPage(orb$Self(), false);
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

	@WrapMethod(method = "mouseClicked")
	private boolean onMouseClicked(double mouseX, double mouseY, int button, Operation<Boolean> original) {
		if (orb$Initialized) {
			return original.call(mouseX, mouseY, button);
		}
		return false;
	}

	@WrapOperation(method = "renderTooltip", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
	private void onRenderTooltip(RecipeBookPage instance, GuiGraphics guiGraphics, int mouseX, int mouseY, Operation<Void> original) {
		if (orb$Initialized) {
			original.call(instance, guiGraphics, mouseX, mouseY);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
	private void onRenderSearchBox(EditBox instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		if (orb$Initialized) {
			original.call(instance, guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/StateSwitchingButton;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
	private void onRenderFilterButton(StateSwitchingButton instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		if (orb$Initialized) {
			original.call(instance, guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	@WrapOperation(method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookTabButton;render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V")
	)
	private void onRenderTabs(RecipeBookTabButton instance, GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		if (orb$Initialized) {
			original.call(instance, guiGraphics, mouseX, mouseY, partialTick);
		}
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookPage;render(Lnet/minecraft/client/gui/GuiGraphics;IIIIF)V"))
	private void onRenderPage(RecipeBookPage instance, GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY, float partialTick, Operation<Void> original) {
		if (orb$Initialized && (!orb$HasSearchQuery() || orb$IndexingProgress.getAsInt() >= 100)) {
			original.call(instance, guiGraphics, x, y, mouseX, mouseY, partialTick);
			return;
		}

		int x1 = x + 11 + CONTAINER_WIDTH / 2;
		int y1 = y + 31 + CONTAINER_HEIGHT / 2 - minecraft.font.lineHeight;

		//guiGraphics.blit(TEXTURE, x1, y1 - minecraft.font.lineHeight / 2 - TEXTURE_V_HEIGHT, 0, 0, TEXTURE_U_WIDTH, TEXTURE_V_HEIGHT, 128, 128);

		guiGraphics.drawCenteredString(minecraft.font, !orb$Initialized ? OrbClient.INITIALIZING_TITLE : OrbClient.LOADING_TITLE, x1, y1, 0xFF_FAFAFA);

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
	private boolean orb$HasSearchQuery() {
		return searchBox != null && !searchBox.getValue().isEmpty();
	}

	@Unique
	private RecipeBookComponent orb$Self() {
		return (RecipeBookComponent) (Object) this;
	}

}
