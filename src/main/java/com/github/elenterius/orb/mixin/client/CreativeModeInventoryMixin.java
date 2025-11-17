package com.github.elenterius.orb.mixin.client;

import com.github.elenterius.orb.core.OrbClient;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.client.CreativeModeTabSearchRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Set;
import java.util.function.IntSupplier;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

	@Shadow
	private static CreativeModeTab selectedTab;

	@Shadow
	protected abstract void refreshSearchResults();

	@Shadow
	@Final
	private Set<TagKey<Item>> visibleTags;
	@Shadow
	private float scrollOffs;

	@Shadow
	private EditBox searchBox;
	@Unique
	private IntSupplier orb$NameIndexingProgress = OrbClient.getIndexingProgress(null);

	@Unique
	private IntSupplier orb$TagIndexingProgress = OrbClient.getIndexingProgress(null);

	@Unique
	private boolean orb$IsForcedUpdateRequired = false;

	@Unique
	private float orb$Time = (float) Math.random() * OrbClient.LOADING_MESSAGES.length;

	public CreativeModeInventoryMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
		super(menu, playerInventory, title);
	}

	@Inject(
			method = "containerTick",
			at = @At(
					value = "INVOKE", shift = At.Shift.AFTER,
					target = "Lnet/minecraft/client/gui/screens/inventory/CreativeModeInventoryScreen;tryRefreshInvalidatedTabs(Lnet/minecraft/world/flag/FeatureFlagSet;ZLnet/minecraft/core/HolderLookup$Provider;)V"
			)
	)
	private void onTick(CallbackInfo ci) {
		SessionSearchTrees.Key nameSearchKey = CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab);
		SessionSearchTrees.Key tagSearchKey = CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab);

		if (nameSearchKey != null && CreativeModeTabSearchRegistry.getNameSearchTree(nameSearchKey).isDone()) {
			OrbClient.getProgressTracker(nameSearchKey).set(100);
		}

		if (tagSearchKey != null && CreativeModeTabSearchRegistry.getTagSearchTree(tagSearchKey).isDone()) {
			OrbClient.getProgressTracker(tagSearchKey).set(100);
		}

		orb$NameIndexingProgress = OrbClient.getIndexingProgress(nameSearchKey);
		orb$TagIndexingProgress = OrbClient.getIndexingProgress(tagSearchKey);

		if (orb$IsForcedUpdateRequired && selectedTab.hasSearchBar() && orb$NameIndexingProgress.getAsInt() >= 100 && orb$TagIndexingProgress.getAsInt() >= 100) {
			refreshSearchResults();
		}
	}

	@WrapMethod(method = "refreshSearchResults")
	private void onRefreshSearchResults(Operation<Void> original) {
		orb$NameIndexingProgress = OrbClient.getIndexingProgress(CreativeModeTabSearchRegistry.getNameSearchKey(selectedTab));
		orb$TagIndexingProgress = OrbClient.getIndexingProgress(CreativeModeTabSearchRegistry.getTagSearchKey(selectedTab));

		if (orb$NameIndexingProgress.getAsInt() >= 100 && orb$TagIndexingProgress.getAsInt() >= 100) {
			orb$IsForcedUpdateRequired = false;
			original.call();
			return;
		}

		if (!selectedTab.hasSearchBar()) return;

		menu.items.clear();
		visibleTags.clear();
		scrollOffs = 0f;
		menu.scrollTo(0f);

		orb$IsForcedUpdateRequired = true;
	}

	@Inject(method = "renderBg", at = @At("TAIL"))
	private void onRenderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
		if (minecraft == null) return;
		if (!selectedTab.hasSearchBar()) return;
		if (!menu.items.isEmpty()) return;

		String query = searchBox.getValue();
		if (query.isEmpty()) return;
		if (query.startsWith("#") && orb$TagIndexingProgress.getAsInt() >= 100) return;
		else if (orb$NameIndexingProgress.getAsInt() >= 100) return;

		int x = leftPos + 8;
		int y = topPos + 17;
		int width = 162;
		int height = 90;
		guiGraphics.fill(x, y, x + width, y + height, 0xFF_373737);

		int x1 = x + width / 2;
		int y1 = y + height / 2 - minecraft.font.lineHeight * 2;

		guiGraphics.drawCenteredString(minecraft.font, OrbClient.INDEXING_TITLE, x1, y1, 0xFF_FAFAFA);

		orb$Time += partialTick;
		int y2 = y1 + minecraft.font.lineHeight + 4;

		Component message = OrbClient.LOADING_MESSAGES[Mth.floor(orb$Time / OrbClient.LOADING_MESSAGE_DURATION) % OrbClient.LOADING_MESSAGES.length];
		List<FormattedCharSequence> lines = minecraft.font.split(message, width - 4);

		for (int j = 0; j < lines.size(); j++) {
			FormattedCharSequence line = lines.get(j);
			guiGraphics.drawCenteredString(minecraft.font, line, x1, y2 + j * (minecraft.font.lineHeight + 2), 0xFF_9F9F9F);
		}
	}

}
