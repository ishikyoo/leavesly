package com.ishikyoo.leavesly.snowlayer;

import com.ishikyoo.iyoo.state.BitsmartStateAccessor;
import com.ishikyoo.iyoo.state.property.BitsmartProperty;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.iyoo.state.property.SubProperty;
import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.leavesly.settings.BlockData;
import com.ishikyoo.iyoo.util.MathUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import org.slf4j.Logger;

import static net.minecraft.state.property.Properties.DOUBLE_BLOCK_HALF;

public class SnowLayerBlock {
    public static final int SKIP_SNOW_LAYER = 65536;
    public static final int SET_BLOCK_STATE_FLAGS = Block.SKIP_DROPS |
            Block.SKIP_REDSTONE_WIRE_STATE_REPLACEMENT |
            Block.SKIP_BLOCK_ENTITY_REPLACED_CALLBACK |
            Block.SKIP_BLOCK_ADDED_CALLBACK |
            SKIP_SNOW_LAYER;
    public static final int SKY_LIGHT_MAX = 15;
    public static final int SKY_LIGHT_MIN = 10;
    public static final int BLOCK_LIGHT_MAX = 13;
    public static final int BLOCK_LIGHT_MIN = 0;
    public static final Identifier PROPERTY_ID = Identifier.of(Leavesly.MOD_ID, "snow_layer");
    public static final String PROPERTY_COVERAGE_ID = "coverage";
    public static final String PROPERTY_DYNAMIC_ID = "dynamic";
    public static final int PROPERTY_COVERAGE_MIN = 0;
    public static final int PROPERTY_COVERAGE_MAX = 31;

    private static final Logger LOG = Leavesly.LOGGER;

    private final SnowLayerChunk chunk;
    private final BlockPos position;
    private final BitsmartProperty property;
    private final SubProperty<Integer> coverageProperty;
    private final SubProperty<Boolean> dynamicProperty;

    private BitsmartStateAccessor accessor;

    private int skyLight;
    private int blockLight;
    private double skyLightNormal;
    private double blockLightNormal;
    private double previousLightNormal;

    private int coverageRaw;
    private int coverageMinRaw;
    private int coverageMaxRaw;

    private double coverage;
    private double coverageMin;
    private double coverageMax;
    private double coverageMaxOverride;
    private boolean shouldCoverageMaxOverride;

    private int direction;
    private int previousDirection;
    private boolean active;

    private boolean changeBlockLight;
    private boolean changeDirection;
    private boolean changeActivity;
    private boolean changeBlocksCount;

    private boolean dynamic;


    private SnowLayerBlock(SnowLayerChunk chunk, BlockPos position) {
        this.chunk = chunk;
        this.position = position;
        this.property = BitsmartRegistry.get(PROPERTY_ID);
        this.coverageProperty = property.getSubProperty(PROPERTY_COVERAGE_ID, Integer.class);
        this.dynamicProperty = property.getSubProperty(PROPERTY_DYNAMIC_ID, Boolean.class);
    }

    public static SnowLayerBlock of(SnowLayerChunk chunk, BlockPos position) {
        SnowLayerBlock block = new SnowLayerBlock(chunk, position);
        if (block.isSupported()) {
            block.accessor = BitsmartStateAccessor.of(block.getBlockState(), BitsmartRegistry.get(PROPERTY_ID));
            block.update(true);
        }
        return block;
    }

