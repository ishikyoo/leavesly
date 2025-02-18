package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.leavesly.settings.SnowLayerData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ishikyoo.leavesly.settings.LeaveslySettings;

public class SnowLayerLogic {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    protected static IntProperty SNOW_LAYER = IntProperty.of("snow_layer", 0, 63);
    protected static int SNOW_LAYER_MAX_VALUE = SNOW_LAYER.getValues().size() - 1;
    protected static int SKYLIGHT_MAX_VALUE = 15;
    protected static int SKYLIGHT_CUTOFF_VALUE = 9;
    protected static double NEIGHBOURS_INFLUENCE = 0.875;
    protected static double MAIN_INFLUENCE = Math.abs(NEIGHBOURS_INFLUENCE - 1);
    protected static int NEIGHBOURS_INFLUENCE_RANGE = 7;

    public static boolean hasRandomTick(BlockState state) {
        return isSnowLayerBlock(state.getBlock());
    }

    public static void randomDoubleBlockTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (LeaveslySettings.getSettings().getSnowLayer().isEnabled() && LeaveslySettings.getSettings().getBlock(state.getBlock()).getSnowLayer().isEnabled()) {

            if (world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.SNOW) {
                int skyLight = world.getLightLevel(LightType.SKY, pos);

                if (skyLight > SKYLIGHT_CUTOFF_VALUE) {
                    int blockSnow = state.get(SNOW_LAYER);
                    double skyLightN = (double) (skyLight - SKYLIGHT_CUTOFF_VALUE) / (SKYLIGHT_MAX_VALUE - SKYLIGHT_CUTOFF_VALUE);
                    double neighboursInfluenceN = getDoubleBlockNeighboursInfluence(world, state, pos, NEIGHBOURS_INFLUENCE_RANGE);
                    double maxSnowPerSkyLight = skyLightN * SNOW_LAYER_MAX_VALUE;
                    double mainInfluenceN = skyLightN * MAIN_INFLUENCE;
                    double valueToChange = getBlockValueToChange(state);
                    double mainInfluenceValue = mainInfluenceN * valueToChange;

                    if (world.isRaining()) {
                        double neighboursInfluenceAddN = (neighboursInfluenceN * skyLightN) * NEIGHBOURS_INFLUENCE;
                        double neighboursInfluenceValueAdd = neighboursInfluenceAddN * valueToChange;

                        int valueToAdd = (int) Math.floor((mainInfluenceValue + neighboursInfluenceValueAdd) / 2);
                        if (valueToAdd == 0) valueToAdd = 1;

                        if (blockSnow < maxSnowPerSkyLight && blockSnow <= SNOW_LAYER_MAX_VALUE - valueToAdd) {
                            setDoubleBlockState(world, pos, state, blockSnow + valueToAdd);
                        } else {
                            setDoubleBlockState(world, pos, state, (int) maxSnowPerSkyLight);
                        }
                    } else {
                        double neighboursInfluenceSubN = (Math.abs(neighboursInfluenceN - 1) * skyLightN) * NEIGHBOURS_INFLUENCE;
                        double neighboursInfluenceValueSub = neighboursInfluenceSubN * valueToChange;

                        int valueToSub = (int) Math.floor((mainInfluenceValue + neighboursInfluenceValueSub) / 2);
                        if (valueToSub == 0) valueToSub = 1;

                        if (blockSnow > 0 && blockSnow >= valueToSub) {
                            setDoubleBlockState(world, pos, state, blockSnow - valueToSub);
                        } else
                            setDoubleBlockState(world, pos, state, 0);
                    }
                }
            }
        }
    }

    public static void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        SnowLayerData snowLayerBlockData = LeaveslySettings.getSettings().getBlock(state.getBlock()).getSnowLayer();
        if (LeaveslySettings.getSettings().getSnowLayer().isEnabled() && LeaveslySettings.getSettings().getBlock(state.getBlock()).getSnowLayer().isEnabled()) {
            if (world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.SNOW) {
                int skyLight = world.getLightLevel(LightType.SKY, pos);

                if (skyLight > SKYLIGHT_CUTOFF_VALUE) {
                    int blockSnow = state.get(SNOW_LAYER);
                    double skyLightN = (double) (skyLight - SKYLIGHT_CUTOFF_VALUE) / (SKYLIGHT_MAX_VALUE - SKYLIGHT_CUTOFF_VALUE);
                    double neighboursInfluenceN = getNeighboursInfluence(world, pos, NEIGHBOURS_INFLUENCE_RANGE);
                    double maxSnowPerSkyLight = skyLightN * SNOW_LAYER_MAX_VALUE;
                    double mainInfluenceN = skyLightN * MAIN_INFLUENCE;
                    double valueToChange = getBlockValueToChange(state);
                    double mainInfluenceValue = mainInfluenceN * valueToChange;

                    if (world.isRaining()) {
                        double neighboursInfluenceAddN = (neighboursInfluenceN * skyLightN) * NEIGHBOURS_INFLUENCE;
                        double neighboursInfluenceValueAdd = neighboursInfluenceAddN * valueToChange;

                        int valueToAdd = (int) Math.floor(mainInfluenceValue + neighboursInfluenceValueAdd);
                        if (valueToAdd == 0) valueToAdd = 1;

                        if (blockSnow < maxSnowPerSkyLight && blockSnow <= SNOW_LAYER_MAX_VALUE - valueToAdd) {
                            setBlockState(world, pos, state, blockSnow + valueToAdd);
                        } else {
                            setBlockState(world, pos, state, (int) maxSnowPerSkyLight);
                        }
                    } else {
                        double neighboursInfluenceSubN = (Math.abs(neighboursInfluenceN - 1) * skyLightN) * NEIGHBOURS_INFLUENCE;
                        double neighboursInfluenceValueSub = neighboursInfluenceSubN * valueToChange;

                        int valueToSub = (int) Math.floor(mainInfluenceValue + neighboursInfluenceValueSub);
                        if (valueToSub == 0) valueToSub = 1;

                        if (blockSnow > 0 && blockSnow >= valueToSub) {
                            setBlockState(world, pos, state, blockSnow - valueToSub);
                        } else
                            setBlockState(world, pos, state, 0);
                    }
                }
            }
        }
    }

    public static void setDefaultState(Block block, StateManager<Block, BlockState> stateManager) {
        if (Blocks.isSupportedVanillaBlockClassName(block.getClass().getName()))
            stateManager.getDefaultState().with(SNOW_LAYER, 0);
    }

    public static void appendProperties(Block block, StateManager.Builder<Block, BlockState> builder) {
        if (Blocks.isSupportedVanillaBlockClassName(block.getClass().getName()))
            builder.add(SNOW_LAYER);
    }

    static double getNeighboursInfluence(ServerWorld world, BlockPos pos, int range) {
        int value = 0;
        int blockCount = 0;
        int foundRange = range;
        for (int x = pos.getX() - range; x <= pos.getX() + range; x++) {
            for (int y = pos.getY() - range; y <= pos.getY() + range; y++) {
                for (int z = pos.getZ() - range; z <= pos.getZ() + range; z++) {
                    if (isInRange(x, y, z, foundRange)) {
                        BlockPos rangePos = new BlockPos(x, y, z);
                        if (!rangePos.equals(pos)) {
                            BlockState rangeState = world.getBlockState(rangePos);
                            Block rangeBlock = rangeState.getBlock();
                            if (isSnowLayerBlock(rangeBlock)) {
                                value += rangeState.get(SNOW_LAYER);
                                blockCount++;
                                foundRange = getMaxValue(foundRange, x, y, z);
                            }
                        }
                    }
                }
            }
        }
        return ((double) value / blockCount) / SNOW_LAYER_MAX_VALUE;
    }

    static double getDoubleBlockNeighboursInfluence(ServerWorld world, BlockState state, BlockPos pos, int range) {
        int value = 0;
        int blockCount = 0;
        int foundRange = range;
        DoubleBlockHalf doubleBlockHalf = state.get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF);
        BlockPos doubleBlockHalfOffset;
        switch (doubleBlockHalf) {
            case LOWER -> doubleBlockHalfOffset = new BlockPos(0, 1, 0);
            case UPPER -> doubleBlockHalfOffset = new BlockPos(0, -1, 0);
            default -> doubleBlockHalfOffset = new BlockPos(0, 0, 0);
        }
        BlockPos halfPos = pos.add(doubleBlockHalfOffset);
        for (int x = pos.getX() - range; x <= pos.getX() + range; x++) {
            for (int y = pos.getY() - range; y <= pos.getY() + range; y++) {
                for (int z = pos.getZ() - range; z <= pos.getZ() + range; z++) {
                    if (isInRange(x, y, z, foundRange)) {
                        BlockPos rangePos = new BlockPos(x, y, z);
                        BlockPos rangeHalfPos = rangePos.add(doubleBlockHalfOffset);
                        if (!rangePos.equals(pos) & !rangeHalfPos.equals(halfPos)) {
                            BlockState rangeState = world.getBlockState(rangePos);
                            Block rangeBlock = rangeState.getBlock();
                            if (isSnowLayerBlock(rangeBlock)) {
                                value += rangeState.get(SNOW_LAYER);
                                blockCount++;
                                foundRange = getMaxValue(foundRange, x, y, z);
                            }
                        }
                    }
                }
            }
        }
        return ((double) value / blockCount) / SNOW_LAYER_MAX_VALUE;
    }

    static boolean isInRange(int x, int y, int z, int range) {
        return (x >= x - range & x <= x + range) && (y >= y - range & y <= y + range) && (z >= z - range & z <= z + range);
    }

    static int getMaxValue(int current, int x, int y, int z) {
        int currentValue = current;
        if (Math.abs(x) > currentValue)
            currentValue = Math.abs(x);
        else if (Math.abs(y) > currentValue)
            currentValue = Math.abs(y);
        else if (Math.abs(z) > currentValue)
            currentValue = Math.abs(z);
        return currentValue;
    }

    protected static boolean isSnowLayerBlock(Block block) {
        return Blocks.isRegisteredBlock(block);
    }

    protected static void setBlockState(ServerWorld world, BlockPos pos, BlockState state, int value) {
        world.setBlockState(pos, state.with(SNOW_LAYER, value));
    }
    protected static void setDoubleBlockState(ServerWorld world, BlockPos pos, BlockState state, int value) {
        world.setBlockState(pos, state.with(SNOW_LAYER, value));
        DoubleBlockHalf doubleBlockHalf = state.get(net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF);
        BlockPos halfBlockPos = switch (doubleBlockHalf) {
            case LOWER -> pos.add(0, 1, 0);
            case UPPER -> pos.add(0, -1, 0);
        };
        BlockState halfBlockState = world.getBlockState(halfBlockPos);
        world.setBlockState(halfBlockPos, halfBlockState.with(SNOW_LAYER, value));
    }

    protected static double getBlockValueToChange(BlockState state) {
        String blockId = Blocks.getBlockId(state.getBlock());
        return (LeaveslySettings.getSettings().getSnowLayer().getTransitionSpeed() * LeaveslySettings.getSettings().getBlock(blockId).getSnowLayer().getTransitionSpeed()) * SNOW_LAYER_MAX_VALUE;
    }
}