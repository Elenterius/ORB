package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.core.OrbServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {

	@Inject(method = "tickServer", at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/MinecraftServer;tickChildren(Ljava/util/function/BooleanSupplier;)V"
	))
	private void onServerTick(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
		OrbServer.onServerTick((MinecraftServer) (Object) this);
	}

}
