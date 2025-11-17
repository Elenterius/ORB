package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.util.UnboundedHashMapCodec;
import com.mojang.serialization.Codec;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerAdvancements.Data.class)
public abstract class PlayerAdvancements$DataMixin {

	@Shadow
	@Final
	@Mutable
	public static Codec<PlayerAdvancements.Data> CODEC;

	/**
	 * Decreases load times for player advancements with a lot of entries (e.g. joining a world with 50k+ recipe advancements)
	 */
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void onStaticInit(CallbackInfo ci) {
		CODEC = new UnboundedHashMapCodec<>(ResourceLocation.CODEC, AdvancementProgress.CODEC).xmap(PlayerAdvancements.Data::new, PlayerAdvancements.Data::map);
	}

}
