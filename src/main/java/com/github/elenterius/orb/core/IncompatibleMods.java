package com.github.elenterius.orb.core;

public enum IncompatibleMods {
	NERB("nerb", "Not Enough Recipe Book", true);

	public final String modId;
	public final String modName;
	public final boolean isFatal;

	IncompatibleMods(String modId, String modName, boolean isFatal) {
		this.modId = modId;
		this.modName = modName;
		this.isFatal = isFatal;
	}

}
