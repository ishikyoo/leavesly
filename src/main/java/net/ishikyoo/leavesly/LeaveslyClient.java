package net.ishikyoo.leavesly;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.FoliageColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveslyClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Leavesly.MOD_ID);

    private static LeaveslyClient instance;

    public static LeaveslyClient getInstance(){ return instance; }

    static final double SPRUCE_LEAVES_MASK = 0.60392156862;
    static final double BIRCH_LEAVES_MASK = 0.72549019607;
    static final double MANGROVE_LEAVES_MASK = 0.70980392156;
    static final double ACACIA_LEAVES_MASK = 0.70980392156;
    static final double DARK_OAK_LEAVES_MASK = 0.72549019607;
    static final double JUNGLE_LEAVES_MASK = 0.72549019607;
    static final double OAK_LEAVES_MASK = 0.73725490196;
    static final double AZALEA_LEAVES_MASK = 0.5725490196;
    static final double FLOWERING_AZALEA_LEAVES_MASK = 0.5725490196;
    static final double CHERRY_LEAVES_MASK = 0.6;
    static final double VINE_MASK = 0.66666666666;

    static final int SPRUCE_LEAVES_COLOR = FoliageColors.getSpruceColor();
    static final int BIRTH_LEAVES_COLOR = FoliageColors.getBirchColor();
    static final int MANGROVE_LEAVES_COLOR = FoliageColors.getMangroveColor();
    static final int AZALEA_LEAVES_COLOR = 0xC4FF4F;
    static final int FLOWERING_AZALEA_LEAVES_COLOR = 0xC4FF4F;
    static final int CHERRY_LEAVES_COLOR = 0xDEFF4C;

    private int getColorProviderValue(BlockState state, BlockRenderView world, BlockPos position, int index) {
        if (state != null && world != null && position != null) {
            Block block = state.getBlock();
            int color = getFoliageColor(block, world, position);
            int snow = state.get(Leavesly.SNOW);
            double leavesSnowMask = (double) snow / Leavesly.SNOW.getValues().size();
            return setFoliageWhiteMask(color, leavesSnowMask);
        }
        return 0;
    }

    private int getFoliageColor(Block block, BlockRenderView world, BlockPos position) {
        if (block == Blocks.SPRUCE_LEAVES) {
            return getMaskedColor(SPRUCE_LEAVES_COLOR, SPRUCE_LEAVES_MASK);
        } else if (block == Blocks.BIRCH_LEAVES) {
            return getMaskedColor(BIRTH_LEAVES_COLOR, BIRCH_LEAVES_MASK);
        } else if (block == Blocks.MANGROVE_LEAVES) {
            return getMaskedColor(MANGROVE_LEAVES_COLOR, MANGROVE_LEAVES_MASK);
        } else if (block == Blocks.AZALEA_LEAVES) {
            return getMaskedColor(AZALEA_LEAVES_COLOR, AZALEA_LEAVES_MASK);
        } else if (block == Blocks.FLOWERING_AZALEA_LEAVES) {
            return getMaskedColor(FLOWERING_AZALEA_LEAVES_COLOR, FLOWERING_AZALEA_LEAVES_MASK);
        } else if (block == Blocks.ACACIA_LEAVES) {
            return getMaskedColor(BiomeColors.getFoliageColor(world, position), ACACIA_LEAVES_MASK);
        } else if (block == Blocks.CHERRY_LEAVES) {
            return getMaskedColor(CHERRY_LEAVES_COLOR, CHERRY_LEAVES_MASK);
        } else if (block == Blocks.DARK_OAK_LEAVES) {
            return getMaskedColor(BiomeColors.getFoliageColor(world, position), DARK_OAK_LEAVES_MASK);
        } else if (block == Blocks.JUNGLE_LEAVES) {
            return getMaskedColor(BiomeColors.getFoliageColor(world, position), JUNGLE_LEAVES_MASK);
        } else if (block == Blocks.OAK_LEAVES) {
            return getMaskedColor(BiomeColors.getFoliageColor(world, position), OAK_LEAVES_MASK);
        } else if (block == Blocks.VINE) {
            return getMaskedColor(BiomeColors.getFoliageColor(world, position), VINE_MASK);
        }
        return 0;
    }

    private int getMaskedColor(int color, double mask) {
        int r = (int) Math.round((color >> 16 & 0xff) * mask);
        int g = (int) Math.round((color >> 8 & 0xff) * mask);
        int b = (int) Math.round((color & 0xff) * mask);
        return r << 16 | g << 8 | b;
    }

    private int setFoliageWhiteMask(int color, double mask) {
        int r = (color >> 16 & 0xff);
        int g = (color >> 8 & 0xff);
        int b = (color & 0xff);
        r += (int) Math.round((0xff - r) * mask);
        g += (int) Math.round((0xff - g) * mask);
        b += (int) Math.round((0xff - b) * mask);
        return r << 16 | g << 8 | b;
    }

    @Override
    public void onInitializeClient() {
        ColorProviderRegistry.BLOCK.register(this::getColorProviderValue,
                Blocks.SPRUCE_LEAVES,
                Blocks.BIRCH_LEAVES,
                Blocks.MANGROVE_LEAVES,
                Blocks.ACACIA_LEAVES,
                Blocks.DARK_OAK_LEAVES,
                Blocks.JUNGLE_LEAVES,
                Blocks.OAK_LEAVES,
                Blocks.AZALEA_LEAVES,
                Blocks.FLOWERING_AZALEA_LEAVES,
                Blocks.CHERRY_LEAVES,
                Blocks.VINE
        );
        instance = this;
    }

}

