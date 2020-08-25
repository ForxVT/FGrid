package com.forx.grid.helpers;

import net.minecraft.util.ResourceLocation;

public class ResourceHelper {
    public static ResourceLocation getSpritePath(ResourceLocation location) {
        return new ResourceLocation(location.getNamespace(), String.format("textures/%s%s", location.getPath(), ".png"));
    }
}
