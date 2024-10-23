package com.ishikyoo.leavesly;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.FoliageColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class LeaveslyColorProvider {
    public enum TintMode {
        Foliage,
        Grass
    }

    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    static final IntProperty SNOW_LAYER = SnowLayerLogic.SNOW_LAYER;
    static final HashMap<Block, Integer> blockStaticColorsHashMap = new HashMap<>();
    static final HashMap<Block, Double> blockColorBrightnesssHashMap = new HashMap<>();

    public static void register(String mod, String id, int brightness) {
        Block block = LeaveslyBlockRegistry.getBlock(mod, id);
        if (block != null) {
            blockStaticColorsHashMap.put(block, brightness);
            ColorProviderRegistry.BLOCK.register(LeaveslyColorProvider::getBlockSnowLayeredStaticColor, block);
            LOGGER.info("Registered color (Mod: {}, Id: {}, Tint: {}).", mod, id, "static");
        }
    }

    public static void register(String mod, String id, TintMode tint, double brightness) {
        Block block = LeaveslyBlockRegistry.getBlock(mod, id);
        if (block != null) {
            switch (tint) {
                case Foliage:
                    blockColorBrightnesssHashMap.put(block, brightness);
                    ColorProviderRegistry.BLOCK.register(LeaveslyColorProvider::getBlockSnowLayeredFoliageColor, block);
                    LOGGER.info("Registered color (Mod: {}, Id: {}, Tint: {}).", mod, id, "foliage");
                    break;
                case Grass:
                    blockColorBrightnesssHashMap.put(block, brightness);
                    ColorProviderRegistry.BLOCK.register(LeaveslyColorProvider::getBlockSnowLayeredGrassColor, block);
                    LOGGER.info("Registered color (Mod: {}, Id: {}, Tint: {}).", mod, id, "grass");
                    break;
            }
        }
    }

    public static void initialize() {
        register("minecraft", "birch_leaves", getBrightenedColor(FoliageColors.getBirchColor(), 0.72549019607));
        register("minecraft", "mangrove_leaves", getBrightenedColor(FoliageColors.getMangroveColor(), 0.70980392156));
        register("minecraft", "spruce_leaves", getBrightenedColor(FoliageColors.getSpruceColor(), 0.60392156862));
        register("minecraft", "azalea_leaves", getBrightenedColor(0xC4FF4F, 0.5725490196));
        register("minecraft", "cherry_leaves", getBrightenedColor(0xDEFF4C, 0.6));
        register("minecraft", "flowering_azalea_leaves", getBrightenedColor(0xC4FF4F, 0.5725490196));
        register("minecraft", "dark_oak_leaves", TintMode.Foliage, 0.72549019607);
        register("minecraft", "jungle_leaves", TintMode.Foliage, 0.85490196078);
        register("minecraft", "oak_leaves", TintMode.Foliage, 0.73725490196);
        register("minecraft", "vine", TintMode.Foliage, 0.66666666666);
        register("minecraft", "short_grass", TintMode.Grass, 0.72156862745);
        register("minecraft", "tall_grass", TintMode.Grass, 0.67450980392);
        register("minecraft", "fern", TintMode.Grass, 0.64705882352);
        register("minecraft", "large_fern", TintMode.Grass, 0.67450980392);
    }

    static int getBlockSnowLayeredStaticColor(BlockState state, BlockRenderView world, BlockPos position, int index) {
        Block block = state.getBlock();
        int color = blockStaticColorsHashMap.get(block);
        int snowLayer = state.get(SNOW_LAYER);
        double leavesSnowLayerMask = (double) snowLayer / SNOW_LAYER.getValues().size();
        return getSnowLayeredColor(color, leavesSnowLayerMask);
    }

    static int getBlockSnowLayeredFoliageColor(BlockState state, BlockRenderView world, BlockPos position, int index) {
        Block block = state.getBlock();
        int color = getBrightenedColor(BiomeColors.getFoliageColor(world, position), blockColorBrightnesssHashMap.get(block));
        int snowLayer = state.get(SNOW_LAYER);
        double leavesSnowLayerMask = (double) snowLayer / SNOW_LAYER.getValues().size();
        return getSnowLayeredColor(color, leavesSnowLayerMask);
    }

    static int getBlockSnowLayeredGrassColor(BlockState state, BlockRenderView world, BlockPos position, int index) {
        Block block = state.getBlock();
        int color = getBrightenedColor(BiomeColors.getGrassColor(world, position), blockColorBrightnesssHashMap.get(block));
        int snowLayer = state.get(SNOW_LAYER);
        double leavesSnowLayerMask = (double) snowLayer / SNOW_LAYER.getValues().size();
        return getSnowLayeredColor(color, leavesSnowLayerMask);
    }

    static int getBrightenedColor(int color, double brightness) {
        int r = (int) Math.round((color >> 16 & 0xff) * brightness);
        int g = (int) Math.round((color >> 8 & 0xff) * brightness);
        int b = (int) Math.round((color & 0xff) * brightness);
        return r << 16 | g << 8 | b;
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
}
