package com.forx.grid.config;

public final class ConfigHelper {
	public static void bakeClient(final net.minecraftforge.fml.config.ModConfig config) {
		ModConfig.gridColor = ConfigHolder.CLIENT.gridColor.get();
	}
}
