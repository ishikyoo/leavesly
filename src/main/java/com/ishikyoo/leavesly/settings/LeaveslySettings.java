package com.ishikyoo.leavesly.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.util.Version;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class LeaveslySettings {
    private LeaveslySettings() {

    }

    private static final Logger LOGGER = Leavesly.LOGGER;

    private Gson gson;

    private Path filePath;

    private LeaveslySettingsData defaultSettings;
    private LeaveslySettingsData settings;

    public Gson getGson() {
        return gson;
    }

    public LeaveslySettingsData getCurrent() {
        return settings;
    }
    public LeaveslySettingsData getDefault() {
        return defaultSettings;
    }

    public static LeaveslySettings of(String filename) {
        LeaveslySettings settings = new LeaveslySettings();
        settings.filePath = Path.of(String.valueOf(FabricLoader.getInstance().getConfigDir()), filename);
        return settings;
    }

    public void initialize() {
        LOGGER.info("Initializing settings...");
        initializeGson();
        initializeDefaultSettings();
        if (doesFileExists()) {
            settings = getConfigFromFile();
        } else {
            setDefault();
            serialize();
        }
    }

    public void register(Identifier id, BlockData blockData) {
        BlockData block = defaultSettings.getBlock(id);
        if (block == null) {
            defaultSettings.putBlock(id, blockData);
        } else {
            LOGGER.warn("Trying to register an already registered block settings (Id: {}).", id);
        }
    }

    public void setDefault() {
        settings = LeaveslySettingsData.of(defaultSettings);
        LOGGER.info("Current settings set to default.");
    }

    private void initializeDefaultSettings() {
        defaultSettings = LeaveslySettingsData.of(Version.mod(), false, true,
                SnowLayerData.of(true, 6000, -1, 0, 1),
                new HashMap<>());
        registerBlockSettings();
        LOGGER.info("Initialized default settings.");
    }

    private LeaveslySettingsData getConfigFromFile() {
        try {
            BufferedReader reader = new BufferedReader(Files.newBufferedReader(filePath, StandardCharsets.UTF_8));
            JsonReader jsonReader = new JsonReader(reader);
            LOGGER.info("Current settings loaded from {}.", filePath);
            return getGson().fromJson(jsonReader, LeaveslySettingsData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.registerTypeAdapter(Identifier.class, new IdentifierSerializer());
        builder.registerTypeAdapter(Tint.class, new Tint.Serializer());
        builder.registerTypeAdapter(SnowLayerData.class, new SnowLayerData.Serializer());
        builder.registerTypeAdapter(BlockSnowLayerData.class, new BlockSnowLayerData.Serializer());
        builder.registerTypeAdapter(BlockData.class, new BlockData.Serializer());
        builder.registerTypeAdapter(LeaveslySettingsData.class, new LeaveslySettingsData.Serializer());
        gson = builder.create();
    }

    private void registerBlockSettings() {
        register(Identifier.of("minecraft", "birch_leaves"), BlockData.of(
                Tint.of(-8345771 & 0x00FFFFFF, 0.72549019607),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "mangrove_leaves"), BlockData.of(
                Tint.of(-7158200 & 0x00FFFFFF, 0.70980392156),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "spruce_leaves"), BlockData.of(
                Tint.of(-10380959 & 0x00FFFFFF, 0.60392156862),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "azalea_leaves"), BlockData.of(
                Tint.of(0xC4FF4F, 0.5725490196),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        if (Version.game().newerEqualThan(Version.CHERRY_LEAVES_BLOCK)) {
            register(Identifier.of("minecraft", "cherry_leaves"), BlockData.of(
                    Tint.of(0xDEFF4C, 0.6),
                    BlockSnowLayerData.of(true, 0, 1)
            ));
        }
        register(Identifier.of("minecraft", "flowering_azalea_leaves"), BlockData.of(
                Tint.of(0xC4FF4F, 0.5725490196),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        if (Version.game().newerEqualThan(Version.PALE_OAK_LEAVES_BLOCK)) {
            register(Identifier.of("minecraft", "pale_oak_leaves"), BlockData.of(
                    Tint.of(0xA0A69C),
                    BlockSnowLayerData.of(true, 0, 1)
            ));
        }
        register(Identifier.of("minecraft", "dark_oak_leaves"), BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.72549019607),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "jungle_leaves"), BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.85490196078),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "oak_leaves"), BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.73725490196),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "acacia_leaves"), BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.70980392156),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "vine"), BlockData.of(
                Tint.of(ColorType.FOLIAGE, 0.66666666666),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "short_grass"), BlockData.of(
                Tint.of(ColorType.GRASS, 0.72156862745),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "tall_grass"), BlockData.of(
                Tint.of(ColorType.GRASS, 0.67450980392),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "fern"), BlockData.of(
                Tint.of(ColorType.GRASS, 0.64705882352),
                BlockSnowLayerData.of(true, 0, 1)
        ));
        register(Identifier.of("minecraft", "large_fern"), BlockData.of(
                Tint.of(ColorType.GRASS, 0.67450980392),
                BlockSnowLayerData.of(true, 0, 1)
        ));
    }

    public void serialize() {
        try {
            BufferedWriter writer = new BufferedWriter(Files.newBufferedWriter(filePath, StandardCharsets.UTF_8));
            writer.write(getGson().toJson(settings));
            writer.flush();
            LOGGER.info("Current settings saved to {}.", filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean doesFileExists() {
        return Files.isRegularFile(filePath);
    }
}