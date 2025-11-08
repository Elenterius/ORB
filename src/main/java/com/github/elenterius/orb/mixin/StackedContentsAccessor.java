package com.github.elenterius.orb.mixin;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import net.minecraft.world.entity.player.StackedContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StackedContents.class)
public interface StackedContentsAccessor {

	@Mutable
	@Accessor
	void setContents(final Int2IntMap map);

}
