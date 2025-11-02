package com.github.elenterius.orb.init;

import com.github.elenterius.orb.ORBMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
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

	public static final int NUMBER_OF_ITEMS = 42;
	public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ORBMod.MOD_ID);

	private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ORBMod.MOD_ID);

	private DevEnvironment() {
	}

	static void init(IEventBus modEventBus) {
		TextProvider textProvider = new TextProvider();

		for (int i = 0; i < NUMBER_OF_ITEMS; i++) {
			List<String> text = textProvider.linesFromAlice29(2);
			ITEMS.register("dev_item_" + i, () -> new TestItem(text));
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

		private final List<MutableComponent> textComponents;

		public TestItem(List<String> text) {
			super(new Properties());
			textComponents = text.stream().map(Component::literal).toList();
		}

		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
			tooltipComponents.addAll(textComponents);
		}

	}

	private static class TextProvider {

		private final Random random = new Random(42);

		private @Nullable List<String> alice29;
		private int alice29LineNumber = 0;

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

		private List<String> linesFromAlice29(int count) {
			if (alice29 == null) {
				alice29 = readAllNonEmptyLines("canterbury_corpus/alice29.txt");
			}

			List<String> lines = new ArrayList<>();
			for (int i = 0; i < count; i++) {
				lines.add(alice29.get(alice29LineNumber++ % (alice29.size() - 1)));
			}
			return lines;
		}

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

	}

}
