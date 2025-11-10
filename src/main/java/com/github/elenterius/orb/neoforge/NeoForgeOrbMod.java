package com.github.elenterius.orb.neoforge;

import com.github.elenterius.orb.core.Orb;
import net.minecraftforge.fml.common.Mod;

@Mod(Orb.MOD_ID)
public class NeoForgeOrbMod {

	public NeoForgeOrbMod() {
		CommonHandler.onModInit();
	}

}
