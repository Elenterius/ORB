package com.github.elenterius.orb.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.RecipeCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Collection;

@Mixin(RecipeCommand.class)
public abstract class RecipeCommandMixin {

	@WrapOperation(
			method = "register",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/brigadier/CommandDispatcher;register(Lcom/mojang/brigadier/builder/LiteralArgumentBuilder;)Lcom/mojang/brigadier/tree/LiteralCommandNode;"
			)
	)
	private static <S> LiteralCommandNode<S> onClear(CommandDispatcher<S> instance, LiteralArgumentBuilder<S> builder, Operation<LiteralCommandNode<S>> original) {
		//noinspection unchecked
		return original.call(instance, builder.then(
				(ArgumentBuilder<S, ?>) Commands.literal("get")
						.then(Commands.argument("targets", EntityArgument.players())
								.executes(context -> ORB$getData(context.getSource(), EntityArgument.getPlayers(context, "targets")))
						)));
	}

	@Unique
	private static int ORB$getData(CommandSourceStack source, Collection<ServerPlayer> targets) {
		for (ServerPlayer player : targets) {
			CompoundTag compoundTag = player.getRecipeBook().toNbt();
			source.sendSuccess(() -> Component.literal("%s has the following recipe book data: %s".formatted(player.getDisplayName(), NbtUtils.toPrettyComponent(compoundTag))), false);
		}
		return 1;
	}

}
