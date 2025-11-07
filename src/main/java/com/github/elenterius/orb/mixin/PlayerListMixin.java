package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.core.ServerRecipeBookManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

	@Shadow
	@Final
	private MinecraftServer server;

	@Inject(method = "save", at = @At("TAIL"))
	private void onSavePlayer(ServerPlayer player, CallbackInfo ci) {
		ServerRecipeBookManager.saveRecipeBook(server, player);
	}

	@Inject(method = "load", at = @At("TAIL"))
	private void onLoadPlayer(ServerPlayer player, CallbackInfoReturnable<CompoundTag> cir) {
		ServerRecipeBookManager.loadRecipeBook(server, player);
	}

}
