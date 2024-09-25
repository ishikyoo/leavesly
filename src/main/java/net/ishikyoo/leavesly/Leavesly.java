package net.ishikyoo.leavesly;

import net.fabricmc.api.ModInitializer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.state.property.IntProperty;

public class Leavesly implements ModInitializer {
    public static final String MOD_ID = "leavesly";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static Leavesly instance;

    static final int SKYLIGHT_MAX = 15;
    static final int SKYLIGHT_CUTOFF = 9;
    static final int SKYLIGHT_RANGE = SKYLIGHT_MAX - SKYLIGHT_CUTOFF;
    static final int SNOW_MAX_VALUE = 8 * SKYLIGHT_RANGE;
    static final int SNOW_VALUE_TO_CHANGE = (SNOW_MAX_VALUE + 1) / 2;
    static final double NEIGHBOURS_INFLUENCE = 0.875;
    static final double MAIN_INFLUENCE = Math.abs(NEIGHBOURS_INFLUENCE - 1);
    static final int NEIGHBOURS_INFLUENCE_RANGE = 1;
    public static final IntProperty SNOW = IntProperty.of("snow", 0, SNOW_MAX_VALUE);

    public static Leavesly getInstance() { return instance; }

    boolean isLeavesBlock(Block block) {
        if (block == Blocks.ACACIA_LEAVES)
            return true;
        else if (block == Blocks.AZALEA_LEAVES)
            return true;
        else if (block == Blocks.BIRCH_LEAVES)
            return true;
        else if (block == Blocks.CHERRY_LEAVES)
            return true;
        else if (block == Blocks.DARK_OAK_LEAVES)
            return true;
        else if (block == Blocks.FLOWERING_AZALEA_LEAVES)
            return true;
        else if (block == Blocks.JUNGLE_LEAVES)
            return true;
        else if (block == Blocks.MANGROVE_LEAVES)
            return true;
        else if (block == Blocks.OAK_LEAVES)
            return true;
        else if (block == Blocks.SPRUCE_LEAVES)
            return true;
        return false;
    }
    boolean isVineBlock(Block block) {
        return block == Blocks.VINE;
    }
    boolean isValidBlock(Block block) {
        return isLeavesBlock(block) | isVineBlock(block);
    }

    double getNeighboursInfluence(ServerWorld world, BlockPos pos, int range) {
        int value = 0;
        int blockCount = 0;
        for (int x = pos.getX() - range; x <= pos.getX() + range; x++) {
            for (int y = pos.getY() - range; y <= pos.getY() + range; y++) {
                for (int z = pos.getZ() - range; z <= pos.getZ() + range; z++) {
                    BlockPos rangePos = new BlockPos(x, y, z);
                    if (!rangePos.equals(pos)) {
                        BlockState rangeState = world.getBlockState(rangePos);
                        Block rangeBlock = rangeState.getBlock();
                        if (isValidBlock(rangeBlock)) {
                            value += rangeState.get(SNOW);
                            blockCount++;
                        }
                    }
                }
            }
        }
        return ((double) value / blockCount) / SNOW_MAX_VALUE;
    }

    @Override
    public void onInitialize() {
        instance = this;
    }

    public void tick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (isValidBlock(state.getBlock())) {
            if (world.getBiome(pos).value().getPrecipitation(pos) == Biome.Precipitation.SNOW) {
                int skyLight = world.getLightLevel(LightType.SKY, pos);

                if (skyLight > SKYLIGHT_CUTOFF) {
                    int blockSnow = state.get(SNOW);
                    double skyLightN = (double) (skyLight - SKYLIGHT_CUTOFF) / (SKYLIGHT_MAX - SKYLIGHT_CUTOFF);
                    double neighboursInfluenceN = getNeighboursInfluence(world, pos, NEIGHBOURS_INFLUENCE_RANGE);
                    double maxSnowPerSkyLight = skyLightN * SNOW_MAX_VALUE;
                    double mainInfluenceN = skyLightN * MAIN_INFLUENCE;
                    double mainInfluenceValue = mainInfluenceN * SNOW_VALUE_TO_CHANGE;

                    if (world.isRaining()) {
                        double neighboursInfluenceAddN = (neighboursInfluenceN * skyLightN) * NEIGHBOURS_INFLUENCE;
                        double neighboursInfluenceValueAdd = neighboursInfluenceAddN * SNOW_VALUE_TO_CHANGE;

                        int valueToAdd = (int) Math.floor(mainInfluenceValue + neighboursInfluenceValueAdd);
                        if (valueToAdd == 0) valueToAdd = 1;

                        if (blockSnow < maxSnowPerSkyLight && blockSnow <= SNOW_MAX_VALUE - valueToAdd)
                            world.setBlockState(pos, state.with(Leavesly.SNOW, blockSnow + valueToAdd));
                        else
                            world.setBlockState(pos, state.with(Leavesly.SNOW, (int) maxSnowPerSkyLight));
                    } else {
                        double neighboursInfluenceSubN = (Math.abs(neighboursInfluenceN - 1) * skyLightN) * NEIGHBOURS_INFLUENCE;
                        double neighboursInfluenceValueSub = neighboursInfluenceSubN * SNOW_VALUE_TO_CHANGE;

                        int valueToSub = (int) Math.floor(mainInfluenceValue + neighboursInfluenceValueSub);
                        if (valueToSub == 0) valueToSub = 1;

                        if (blockSnow > 0 && blockSnow >= valueToSub) {
                            world.setBlockState(pos, state.with(Leavesly.SNOW, blockSnow - valueToSub));
                        } else
                            world.setBlockState(pos, state.with(Leavesly.SNOW, 0));
                    }
                }
            }
        }
    }
}