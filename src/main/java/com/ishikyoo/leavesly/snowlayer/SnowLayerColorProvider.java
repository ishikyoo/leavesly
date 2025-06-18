package com.ishikyoo.leavesly.snowlayer;

import com.ishikyoo.iyoo.state.BitsmartStateViewer;
import com.ishikyoo.iyoo.state.property.SubProperty;
import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.iyoo.state.property.BitsmartProperty;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.leavesly.settings.*;
import com.ishikyoo.iyoo.util.MathUtils;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SnowLayerColorProvider {
    private static final Logger LOG = Leavesly.LOGGER;

    private final ArrayList<Identifier> registeredBlockIds = new ArrayList<>();
    private final HashMap<Identifier, Tint> modPatchBlockTintHashMap = new HashMap<>();

    public SnowLayerColorProvider() {

    }

    public void initialize() {
        registerBlockAndItemsColors();
        applyModPatch();
    }

    public void register(Identifier id) {
        if (Blocks.isSupportedBlockId(id)) {
            BlockData blockData = Leavesly.getSettings().getCurrent().getBlock(id);
            Tint tint = blockData.getTint();
            if (!registeredBlockIds.contains(id)) {
                if (shouldLog()) {
                    LOG.info("Registering block and item colors (Id: {}, Tint: {}).", id, tint.getColorType().toString().toLowerCase());
                }
                Block block = Blocks.getBlock(id);
                ColorProviderRegistry.BLOCK.register(this::getColorProviderBlockColor, block);
                //ColorProviderRegistry.ITEM.register(this::getColorProviderItemColor, block);
                registeredBlockIds.add(id);
            } else {
                LOG.error("Trying to register a already registered block and item colors (Id: {}, Tint: {}).", id, tint.getColorType().toString().toLowerCase());
            }
        } else {
            LOG.error("Trying to register a block and item colors for a unregistered block (Id: {}).", id);
        }
    }

    private void registerBlockAndItemsColors() {
        HashMap<Identifier, BlockData> blocks = Leavesly.getSettings().getCurrent().getBlocks();
        for(Map.Entry<Identifier, BlockData> entry : blocks.entrySet()) {
            Identifier id = entry.getKey();
            register(id);
        }
    }

    private void applyModPatch() {
        if (shouldLog()) {
            LOG.info("Registering mod patches...");
        }
        //Clutter
        String clutterId = "clutter";
        registerModBlockPatch(Identifier.of(clutterId, "redwood_leaves"), Tint.FOLIAGE);
        registerModBlockPatch(Identifier.of(clutterId, "giant_fern"), Tint.GRASS);
    }

    private void registerModBlockPatch(Identifier blockId, Tint tint) {
        LeaveslySettingsData settings = Leavesly.getSettings().getCurrent();
        if (!settings.containsBlock(blockId) && Blocks.isRegisteredBlockId(blockId)) {
            if (shouldLog()) {
                LOG.info("Registering block mod patch (Id: {}, Tint: {}).", blockId, tint.getColorType().toString().toLowerCase());
            }
            modPatchBlockTintHashMap.put(blockId, tint);
        }
    }

    private int getColorProviderBlockColor(BlockState state, BlockRenderView world, BlockPos position, int index) {
        LeaveslySettingsData settings = Leavesly.getSettings().getCurrent();
        BlockData blockData = settings.getBlock(state.getBlock());
        Block block = state.getBlock();

        if (blockData == null)
            return isModPatchBlock(block) ? getModPatchBlockColor(block, world, position) : Tint.NEON_PINK.getColorValue();

        int blockColor = !isModPatchBlock(block) ?
                getBlockColor(world, position, blockData) :
                getModPatchBlockColor(block, world, position);

        if (!blockData.getSnowLayer().isEnabled())
            return blockColor;

        BitsmartProperty property = BitsmartRegistry.get(SnowLayerBlock.PROPERTY_ID);
        BitsmartStateViewer viewer = BitsmartStateViewer.of(state, property);
        SubProperty<Integer> coverageProperty = property.getSubProperty(SnowLayerBlock.PROPERTY_COVERAGE_ID, Integer.class);
        int coverage = viewer.get(coverageProperty);
        double snowLayerMask = getSnowLayerMask(coverage, settings, blockData);
        return getSnowLayeredColor(blockColor, snowLayerMask);
    }

    private static double getSnowLayerMask(int coverage, LeaveslySettingsData settings, BlockData blockData) {
        double snowLayerN = MathUtils.normalize(coverage, 0, SnowLayerBlock.PROPERTY_COVERAGE_MAX);
        double coverageMax = settings.getSnowLayer().getMaxCoverage() * blockData.getSnowLayer().getMaxCoverage();
        double coverageMin = settings.getSnowLayer().getMinCoverage() + (blockData.getSnowLayer().getMinCoverage() * (coverageMax - blockData.getSnowLayer().getMinCoverage()));
        return coverageMin + ((coverageMax - coverageMin) * snowLayerN);
    }

    private int getColorProviderItemColor(ItemStack stack, int index) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (Blocks.isSupportedBlock(block)) {
            return getItemColor(block);
        } else {
            if (isModPatchBlock(block))
                return getModPatchItemColor(block);
            return Tint.NEON_PINK.getColorValue();
        }
    }

    private int getBlockColor(BlockRenderView world, BlockPos position, BlockData blockData) {
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

    private int getItemColor(Block block) {
        BlockData blockData = Leavesly.getSettings().getCurrent().getBlock(Blocks.getBlockId(block));
        if (blockData == null)
            return Tint.NEON_PINK.getColorValue();
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

    private  int getModPatchBlockColor(Block block, BlockRenderView world, BlockPos position) {
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

    private  int getModPatchItemColor(Block block) {
        Tint tint = getModPatchBlockTint(block);
        return switch (tint.getColorType()) {
            case STATIC -> tint.getColorValue();
            case FOLIAGE -> Tint.DEFAULT_FOLIAGE.getColorValue();
            case GRASS -> Tint.DEFAULT_GRASS.getColorValue();
        };
    }

    private Tint getModPatchBlockTint(Block block) {
        return modPatchBlockTintHashMap.get(Blocks.getBlockId(block));
    }

    private  boolean isModPatchBlock(Block block) {
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

    private static boolean shouldLog() {
        return Leavesly.getSettings().getCurrent().shouldLog();
    }

    private static boolean isDebug() {
        return Leavesly.isDebug();
    }
}