package com.ishikyoo.leavesly.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.ishikyoo.leavesly.support.Version;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.FoliageColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class LeaveslySettings {
    private static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    private static final Path FILE_PATH = Path.of(String.valueOf(FabricLoader.getInstance().getConfigDir()), "leavesly.json");

    private static final int VERSION = 1;

    private static Gson gson;

    private static LeaveslySettingsData defaultSettings;
    private static LeaveslySettingsData settings;

    public static Gson getGson() {
        return gson;
    }

    public static LeaveslySettingsData getSettings() {
        return settings;
    }

    public static void initialize() {
        if (settings == null) {
            setDefault();
            serialize();
        }
    }

    public static void preInitialize() {
        initializeGson();
        initializeDefaultSettings();
        if (doesFileExists()) {
            settings = getConfigFromFile();
        }
    }

    private static void initializeDefaultSettings() {
        defaultSettings = LeaveslySettingsData.of(VERSION,
                SnowLayerData.of(true, 0.5, 0, 1),
                new HashMap<>());
        registerBlockSettings();
    }

    public static void setDefault() {
        settings = LeaveslySettingsData.of(defaultSettings);
    }

    private static LeaveslySettingsData getConfigFromFile() {
        try {
            BufferedReader reader = new BufferedReader(Files.newBufferedReader(LeaveslySettings.FILE_PATH, StandardCharsets.UTF_8));
            JsonReader jsonReader = new JsonReader(reader);
            return getGson().fromJson(jsonReader, LeaveslySettingsData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void initializeGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Tint.class, new Tint.Serializer());
        builder.registerTypeAdapter(SnowLayerData.class, new SnowLayerData.Serializer());
        builder.registerTypeAdapter(BlockData.class, new BlockData.Serializer());
        builder.registerTypeAdapter(LeaveslySettingsData.class, new LeaveslySettingsData.Serializer());
        gson = builder.create();
    }

    private static void registerBlockSettings() {
        Version mineVer = Version.of("minecraft");

        register("minecraft:birch_leaves", BlockData.of(
                Tint.of(FoliageColors.getBirchColor() & 0x00FFFFFF, 0.72549019607),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:mangrove_leaves", BlockData.of(
                Tint.of(FoliageColors.getMangroveColor() & 0x00FFFFFF, 0.70980392156),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:spruce_leaves", BlockData.of(
                Tint.of(FoliageColors.getSpruceColor() & 0x00FFFFFF, 0.60392156862),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:azalea_leaves", BlockData.of(
                Tint.of(0xC4FF4F, 0.5725490196),
                SnowLayerData.of(true, 1, 0,1)
        ));
        if (mineVer.newerThan(Version.of(1, 19, 4))) {
            register("minecraft:cherry_leaves", BlockData.of(
                    Tint.of(0xDEFF4C, 0.6),
                    SnowLayerData.of(true, 1, 0, 1)
            ));
        }
        register("minecraft:flowering_azalea_leaves", BlockData.of(
                Tint.of(0xC4FF4F, 0.5725490196),
                SnowLayerData.of(true, 1, 0,1)
        ));
        if (mineVer.newerThan(Version.of(1, 21, 1))) {
            register("minecraft:pale_oak_leaves", BlockData.of(
                    Tint.of(0xA0A69C),
                    SnowLayerData.of(true, 1, 0, 1)
            ));
        }
        register("minecraft:dark_oak_leaves", BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.72549019607),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:jungle_leaves", BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.85490196078),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:oak_leaves", BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.73725490196),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:acacia_leaves", BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.70980392156),
                SnowLayerData.of(true, 1, 0,1)
        ));

        register("minecraft:vine", BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.66666666666),
                SnowLayerData.of(true, 1, 0,1)
        ));

        register("minecraft:short_grass", BlockData.of(
                Tint.of(ColorType.GRASS, 0.72156862745),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:tall_grass", BlockData.of(
                Tint.of(ColorType.GRASS, 0.67450980392),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:fern", BlockData.of(
                Tint.of(ColorType.GRASS, 0.64705882352),
                SnowLayerData.of(true, 1, 0,1)
        ));
        register("minecraft:large_fern", BlockData.of(
                Tint.of(ColorType.GRASS, 0.67450980392),
                SnowLayerData.of(true, 1, 0,1)
        ));
    }

    public static void serialize() {
        try {
            BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(FILE_PATH, StandardCharsets.UTF_8));
            writer.write(getGson().toJson(settings));
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean doesFileExists() {
        return Files.isRegularFile(FILE_PATH);
    }

    public static void register(String id, BlockData blockData) {
        BlockData block = defaultSettings.getBlock(id);
        if (block == null) {
            defaultSettings.putBlock(id, blockData);
        } else {
            LOGGER.warn("Trying to register an already registered block settings (Id: {}).", id);
        }
    }
}
