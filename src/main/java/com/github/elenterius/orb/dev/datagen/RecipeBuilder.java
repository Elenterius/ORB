package com.github.elenterius.orb.dev.datagen;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public sealed interface RecipeBuilder<T extends RecipeBuilder<?>> permits WorkbenchRecipeBuilder.ShapedBuilder, WorkbenchRecipeBuilder.ShapelessBuilder {

	static String getRecipeFolderName(@Nullable RecipeCategory category, String modId) {
		return category != null ? category.getFolderName() : modId;
	}

	private Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(itemLike).build());
	}

	private Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
		return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
	}

	private Criterion<InventoryChangeTrigger.TriggerInstance> inventoryTrigger(ItemPredicate... predicates) {
		return CriteriaTriggers.INVENTORY_CHANGED.createCriterion(
				new InventoryChangeTrigger.TriggerInstance(Optional.empty(), InventoryChangeTrigger.TriggerInstance.Slots.ANY, List.of(predicates))
		);
	}

	private String getItemName(ItemLike itemLike) {
		return BuiltInRegistries.ITEM.getKey(itemLike.asItem()).getPath();
	}

	private String getTagName(TagKey<Item> tag) {
		return tag.location().getPath();
	}

	T unlockedBy(String name, Criterion<?> criterion);

	default T unlockedBy(String name, ItemPredicate predicate) {
		return unlockedBy(name, inventoryTrigger(predicate));
	}

	default T unlockedBy(ItemLike itemLike, Criterion<?> criterion) {
		return unlockedBy("has_" + getItemName(itemLike), criterion);
	}

	default T unlockedBy(ItemLike itemLike) {
		return unlockedBy("has_" + getItemName(itemLike), has(itemLike));
	}

	default T unlockedBy(DeferredItem<? extends Item> itemHolder) {
		Item item = itemHolder.asItem();
		return unlockedBy("has_" + getItemName(item), has(item));
	}

	default T unlockedBy(TagKey<Item> tag, Criterion<?> criterion) {
		return unlockedBy("has_" + getTagName(tag), criterion);
	}

	default T unlockedBy(TagKey<Item> tag) {
		return unlockedBy("has_" + getTagName(tag), has(tag));
	}

	default void save(RecipeOutput recipeOutput) {
		save(recipeOutput, null);
	}

	void save(RecipeOutput recipeOutput, @Nullable RecipeCategory category);

}
