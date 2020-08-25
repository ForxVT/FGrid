package com.forx.grid.config;

import com.forx.grid.ModInfo;
import net.minecraftforge.common.ForgeConfigSpec;

final class ClientConfig {
	final ForgeConfigSpec.ConfigValue<String> gridColor;

	ClientConfig(final ForgeConfigSpec.Builder builder) {
		builder.push("grid");
		gridColor = builder
				.comment("Define the grid's color")
				.translation(ModInfo.MODID_LOWER + ".config.grid.color")
				.define("color", "#FFFFFF");
		builder.pop();
	}

}