    private void update(boolean initialization) {
        //update light values
        skyLight = getWorld().getLightLevel(LightType.SKY, position);
        blockLight = getWorld().getLightLevel(LightType.BLOCK, position);
        skyLightNormal = MathUtils.normalize(skyLight, SKY_LIGHT_MIN, SKY_LIGHT_MAX);
        previousLightNormal = blockLightNormal;
        blockLightNormal = MathUtils.normalize(blockLight, BLOCK_LIGHT_MIN, BLOCK_LIGHT_MAX);
        //update coverage values
        if (initialization)
            coverage = skyLightNormal == 0 ? 0 :
                    accessor.get(coverageProperty) / (skyLightNormal * PROPERTY_COVERAGE_MAX);
        coverageMin = 0;
        coverageMax = Math.abs(blockLightNormal - 1);
        coverageRaw = (int) (coverage * PROPERTY_COVERAGE_MAX);
        coverageMinRaw = 0;
        coverageMaxRaw = (int) (coverageMax * PROPERTY_COVERAGE_MAX);
        //update direction values
        previousDirection = direction;
        if ((!isRaining() & coverageRaw > coverageMinRaw) | (coverageRaw > coverageMaxRaw))
            direction = -1;
        else if (isRaining() & (coverageRaw < coverageMaxRaw))
            direction = 1;
        else if (direction != 0)
            direction = 0;
        if (direction != previousDirection)
            changeDirection = true;
        //update light type
        if (!initialization) {
            if ((!dynamic & hasBlockLight())) {
                dynamic = true;
                changeBlockLight = true;
                changeBlocksCount = true;
            } else if ((dynamic & direction == -1 & !hasBlockLight() & coverageRaw == coverageMinRaw) |
                    (dynamic & direction == 1 & !hasBlockLight() & coverageRaw == coverageMaxRaw)) {
                dynamic = false;
                changeBlockLight = true;
                changeBlocksCount = true;
            }
        } else {
            //dynamic = MathUtils.normalize(properties.getBlockLight(), BLOCK_LIGHT_MIN, BLOCK_LIGHT_MAX);
            //dynamic = blockLightNormal;
            dynamic = accessor.is(dynamicProperty);
            changeBlockLight = false;
        }
        //update activity
        if (!active && this.direction != 0) {
            active = true;
            if (!initialization) {
                changeActivity = true;
                changeBlocksCount = true;
            }
        } else if (active && this.direction == 0) {
            active = false;
            if (!initialization) {
                changeActivity = true;
                changeBlocksCount = true;
            }
        }
    }

    public void tick(double delta) {
        update(false);
        //apply changes
        if (changeBlocksCount) {
            chunk.updateBlocksCount(this, false);
            changeBlocksCount = false;
        }
        if (changeBlockLight) {
            onBlockLightChange(dynamic, blockLightNormal, previousLightNormal);
            changeBlockLight = false;
        }
        if (changeDirection) {
            onDirectionChange(direction, previousDirection);
            changeDirection = false;
        }
        if (changeActivity) {
            onActivityChange(active);
            changeActivity = false;
        }
        //update coverage value
        if (active)
            onCoverageChange(delta);
    }

    private void onDirectionChange(double current, double previous) {

    }

    private void onActivityChange(boolean active) {
        if (isDebug()) {
            if (active)
                LOG.info("[DEBUG] Changing snow layer block to active (x={}, y={}, z={})",
                        position.getX(), position.getY(), position.getZ());
            else
                LOG.info("[DEBUG] Changing snow layer block to inactive (x={}, y={}, z={})",
                        position.getX(), position.getY(), position.getZ());
        }
    }

    private void onBlockLightChange(boolean dynamic, double current, double previous) {
//        if (!dynamic && (current > previous)) {
//
//        }
//        if (shouldBlockLightNormalOverride)
//            coverageMaxOverride = previous;
        setDynamic(dynamic);
    }

    private void onCoverageChange(double delta) {
        double valueToChange = 0;
        double valueMin = 0;
        double valueMax = 1;
        double blockLightFactor = 3;
        if (direction == 1) {
            valueToChange = skyLightNormal * (!hasBlockLight() ? delta : delta - (blockLightNormal * (delta * (delta / (blockLightFactor + 1) * blockLightFactor))));
        } else if (direction == -1) {
            valueToChange = skyLightNormal * (!hasBlockLight() ? delta : delta + (blockLightNormal * (delta * blockLightFactor)));
            if (coverage > coverageMax) {
                valueMin = coverageMax;
                valueMax = PROPERTY_COVERAGE_MAX;
            } else if (coverage <= coverageMax) {
                valueMin = 0;
                valueMax = coverageMax;
            }
        }
        setCoverage(coverage + (direction * valueToChange), valueMin, valueMax, false, true);
    }

