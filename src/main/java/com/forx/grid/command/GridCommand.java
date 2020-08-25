package com.forx.grid.command;

import com.forx.grid.FGrid;
import com.forx.grid.ModInfo;
import com.forx.grid.config.ModConfig;
import com.forx.grid.handlers.FileHandler;
import com.forx.grid.helpers.ResourceHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.Vec3Argument;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class GridCommand {
    private static final ResourceLocation modIcons = new ResourceLocation(ModInfo.MODID_LOWER, "textures/icons.png");

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("fgrid")
            .requires(source -> source.hasPermissionLevel(1))
            .then(Commands.argument("firstBlockPos", Vec3Argument.vec3())
                .then(Commands.argument("lastBlockPos", Vec3Argument.vec3())
                    .executes(context -> createGrid(context.getSource(),
                        BlockPosArgument.getBlockPos(context, "firstBlockPos"),
                        BlockPosArgument.getBlockPos(context, "lastBlockPos")))
                )
            )
        );
    }

    private static int createGrid(CommandSource source, BlockPos firstBlockPos, BlockPos lastBlockPos) {
        World world = source.getWorld();
        String path = FileHandler.getLastFolderPath();

        createGridJson(path, world, firstBlockPos, lastBlockPos);
        createGridImages(path, world, firstBlockPos, lastBlockPos);

        Minecraft.getInstance().ingameGUI.setOverlayMessage(new TranslationTextComponent("fgrid.command.success"), false);

        return 1;
    }

    private static void createGridJson(String path, World world, BlockPos firstBlockPos, BlockPos lastBlockPos) {
        int minX = Math.min(firstBlockPos.getX(), lastBlockPos.getX());
        int minZ = Math.min(firstBlockPos.getZ(), lastBlockPos.getZ());
        int minY = Math.min(firstBlockPos.getY(), lastBlockPos.getY());
        int maxX = Math.max(firstBlockPos.getX(), lastBlockPos.getX());
        int maxZ = Math.max(firstBlockPos.getZ(), lastBlockPos.getZ());
        int maxY = Math.max(firstBlockPos.getY(), lastBlockPos.getY());

        JsonObject grid = new JsonObject();

        grid.addProperty("minX", minX);
        grid.addProperty("minZ", minZ);
        grid.addProperty("minY", minY);
        grid.addProperty("maxX", maxX);
        grid.addProperty("maxZ", maxZ);
        grid.addProperty("maxY", maxY);

        JsonArray layer = new JsonArray();

        for (int y = minY; y <= maxY; y++) {
            JsonArray xLayer = new JsonArray();

            for (int x = minX; x <= maxX; x++) {
                JsonArray zLayer = new JsonArray();

                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);

                    if (world.isBlockPresent(blockPos)) {
                        BlockState blockState = world.getBlockState(blockPos);
                        zLayer.add(blockState.getBlock().getRegistryName().getPath());
                    }
                }

                xLayer.add(zLayer);
            }

            layer.add(xLayer);
        }

        grid.add("layers", layer);

        FileHandler.makeFile(path + "fgrid", grid);
    }

    private static void createGridImages(String path, World world, BlockPos firstBlockPos, BlockPos lastBlockPos) {
        int minX = Math.min(firstBlockPos.getX(), lastBlockPos.getX()) - 1;
        int minZ = Math.min(firstBlockPos.getZ(), lastBlockPos.getZ());
        int minY = Math.min(firstBlockPos.getY(), lastBlockPos.getY());
        int maxX = Math.max(firstBlockPos.getX(), lastBlockPos.getX()) - 1;
        int maxZ = Math.max(firstBlockPos.getZ(), lastBlockPos.getZ());
        int maxY = Math.max(firstBlockPos.getY(), lastBlockPos.getY());

        int imageWidth = 2 + (maxX - minX) + (1 + maxX - minX) * 16;
        int imageHeight = 2 + (maxZ - minZ) + (1 + maxZ - minZ) * 16;
        int i = 0;

        for (int y = minY; y <= maxY; y++) {
            BufferedImage bufferedImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics2D = bufferedImage.createGraphics();
            graphics2D.setColor(Color.decode(ModConfig.gridColor));

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockPos blockPos = new BlockPos(x, y, z);

                    if (world.isBlockPresent(blockPos)) {
                        BlockState blockState = world.getBlockState(blockPos);
                        TextureAtlasSprite textureAtlasSprite = Minecraft.getInstance().getBlockRendererDispatcher().getBlockModelShapes().getTexture(blockState);
                        ResourceLocation resourcelocation = ResourceHelper.getSpritePath(textureAtlasSprite.getName());

                        if (!resourcelocation.toString().contains("missingno")) {
                            try (IResource iresource = Minecraft.getInstance().getResourceManager().getResource(resourcelocation)) {
                                NativeImage nativeimage = NativeImage.read(iresource.getInputStream());
                                ByteArrayInputStream bis = new ByteArrayInputStream(nativeimage.getBytes());
                                BufferedImage bImage2 = ImageIO.read(bis);
                                graphics2D.drawImage(bImage2, 1 + (maxX - x) + (maxX - x) * 16, 1 + (maxZ - z) + (maxZ - z) * 16, null);
                            } catch (RuntimeException runtimeexception) {
                                FGrid.LOGGER.error("Unable to parse metadata from {}", resourcelocation, runtimeexception);
                            } catch (IOException ioexception) {
                                FGrid.LOGGER.error("Using missing texture, unable to load {}", resourcelocation, ioexception);
                            }
                        }

                        int x1 = (maxX - x) + (maxX - x) * 16;
                        int y1 = (maxZ - z) + (maxZ - z) * 16;
                        int x2 = 17 + (maxX - x) + (maxX - x) * 16;
                        int y2 = 17 + (maxZ - z) + (maxZ - z) * 16;

                        graphics2D.drawLine(x1, y1, x2, y1);
                        graphics2D.drawLine(x1, y1, x1, y2);
                        graphics2D.drawLine(x2, y1, x2, y2);
                        graphics2D.drawLine(x1, y2, x2, y2);
                    }
                }
            }

            FileHandler.makeFile(path + "layer_" + (i < 10 ? "00" : (i < 100 ? "0" : "")) + i, bufferedImage);

            i++;
        }
    }
}
