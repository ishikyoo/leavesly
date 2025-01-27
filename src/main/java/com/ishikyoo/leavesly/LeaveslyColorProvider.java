package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.settings.*;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LeaveslyColorProvider {
    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    static final IntProperty SNOW_LAYER = SnowLayerLogic.SNOW_LAYER;
    static final ArrayList<Identifier> registeredBlockIds = new ArrayList<>();

    public static void register(Identifier id) {
        Block block = LeaveslyBlockRegistry.getBlock(id);
        if (block != null) {
            BlockData blockData = LeaveslySettings.getSettings().getBlock(id);
            Tint tint = blockData.getTint();
            if (!registeredBlockIds.contains(id)) {
                ColorProviderRegistry.BLOCK.register(LeaveslyColorProvider::getSnowLayeredBlockColor, block);
                LOGGER.info("Registered color (Mod: {}, Id: {}, Tint: {}).", id.getNamespace(), id.getPath(), tint.getColorType().toString().toLowerCase());
                registeredBlockIds.add(id);
            } else {
                LOGGER.error("Trying to register a already registered block color (Mod: {}, Id: {}, Tint: {}).", id.getNamespace(), id.getPath(), tint.getColorType().toString().toLowerCase());
            }
        } else {
            LOGGER.error("Trying to register a color for a unregistered block (Mod: {}, Id: {}).", id.getNamespace(), id.getPath());
        }
    }

    public static void initialize() {
        HashMap<Identifier, BlockData> blocks = LeaveslySettings.getSettings().getBlocks();
        for(Map.Entry<Identifier, BlockData> entry : blocks.entrySet()) {
            Identifier id = entry.getKey();
            register(id);
        }
    }

    static int getSnowLayeredBlockColor(BlockState state, BlockRenderView world, BlockPos position, int index) {
        int snowLayer = state.get(SNOW_LAYER);
        int blockColor = getBlockColor(state, world, position);
        LeaveslySettingsData settings = LeaveslySettings.getSettings();
        BlockData blockData = settings.getBlock(state.getBlock());
        ClientWorld clientWorld = MinecraftClient.getInstance().world;
        if (settings.getSnowLayer().isEnabled() && blockData.getSnowLayer().isEnabled() &&
                clientWorld.getBiome(position).value().getPrecipitation(position, clientWorld.getSeaLevel()) == Biome.Precipitation.SNOW) {
            double snowLayerN = (double) snowLayer / SNOW_LAYER.getValues().size();
            double coverageMax = settings.getSnowLayer().getMaxCoverage() * blockData.getSnowLayer().getMaxCoverage();
            double coverageMin = settings.getSnowLayer().getMinCoverage() + (blockData.getSnowLayer().getMinCoverage() * (coverageMax - blockData.getSnowLayer().getMinCoverage()));
            double snowLayerMask = coverageMin + ((coverageMax - coverageMin) * snowLayerN);
            return getSnowLayeredColor(blockColor, snowLayerMask);
        }
        return blockColor;
    }

    static int getBlockColor(BlockState state, BlockRenderView world, BlockPos position) {
        Block block = state.getBlock();
        BlockData blockData = LeaveslySettings.getSettings().getBlock(LeaveslyBlockRegistry.getBlock(block));
        Tint tint = blockData.getTint();
        switch (tint.getColorType()) {
            case ColorType.STATIC:
                return tint.getColorValue();
            case ColorType.FOLIAGE:
                switch (tint.getColorBlend()) {
                    case ColorBlend.MULTIPLY:
                        return getMultiplyColor(BiomeColors.getFoliageColor(world, position), tint.getColorValue());
                    case ColorBlend.SCREEN:
                        return getScreenColor(BiomeColors.getFoliageColor(world, position), tint.getColorValue());
                }
            case ColorType.GRASS:
                switch (tint.getColorBlend()) {
                    case ColorBlend.MULTIPLY:
                        return getMultiplyColor(BiomeColors.getGrassColor(world, position), tint.getColorValue());
                    case ColorBlend.SCREEN:
                        return getScreenColor(BiomeColors.getGrassColor(world, position), tint.getColorValue());
                }
            default:
                return 0;
        }
    }

    static int getSnowLayeredColor(int color, double mask) {
        int r = (color >> 16 & 0xff);
        int g = (color >> 8 & 0xff);
        int b = (color & 0xff);
        r += (int) Math.round((0xff - r) * mask);
        g += (int) Math.round((0xff - g) * mask);
        b += (int) Math.round((0xff - b) * mask);
        return r << 16 | g << 8 | b;
    }

    static int getMultiplyColor(int color, int multiplier) {
        int r = (color >> 16 & 0xff) * (multiplier >> 16 & 0xff) / 255;
        int g = (color >> 8 & 0xff) * (multiplier >> 8 & 0xff) / 255;
        int b = (color & 0xff) * (multiplier & 0xff) / 255;
        return r << 16 | g << 8 | b;
    }

    static int getScreenColor(int color, int multiplier) {
        int r = 1 - (1 - (color >> 16 & 0xff)) * (1 - (multiplier >> 16 & 0xff)) / 255;
        int g = 1 - (1 - (color >> 8 & 0xff)) * (1 - (multiplier >> 8 & 0xff)) / 255;
        int b = 1 - (1 - (color & 0xff)) * (1 - (multiplier & 0xff)) / 255;
        return r << 16 | g << 8 | b;
    }
}