    public SnowLayerChunk getChunk() {
        return chunk;
    }

    public SnowLayerBlock getHalfBlock() {
        if (!isDoubleBlock()) {
            LOG.error("Trying to get snow layer properties from unsupported block! (block: {}, position: x={}, y={}, z={})",
                    Blocks.getBlockId(getBlockState().getBlock()), position.getX(), position.getY(), position.getZ());
            return null;
        }
        DoubleBlockHalf doubleBlockHalf = getBlockState().get(DOUBLE_BLOCK_HALF);
        BlockPos halfBlockPos = switch (doubleBlockHalf) {
            case LOWER -> position.add(0, 1, 0);
            case UPPER -> position.add(0, -1, 0);
        };
        return SnowLayerBlock.of(chunk, halfBlockPos);
    }

    public BlockState getBlockState() {
        return getChunk().getWorldChunk().getBlockState(position);
    }

    public BlockPos getPosition() {
        return position;
    }

    public boolean isDoubleBlock() {
        return getBlockState().contains(DOUBLE_BLOCK_HALF);
    }

    public boolean isSupported() {
        BlockData blockData = Leavesly.getSettings().getDefault().getBlock(getBlockState().getBlock());
        if (blockData == null)
            return false;
        if (!blockData.getSnowLayer().isEnabled())
            return false;
        return getBlockState().contains(property.property());
    }

    public boolean hasBlockLight() {
        return blockLight > BLOCK_LIGHT_MIN;
    }

    public int getSkyLight() {
        return skyLight;
    }

    public int getBlockLight() {
        return blockLight;
    }

    public boolean isEnabled() {
        if (!Leavesly.getSettings().getCurrent().containsBlock(getBlockState().getBlock()))
            return false;
        BlockData blockData = Leavesly.getSettings().getCurrent().getBlock(getBlockState().getBlock());
        return blockData.getSnowLayer().isEnabled();
    }

    public double getCoverageMin(boolean raw) {
        return raw ? coverageMinRaw : coverageMin;
    }

    public double getCoverageMax(boolean raw) {
        return raw ? coverageMaxRaw : coverageMax;
    }

    public double getCoverage() {
        return getCoverage(false, false);
    }

    public double getCoverage(boolean raw, boolean normalized) {
        return coverageMax == 0 ? 1 :
                raw ? (normalized ?
                        (int) ((coverage / (shouldCoverageMaxOverride ? coverageMaxOverride : coverageMax)) * PROPERTY_COVERAGE_MAX) :
                        coverageRaw) :
                (normalized ?
                        coverage / (shouldCoverageMaxOverride ? coverageMaxOverride : coverageMax) :
                        coverage);
    }

    public boolean setCoverage(double value) {
        return setCoverage(value, 0, 1);
    }

    public boolean setCoverage(double value, double min, double max) {
        return setCoverage(value, min, max, false, true);
    }

    public boolean setCoverage(double value, double min, double max, boolean normalized, boolean update) {
        if (normalized) {
            min = MathHelper.clamp(MathHelper.lerp(min, 0, coverageMax), 0, 1);
            max = MathHelper.clamp(MathHelper.lerp(max, 0, coverageMax), 0, 1);
            value = MathHelper.clamp(MathHelper.lerp(value, 0, coverageMax), 0, max);
        } else {
            min = MathHelper.clamp(min, 0, 1);
            max = MathHelper.clamp(max, 0, 1);
            value = MathHelper.clamp(value, min, max);
        }
        coverage = value;
        if (update) {
            double _coverageMinRaw = min * PROPERTY_COVERAGE_MAX;
            double _coverageMaxRaw = max * PROPERTY_COVERAGE_MAX;
            int _coverageRaw = (int) MathHelper.clamp((skyLightNormal * coverage) * PROPERTY_COVERAGE_MAX,
                    _coverageMinRaw,
                    _coverageMaxRaw);
            if (_coverageRaw == accessor.get(coverageProperty))
                return false;
            accessor = accessor.with(coverageProperty, _coverageRaw);
            setBlockState(Block.NOTIFY_LISTENERS);
            return true;
        }
        return false;
    }

