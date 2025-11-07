package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.core.RecipeUnlockedTriggerManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin {

	//	@Inject(method = "sendInitialRecipeBook", at = @At("HEAD"), cancellable = true)
	//	private void onLoadPlayer(ServerPlayer player, CallbackInfo ci) {
	//		ORBMod.LOGGER.debug("Cancelled initial recipe book sync to player {}", player.getName().getString());
	//		ci.cancel();
	//	}

	@WrapOperation(method = "addRecipes",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/advancements/critereon/RecipeUnlockedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/Recipe;)V"
			)
	)
	private void bypassRecipeUnlockTrigger(RecipeUnlockedTrigger instance, ServerPlayer player, Recipe<?> recipe, Operation<Void> original, Collection<Recipe<?>> recipes) {
		if (recipes.size() >= 50) {
			RecipeUnlockedTriggerManager.enqueue(player, recipe);
			return;
		}
		original.call(instance, player, recipe);
	}

}
