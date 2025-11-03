package com.github.elenterius.orb.dev.datagen;

import com.github.elenterius.orb.ORBMod;
import com.github.elenterius.orb.dev.DevEnvironment;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.numbers.combinatorics.Combinations;

import java.util.Iterator;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = ORBMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DevDataGenerators {

	private DevDataGenerators() {
	}

	@SubscribeEvent
	public static void gatherData(final GatherDataEvent event) {
		if (FMLEnvironment.production) return;

		DataGenerator generator = event.getGenerator();
		generator.addProvider(true, new DevRecipeProvider(generator.getPackOutput()));
	}

	private static class DevRecipeProvider extends RecipeProvider {

		public DevRecipeProvider(PackOutput output) {
			super(output);
		}

		@Override
		protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
			Item[] ingredients = new Item[]{
					Items.WHITE_CONCRETE, Items.ORANGE_CONCRETE, Items.MAGENTA_CONCRETE, Items.LIGHT_BLUE_CONCRETE, Items.YELLOW_CONCRETE,
					Items.LIME_CONCRETE, Items.PINK_CONCRETE, Items.GRAY_CONCRETE, Items.LIGHT_GRAY_CONCRETE, Items.CYAN_CONCRETE, Items.PURPLE_CONCRETE,
					Items.BLUE_CONCRETE, Items.BROWN_CONCRETE, Items.GREEN_CONCRETE, Items.RED_CONCRETE, Items.BLACK_CONCRETE,

					Items.WHITE_CONCRETE_POWDER, Items.WHITE_CONCRETE_POWDER, Items.MAGENTA_CONCRETE_POWDER, Items.LIGHT_BLUE_CONCRETE_POWDER, Items.YELLOW_CONCRETE_POWDER,
					Items.LIME_CONCRETE_POWDER, Items.PINK_CONCRETE_POWDER, Items.GRAY_CONCRETE_POWDER, Items.LIGHT_GRAY_CONCRETE_POWDER, Items.CYAN_CONCRETE_POWDER, Items.PURPLE_CONCRETE_POWDER,
					Items.BLUE_CONCRETE_POWDER, Items.BROWN_CONCRETE_POWDER, Items.GREEN_CONCRETE_POWDER, Items.RED_CONCRETE_POWDER, Items.BLACK_CONCRETE_POWDER,
			};

			int craftingSlots = 9;
			Iterator<int[]> combinations = Combinations.of(ingredients.length, craftingSlots).iterator();

			DevEnvironment.ITEMS.getEntries().stream().map(RegistryObject::get).forEach(item -> {
				if (!combinations.hasNext()) throw new RuntimeException("You Must Construct Additional Pylons!");

				WorkbenchRecipeBuilder.ShapedBuilder builder = WorkbenchRecipeBuilder.shaped(RecipeCategory.MISC, item)
						.pattern("ABC").pattern("DEF").pattern("GHI");

				int[] combination = combinations.next();
				for (int i = 0; i < combination.length; i++) {
					builder.define((char) ('A' + i), ingredients[combination[i]]);
				}

				builder.unlockedBy(item).save(consumer);
			});
		}

	}

}