    public double getSkyLightNormal() {
        return skyLightNormal;
    }

    public double getBlockLightNormal() {
        return blockLightNormal;
    }

    public boolean isActive() {
        return active;
    }

    private double _getSkyLightNormal() {
        return MathHelper.clamp(MathHelper.clamp((double) getSkyLight() - SKY_LIGHT_MIN, 0, SKY_LIGHT_MAX) /
                MathHelper.clamp((double) SKY_LIGHT_MAX - SKY_LIGHT_MIN, 0, SKY_LIGHT_MAX),
                0, 1);
    }

    private double _getBlockLightNormal() {
        return MathHelper.clamp(MathHelper.clamp((double) getBlockLight() - BLOCK_LIGHT_MIN, 0, BLOCK_LIGHT_MAX) /
                MathHelper.clamp((double) BLOCK_LIGHT_MAX - BLOCK_LIGHT_MIN, 0, BLOCK_LIGHT_MAX),
                0, 1);
    }

    private boolean hasSkyLight() {
        return skyLightNormal > 0;
    }

    public boolean isDynamic() {
        return dynamic;
    }

//    private void setDynamic() {
//        if ((!dynamic && hasBlockLight()))
//            setDynamic(true, true);
//        else if ((dynamic && !hasBlockLight() && coverageRaw == coverageMinRaw) |
//                (dynamic && !hasBlockLight() && coverageRaw == coverageMaxRaw))
//            setDynamic(false, true);
//    }

    private boolean getDynamic() {
        return accessor.is(dynamicProperty);
    }

    private void setDynamic(boolean value) {
        if (!isSupported())
            LOG.error("Trying to set snow layer dynamic properties to unsupported block! (block: {}, position: x={}, y={}, z={})",
                    Blocks.getBlockId(getBlockState().getBlock()), position.getX(), position.getY(), position.getZ());
        if (value == accessor.is(dynamicProperty))
            return;
        if (isDebug()) {
            if (value) {
                LOG.debug("[DEBUG] Changing snow layer block to dynamic (block: x={}, y={}, z={}, chunk: x={}, z={})",
                        position.getX(), position.getY(), position.getZ(),
                        getChunk().getPosition().x, getChunk().getPosition().z);
            } else {
                LOG.debug("[DEBUG] Changing snow layer block to static (block: x={}, y={}, z={}, chunk: x={}, z={})",
                        position.getX(), position.getY(), position.getZ(),
                        getChunk().getPosition().x, getChunk().getPosition().z);
            }
        }
        accessor = accessor.with(dynamicProperty, value);
        setBlockState(Block.FORCE_STATE);
    }

    private void setBlockState(int flags) {
        if (isDoubleBlock()) {
            SnowLayerBlock halfBlock = getHalfBlock();
            BlockPos halfBlockPos = halfBlock.getPosition();
            if (!halfBlock.isSupported()) {
                LOG.error("Trying to set snow layer property to unsupported half block! (block: {}, position: x={}, y={}, z={})",
                        Blocks.getBlockId(getBlockState().getBlock()), halfBlockPos.getX(), halfBlockPos.getY(), halfBlockPos.getZ());
                return;
            }
            BitsmartStateAccessor halfBlockAccessor = BitsmartStateAccessor.of(halfBlock.getBlockState(), property, accessor.raw());
            getChunk().getWorld().setBlockState(halfBlock.getPosition(), halfBlockAccessor.state(), SET_BLOCK_STATE_FLAGS | flags);
        }
        getChunk().getWorld().setBlockState(position, accessor.state(), SET_BLOCK_STATE_FLAGS | flags);
    }

    private boolean isRaining() {
        return getWorld().isRaining();
    }

    private ServerWorld getWorld() {
        return getChunk().getWorld();
    }

    public static boolean isDebug() {
        return Leavesly.isDebug();
    }
}