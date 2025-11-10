package com.github.elenterius.orb.dev;

import com.github.elenterius.orb.core.Orb;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jspecify.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public final class DevEnvironment {

	public static final int NUMBER_OF_ITEMS = 50_000;
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Orb.MOD_ID);

	private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Orb.MOD_ID);

	private DevEnvironment() {
	}

	public static void init() {
		IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

		StringProvider provider = new StringProvider();

		for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
			String name = provider.getToolName();
			List<String> text = provider.getTextFromAlice29(3);
			ITEMS.register("dev_item_" + i, () -> new TestItem(name, text));
		}
		ITEMS.register(modEventBus);

		TABS.register("dev", () -> CreativeModeTab.builder()
				.title(Component.literal("Dev Items"))
				.icon(Items.KNOWLEDGE_BOOK::getDefaultInstance)
				.displayItems((context, output) -> ITEMS.getEntries().stream().map(RegistryObject::get).forEach(output::accept))
				.build()
		);
		TABS.register(modEventBus);
	}

	private static class TestItem extends Item {

		private final MutableComponent nameComponent;
		private final List<MutableComponent> textComponents;

		public TestItem(String name, List<String> text) {
			super(new Properties());
			textComponents = text.stream().map(Component::literal).toList();
			nameComponent = Component.literal(name);
		}

		@Override
		public Component getName(ItemStack stack) {
			return nameComponent;
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
			tooltipComponents.addAll(textComponents);
		}

	}

	private static class StringProvider {

		private final Random random = new Random(42);

		private @Nullable List<String> alice29;
		private int alice29LineNumber = 0;

		private int toolNameIndex = 0;

		private final String[] toolPrefix = {
				"", "Ancient", "Blazing", "Blessed", "Cursed", "Bleeding", "Shiny", "Gloomy", "Bright", "Dark", "Hallowed", "Hollow",
				"Broken", "Brittle", "Tough", "Glowing", "Magic", "Enchanted", "Freezing"
		};
		private final String[] toolMaterials = {
				"Ruby", "Emerald", "Sapphire", "Quartz", "Obsidian", "Amethyst",
				"Amber", "Bone", "Necrotic Bone", "Slime", "Quicksilver", "Damascus",
				"Tin", "Zinc", "Nickle", "Bronze", "Copper", "Steel", "Lead", "Silver", "Aluminium",
				"Pig Iron", "Rose Gold", "Electrum", "Manyullyn",
				"Cobalt", "Titanium", "Osmium", "Platinum", "Tungsten", "Palladium",
				"Ironwood", "Bamboo"
		};
		private final String[] toolTypes = {
				"Ingot", "Nugget", "Rod",
				"Needle", "Shears", "Fishing Rod", "Knife",
				"Pickaxe", "Shovel", "Hoe", "Axe", "Hammer", "Paxel", "Scythe", "Kama", "Mattock", "Pickdaze",
				"Excavator", "Sledge Hammer", "Vein Hammer", "Broad Axe",
				"Bow", "Crossbow", "Pistol", "Rifle", "Cannon", "Longbow",
				"War Pick", "Claws",
				"Sword", "Broadsword", "Longsword", "Rapier", "Dagger", "Cleaver", "Katana", "Saber",
				"Spear", "Javelin", "Trident", "Halberd", "Pike", "Lance",
				"Helmet", "Chestplate", "Leggings", "Boots", "Chainmail", "Shield"
		};

		public static List<String> readAllNonEmptyLines(String resourcePath) {
			try (InputStream in = ClassLoader.getSystemResourceAsStream(resourcePath)) {
				if (in == null) {
					throw new RuntimeException("Resource not found: " + resourcePath);
				}

				try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
					return reader.lines().map(String::trim).filter(line -> !line.isEmpty()).collect(Collectors.toList());
				}
			}
			catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		private String randomAlphabetString(int length) {
			return randomString("abcdefghijklmnopqrstuvwxyz", length);
		}

		private String randomString(String alphabet, int length) {
			StringBuilder sb = new StringBuilder(length);
			for (int i = 0; i < length; i++) {
				sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
			}
			return sb.toString();
		}

		private List<String> randomPangrams() {
			final String[] pangrams = """
					The quick brown fox jumps over the lazy dog.
					Waltz, bad nymph, for quick jigs vex.
					Sphinx of black quartz, judge my vow.
					How vexingly quick daft zebras jump!
					The five boxing wizards jump quickly.
					Pack my box with five dozen liquor jugs.
					""".split("\n");

			int a = random.nextInt(pangrams.length);
			int b = (a + 1 + random.nextInt(pangrams.length - 2)) % pangrams.length;
			return List.of(pangrams[a], pangrams[b]);
		}

		private String getToolName() {
			int prefixIndex = toolNameIndex / (toolTypes.length * toolMaterials.length);
			int materialIndex = (toolNameIndex / toolTypes.length) % toolMaterials.length;
			int typeIndex = toolNameIndex % toolTypes.length;

			toolNameIndex = (toolNameIndex + 1) % (toolTypes.length * toolMaterials.length * toolPrefix.length);

			return (toolPrefix[prefixIndex] + " " + toolMaterials[materialIndex] + " " + toolTypes[typeIndex]).trim();
		}

		private String getOneLineFromAlice29() {
			if (alice29 == null) {
				alice29 = readAllNonEmptyLines("canterbury_corpus/alice29.txt");
			}

			return alice29.get(alice29LineNumber++ % (alice29.size() - 1));
		}

		private List<String> getTextFromAlice29(int lineCount) {
			if (alice29 == null) {
				alice29 = readAllNonEmptyLines("canterbury_corpus/alice29.txt");
			}

			List<String> lines = new ArrayList<>();
			for (int i = 0; i < lineCount; i++) {
				lines.add(alice29.get(alice29LineNumber++ % (alice29.size() - 1)));
			}
			return lines;
		}

	}

}
