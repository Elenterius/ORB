package com.github.elenterius.orb.mixin;

import com.github.elenterius.orb.core.OrbServer;
import com.github.elenterius.orb.core.RecipeUnlockedTriggerManager;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(ServerRecipeBook.class)
public abstract class ServerRecipeBookMixin {

	@WrapOperation(method = "addRecipes",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/advancements/critereon/RecipeUnlockedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/crafting/RecipeHolder;)V"
			)
	)
	private void bypassRecipeUnlockTrigger(RecipeUnlockedTrigger instance, ServerPlayer player, RecipeHolder<?> recipe, Operation<Void> original, Collection<RecipeHolder<?>> recipes) {
		if (recipes.size() >= RecipeUnlockedTriggerManager.LIMIT_PER_TICK) {
			OrbServer.enqueueRecipeUnlockedTrigger(player, recipe);
			return;
		}
		original.call(instance, player, recipe);
	}

}
