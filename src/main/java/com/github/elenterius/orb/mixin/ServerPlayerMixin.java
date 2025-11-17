package com.github.elenterius.orb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

	@Unique
	private static final CompoundTag EMPTY_TAG = new CompoundTag();

	@WrapOperation(
			method = "addAdditionalSaveData",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/stats/ServerRecipeBook;toNbt()Lnet/minecraft/nbt/CompoundTag;")
	)
	private CompoundTag preventRecipeBookToNBTCall(ServerRecipeBook instance, Operation<CompoundTag> original) {
		return EMPTY_TAG;
	}

	@WrapOperation(
			method = "addAdditionalSaveData",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;put(Ljava/lang/String;Lnet/minecraft/nbt/Tag;)Lnet/minecraft/nbt/Tag;")
	)
	private Tag onlySaveIfNotRecipeBook(CompoundTag instance, String key, Tag value, Operation<Tag> original) {
		if (key.equals(ServerRecipeBook.RECIPE_BOOK_TAG)) return instance;
		return original.call(instance, key, value);
	}

	@WrapOperation(
			method = "readAdditionalSaveData",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;contains(Ljava/lang/String;I)Z")
	)
	private boolean onlyReadIfNotRecipeBook(CompoundTag instance, String key, int tagType, Operation<Boolean> original) {
		if (key.equals(ServerRecipeBook.RECIPE_BOOK_TAG)) return false;
		return original.call(instance, key, tagType);
	}

}
