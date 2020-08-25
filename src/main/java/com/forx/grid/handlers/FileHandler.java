package com.forx.grid.handlers;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class FileHandler {
    private static final String saveFolder = "\\My Games\\Minecraft\\FGrid\\";
    private static final String documentsFolder = getMyDocumentsFolderPath();
    private static final String basePath = documentsFolder + saveFolder;

    public static void initFolder() {
        File file = new File(basePath);

        if (!file.exists()) {
            file.mkdirs();
        }
    }

    public static void makeFile(String filename, JsonObject content) {
        try {
            FileWriter file = new FileWriter(basePath + filename + ".json");
            file.write(content.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeFile(String filename, byte[] content) {
        try {
            FileUtils.writeByteArrayToFile(new File(basePath + filename + ".png"), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeFile(String filename, BufferedImage content) {
        try {
            ImageIO.write(content, "PNG", new File(basePath + filename + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getLastFolderPath() {
        int i = 0;

        while (true) {
            File file = new File(basePath + "grid_" + (i < 10 ? "00" : (i < 100 ? "0" : "")) + i + "\\");

            if (!file.exists()) {
                file.mkdirs();

                return "grid_" + (i < 10 ? "00" : (i < 100 ? "0" : "")) + i + "\\";
            }

            i++;
        }
    }

    private static String getMyDocumentsFolderPath() {
        try {
            Process process =  Runtime.getRuntime().exec(
                "reg query \"HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\Shell Folders\" /v personal");
            process.waitFor();

            InputStream in = process.getInputStream();
            byte[] bytes = new byte[in.available()];
            in.read(bytes);
            in.close();

            return new String(bytes).split("\\s\\s+")[4];

        } catch (IOException | InterruptedException ie) {
            ie.printStackTrace();
        }

        return "";
    }
}
