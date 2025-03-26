package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.leavesly.settings.*;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LeaveslyColorProvider {
    private static final Logger LOG = Leavesly.LOGGER;

    private static final IntProperty SNOW_LAYER = SnowLayerLogic.SNOW_LAYER;
    private static final ArrayList<Identifier> registeredBlockIds = new ArrayList<>();

    private static final HashMap<Identifier, Tint> modPatchBlockTintHashMap = new HashMap<>();

    public static void initialize() {
        registerBlockAndItemsColors();
        applyModPatch();
    }

    public static void register(Identifier id) {
        if (Blocks.isSupportedBlockId(id)) {
            BlockData blockData = LeaveslySettings.getSettings().getBlock(id);
            Tint tint = blockData.getTint();
            if (!registeredBlockIds.contains(id)) {
                Block block = Blocks.getBlock(id);
                ColorProviderRegistry.BLOCK.register(LeaveslyColorProvider::getColorProviderBlockColor, block);
                ColorProviderRegistry.ITEM.register(LeaveslyColorProvider::getColorProviderItemColor, block);
                LOG.info("Registered block and item colors (Id: {}, Tint: {}).", id, tint.getColorType().toString().toLowerCase());
                registeredBlockIds.add(id);
            } else {
                LOG.error("Trying to register a already registered block and item colors (Id: {}, Tint: {}).", id, tint.getColorType().toString().toLowerCase());
            }
        } else {
            LOG.error("Trying to register a block and item colors for a unregistered block (Id: {}).", id);
        }
    }

    private static void registerBlockAndItemsColors() {
        HashMap<Identifier, BlockData> blocks = LeaveslySettings.getSettings().getBlocks();
        for(Map.Entry<Identifier, BlockData> entry : blocks.entrySet()) {
            Identifier id = entry.getKey();
            register(id);
        }
    }

    private static void applyModPatch() {
        LOG.info("Registering mod patches...");
        //Clutter
        String clutterId = "clutter";
        registerModBlockPatch(Identifier.of(clutterId, "redwood_leaves"), Tint.FOLIAGE);
        registerModBlockPatch(Identifier.of(clutterId, "giant_fern"), Tint.GRASS);
    }

    private static void registerModBlockPatch(Identifier blockId, Tint tint) {
        LeaveslySettingsData settings = LeaveslySettings.getSettings();
        if (!settings.isRegisteredBlockId(blockId) && Blocks.isRegisteredBlockId(blockId)) {
            modPatchBlockTintHashMap.put(blockId, tint);
            LOG.info("Registered block and item colors mod patch (Id: {}, Tint: {}).", blockId, tint.getColorType().toString().toLowerCase());
        }
    }

    private static int getColorProviderBlockColor(BlockState state, BlockRenderView world, BlockPos position, int index) {
        Block block = state.getBlock();
        if (Blocks.isSupportedBlock(block)) {
            int snowLayer = state.get(SNOW_LAYER);
            int blockColor = getBlockColor(state, world, position);
            LeaveslySettingsData settings = LeaveslySettings.getSettings();
            BlockData blockData = settings.getBlock(state.getBlock());
            ClientWorld clientWorld = MinecraftClient.getInstance().world;
            if (settings.getSnowLayer().isEnabled() && blockData.getSnowLayer().isEnabled() &&
                    clientWorld.getBiome(position).value().getPrecipitation(position) == Biome.Precipitation.SNOW) {
                double snowLayerN = (double) snowLayer / SNOW_LAYER.getValues().size();
                double coverageMax = settings.getSnowLayer().getMaxCoverage() * blockData.getSnowLayer().getMaxCoverage();
                double coverageMin = settings.getSnowLayer().getMinCoverage() + (blockData.getSnowLayer().getMinCoverage() * (coverageMax - blockData.getSnowLayer().getMinCoverage()));
                double snowLayerMask = coverageMin + ((coverageMax - coverageMin) * snowLayerN);
                return getSnowLayeredColor(blockColor, snowLayerMask);
            }
            return blockColor;
        } else {
            if (isModPatchBlock(block))
                return getModPatchBlockColor(block, world, position);
            return Tint.NEON_PINK.getColorValue();
        }
    }

    private static int getColorProviderItemColor(ItemStack stack, int index) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (Blocks.isSupportedBlock(block)) {
            return getItemColor(block);
        } else {
            if (isModPatchBlock(block))
                return getModPatchItemColor(block);
            return Tint.NEON_PINK.getColorValue();
        }
    }

    private static int getBlockColor(BlockState state, BlockRenderView world, BlockPos position) {
        Block block = state.getBlock();
        BlockData blockData = LeaveslySettings.getSettings().getBlock(Blocks.getBlockId(block));
        Tint tint = blockData.getTint();
        switch (tint.getColorType()) {
            case STATIC:
                return tint.getColorValue();
            case FOLIAGE:
                switch (tint.getColorBlend()) {
                    case MULTIPLY:
                        return getMultiplyColor(BiomeColors.getFoliageColor(world, position), tint.getColorValue());
                    case SCREEN:
                        return getScreenColor(BiomeColors.getFoliageColor(world, position), tint.getColorValue());
                }
            case GRASS:
                switch (tint.getColorBlend()) {
                    case MULTIPLY:
                        return getMultiplyColor(BiomeColors.getGrassColor(world, position), tint.getColorValue());
                    case SCREEN:
                        return getScreenColor(BiomeColors.getGrassColor(world, position), tint.getColorValue());
                }
            default:
                return Tint.NEON_PINK.getColorValue();
        }
    }

    private static int getItemColor(Block block) {
        BlockData blockData = LeaveslySettings.getSettings().getBlock(Blocks.getBlockId(block));
        Tint tint = blockData.getTint();
        switch (tint.getColorType()) {
            case STATIC:
                return tint.getColorValue();
            case FOLIAGE:
                switch (tint.getColorBlend()) {
                    case MULTIPLY:
                        return getMultiplyColor(Tint.DEFAULT_FOLIAGE.getColorValue(), tint.getColorValue());
                    case SCREEN:
                        return getScreenColor(Tint.DEFAULT_FOLIAGE.getColorValue(), tint.getColorValue());
                }
            case GRASS:
                switch (tint.getColorBlend()) {
                    case MULTIPLY:
                        return getMultiplyColor(Tint.DEFAULT_GRASS.getColorValue(), tint.getColorValue());
                    case SCREEN:
                        return getScreenColor(Tint.DEFAULT_GRASS.getColorValue(), tint.getColorValue());
                }
            default:
                return Tint.NEON_PINK.getColorValue();
        }
    }

    private static int getModPatchBlockColor(Block block, BlockRenderView world, BlockPos position) {
        Tint tint = getModPatchBlockTint(block);
        switch (tint.getColorType()) {
            case STATIC:
                return tint.getColorValue();
            case FOLIAGE:
                switch (tint.getColorBlend()) {
                    case MULTIPLY:
                        return getMultiplyColor(BiomeColors.getFoliageColor(world, position), tint.getColorValue());
                    case SCREEN:
                        return getScreenColor(BiomeColors.getFoliageColor(world, position), tint.getColorValue());
                }
            case GRASS:
                switch (tint.getColorBlend()) {
                    case MULTIPLY:
                        return getMultiplyColor(BiomeColors.getGrassColor(world, position), tint.getColorValue());
                    case SCREEN:
                        return getScreenColor(BiomeColors.getGrassColor(world, position), tint.getColorValue());
                }
            default:
                return Tint.NEON_PINK.getColorValue();
        }
    }

    private static int getModPatchItemColor(Block block) {
        Tint tint = getModPatchBlockTint(block);
        return switch (tint.getColorType()) {
            case STATIC -> tint.getColorValue();
            case FOLIAGE -> Tint.DEFAULT_FOLIAGE.getColorValue();
            case GRASS -> Tint.DEFAULT_GRASS.getColorValue();
        };
    }

    private static Tint getModPatchBlockTint(Block block) {
        return modPatchBlockTintHashMap.get(Blocks.getBlockId(block));
    }

    private static boolean isModPatchBlock(Block block) {
        return modPatchBlockTintHashMap.containsKey(Blocks.getBlockId(block));
    }

    private static int getSnowLayeredColor(int color, double mask) {
        int r = (color >> 16 & 0xff);
        int g = (color >> 8 & 0xff);
        int b = (color & 0xff);
        r += (int) ((0xff - r) * mask);
        g += (int) ((0xff - g) * mask);
        b += (int) ((0xff - b) * mask);
        return r << 16 | g << 8 | b;
    }

    private static int getMultiplyColor(int color, int multiplier) {
        int r = (color >> 16 & 0xff) * (multiplier >> 16 & 0xff) / 255;
        int g = (color >> 8 & 0xff) * (multiplier >> 8 & 0xff) / 255;
        int b = (color & 0xff) * (multiplier & 0xff) / 255;
        return r << 16 | g << 8 | b;
    }

    private static int getScreenColor(int color, int multiplier) {
        int r = 1 - (1 - (color >> 16 & 0xff)) * (1 - (multiplier >> 16 & 0xff)) / 255;
        int g = 1 - (1 - (color >> 8 & 0xff)) * (1 - (multiplier >> 8 & 0xff)) / 255;
        int b = 1 - (1 - (color & 0xff)) * (1 - (multiplier & 0xff)) / 255;
        return r << 16 | g << 8 | b;
    }
}
