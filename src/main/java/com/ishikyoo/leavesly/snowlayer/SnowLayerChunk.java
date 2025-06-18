package com.ishikyoo.leavesly.snowlayer;

import com.ishikyoo.leavesly.Leavesly;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SnowLayerChunk {
    private SnowLayerChunk(WorldChunk chunk) {
        this.chunk = chunk;
    }

    private static final Logger LOG = Leavesly.LOGGER;

    private final ConcurrentHashMap<BlockPos, SnowLayerBlock> blockHashMap = new ConcurrentHashMap<>();

    private final List<BlockPos> activeBlocksList = new ArrayList<>();
    private final List<BlockPos> inactiveBlocksList = new ArrayList<>();
    private final Set<BlockPos> activeDynamicBlocksHashSet = new HashSet<>();
    private final Set<BlockPos> activeStaticBlocksHashSet = new HashSet<>();
    private final Set<BlockPos> inactiveDynamicBlocksHashSet = new HashSet<>();
    private final Set<BlockPos> inactiveStaticBlocksHashSet = new HashSet<>();

    private int activeNextBlockPosArrayListIndex;
    private int inactiveNextBlockPosArrayListIndex;

    private final WorldChunk chunk;

    private boolean inSync;
    private int cycleTick;
    private int lod;

    private boolean snowyBiome;


    public static SnowLayerChunk of(WorldChunk chunk, boolean initialize) {
        SnowLayerChunk snowLayerChunk = new SnowLayerChunk(chunk);
        snowLayerChunk.activeNextBlockPosArrayListIndex = 0;
        snowLayerChunk.inactiveNextBlockPosArrayListIndex = 0;
        snowLayerChunk.lod = -1;
        if (initialize) {
            if (isDebug()) {
                LOG.info("[DEBUG] Initializing snow layer chunk (x={}, z={})...",
                        snowLayerChunk.getPosition().x, snowLayerChunk.getPosition().z);
            }
            ChunkPos chunkPos = chunk.getPos();
            BlockPos startPos = chunk.getPos().getStartPos();
            BlockPos endPos = new BlockPos(chunkPos.getEndX(), chunk.getHeight(), chunkPos.getEndZ());
            ServerWorld world = snowLayerChunk.getWorld();
            for (int x = startPos.getX(); x <= endPos.getX(); x++) {
                for (int y = startPos.getY(); y <= endPos.getY(); y++) {
                    for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
                        BlockPos blockPos = new BlockPos(x, y + chunk.getBottomY(), z);
                        SnowLayerBlock snowLayerBlock = SnowLayerBlock.of(snowLayerChunk, blockPos);
                        if (!snowLayerBlock.isSupported())
                            continue;
                        if (!world.getBiome(blockPos).value().doesNotSnow(blockPos, world.getSeaLevel())) {
                            snowLayerChunk.snowyBiome = true;
                            snowLayerChunk.putBlock(snowLayerBlock);
                        }
                    }
                }
            }
        }
        snowLayerChunk.setInSync(true);
        return snowLayerChunk;
    }

    public void tick(int lod, int totalTicks, int startTick, int ticksCount, double value) {
        if (cycleTick > totalTicks)
            cycleTick = 0;
        //
        sync();
        if (shouldTick(startTick, ticksCount)) {
            if (isActive()) {
                if (this.lod >= 0 && this.lod != lod) {
                    onLODChange(this.lod, lod);
                }
                this.lod = lod;
                //tick active blocks
                BlockPos blockPos;
                SnowLayerBlock snowLayerBlock;
                double finalValue = MathHelper.lerp((double) getBlocksCount(true) / getBlocksCount(), 0, value);
                if (getBlocksCount(true) > 0) {
                    blockPos = getNextActiveBlockPosition();
                    snowLayerBlock = getBlock(blockPos);
                    snowLayerBlock.tick(finalValue);
                }
                //tick inactive blocks
                if (getBlocksCount(false) > 0) {
                    blockPos = getNextInactiveBlockPosition();
                    snowLayerBlock = getBlock(blockPos);
                    snowLayerBlock.tick(finalValue);
                }
            }
        }
        //
        if (cycleTick <= totalTicks)
            cycleTick++;
    }

    public BlockPos getNextActiveBlockPosition() {
        BlockPos pos = activeBlocksList.get(activeNextBlockPosArrayListIndex);
        if (activeNextBlockPosArrayListIndex == 0) {
            Collections.shuffle(activeBlocksList);
        } else if (activeNextBlockPosArrayListIndex < getBlocksCount(true) - 1) {
            activeNextBlockPosArrayListIndex++;
        } else {
            activeNextBlockPosArrayListIndex = 0;
            Collections.shuffle(activeBlocksList);
        }
        return pos;
    }

    public BlockPos getNextInactiveBlockPosition() {
        BlockPos pos = inactiveBlocksList.get(inactiveNextBlockPosArrayListIndex);
        if (inactiveNextBlockPosArrayListIndex == 0) {
            Collections.shuffle(inactiveBlocksList);
        } else if (inactiveNextBlockPosArrayListIndex < getBlocksCount(false) - 1) {
            inactiveNextBlockPosArrayListIndex++;
        } else {
            inactiveNextBlockPosArrayListIndex = 0;
        }
        return pos;
    }

    private void onLODChange(int previous, int current) {
        if (current > previous)
            return;
        if (isDebug() ? current == 0 : current == 1 | current == 3) {
            if (isDebug())
                LOG.info("[DEBUG] Changing snow layer chunk LOD to {}, from {} (x={}, z={})",
                        current, previous,
                        getPosition().x, getPosition().z);
            double staticCoverage = getCoverage(false, false,false);
            double dynamicCoverage = getCoverage(false, true,true);
            for (SnowLayerBlock block : blockHashMap.values()) {
                int coverageMin = (int) block.getCoverageMin(true);
                int coverageMax = (int) block.getCoverageMax(true);
                if (block.isDynamic()) {
                    int dynamicCoverageRaw = (int) getCoverage(true, false,true);
                    boolean update = current == 3 | (dynamicCoverageRaw == coverageMin | dynamicCoverageRaw == coverageMax);
                    block.setCoverage(dynamicCoverage, 0, dynamicCoverage, true, update);
                } else {
                    int staticCoverageRaw = (int) getCoverage(true, false,false);
                    boolean update = current == 3 | (staticCoverageRaw == coverageMin | staticCoverageRaw == coverageMax);
                    block.setCoverage(staticCoverage, 0, 1, false, update);
                }
            }
        }
    }

    public double getCoverage(boolean raw, boolean normalized, boolean dynamic) {
        int activeBlocksCount = getBlocksCount(true, dynamic);
        int inactiveBlocksCount = getBlocksCount(false, dynamic);
        if (activeBlocksCount == 0 & inactiveBlocksCount == 0)
            return 0;
        double activeCoverage = 0;
        double inactiveCoverage = 0;
        Set<BlockPos> activeSet = dynamic ? activeDynamicBlocksHashSet : activeStaticBlocksHashSet;
        Set<BlockPos> inactiveSet = dynamic ? inactiveDynamicBlocksHashSet : inactiveStaticBlocksHashSet;
        for (BlockPos pos : activeSet) {
            SnowLayerBlock block = getBlock(pos);
            activeCoverage += block.getCoverage(raw, normalized);
        }
        for (BlockPos pos : inactiveSet) {
            SnowLayerBlock block = getBlock(pos);
            inactiveCoverage += block.getCoverage(raw, normalized);
        }
        double blocksCount = activeBlocksCount + inactiveBlocksCount;
        double coverage = activeCoverage + inactiveCoverage;
        return coverage / blocksCount;
    }

    public SnowLayerBlock getBlock(BlockPos position) {
        SnowLayerBlock block = blockHashMap.get(position);
        if (block == null) {
            block = SnowLayerBlock.of(this, position);
            if (block.isDoubleBlock())
                block = blockHashMap.get(block.getHalfBlock().getPosition());
            else
                block = null;
        }
        return block;
    }

    public int getBlocksCount() {
        return blockHashMap.size();
    }

    public int getBlocksCount(boolean active) {
        if (active)
            return activeBlocksList.size();
        else
            return inactiveBlocksList.size();
    }

    public int getBlocksCount(boolean active, boolean dynamic) {
        if (active) {
            if (dynamic)
                return activeDynamicBlocksHashSet.size();
            else
                return activeStaticBlocksHashSet.size();
        } else {
            if (dynamic)
                return inactiveDynamicBlocksHashSet.size();
            else
                return inactiveStaticBlocksHashSet.size();
        }
    }

    public boolean containsBlock(SnowLayerBlock block) {
        BlockPos pos = block.getPosition();
        if (blockHashMap.containsKey(pos))
            return true;
        return block.isDoubleBlock() && blockHashMap.containsKey(block.getHalfBlock().getPosition());
    }

    public boolean containsBlock(BlockPos position) {
        return containsBlock(SnowLayerBlock.of(this, position));
    }

    public ChunkPos getPosition() {
        return chunk.getPos();
    }

    public WorldChunk getWorldChunk() {
        return chunk;
    }

    public void setInSync(boolean value) {
        inSync = value;
    }

    public boolean isInSync() {
        return inSync;
    }

    public int getCycleTick() {
        return cycleTick;
    }

    private boolean shouldTick(int startTick, int ticksCount) {
        return isSupported() && (cycleTick >= startTick && cycleTick < startTick + ticksCount);
    }

    public boolean putBlock(BlockPos position) {
        return putBlock(SnowLayerBlock.of(this, position));
    }

    public boolean putBlock(SnowLayerBlock block) {
        BlockPos position = block.getPosition();
        if (containsBlock(position))
            return false;
        if (isDebug())
            LOG.info("[DEBUG] Adding block to snow layer chunk (block: x={}, y={}, z={}, chunk: x={}, z={})",
                    position.getX(), position.getY(), position.getZ(),
                    getPosition().x, getPosition().z);
        blockHashMap.put(position, block);
        updateBlocksCount(block, false);
        return true;
    }

    public boolean removeBlock(BlockPos position) {
        SnowLayerBlock block = getBlock(position);
        if (block != null)
            return removeBlock(block);
        block = SnowLayerBlock.of(this, position);
        return removeBlock(block);
    }

    public boolean removeBlock(SnowLayerBlock block) {
        BlockPos position = block.getPosition();
        if (!containsBlock(position))
            return false;
        if (isDebug())
            LOG.info("[DEBUG] Removing block from snow layer chunk (block: x={}, y={}, z={}, chunk: x={}, z={})",
                    position.getX(), position.getY(), position.getZ(),
                    getPosition().x, getPosition().z);
        blockHashMap.remove(block.getPosition());
        updateBlocksCount(block, true);
        return true;
    }

    public ServerWorld getWorld() {
        return getSystem().getWorld();
    }

    public boolean isSupported() {
        return getBlocksCount() > 0;
    }

    public boolean isActive() {
        return getBlocksCount() > 0;
    }

    public boolean hasSnowyBiome() {
        return snowyBiome;
    }

    protected void sync() {
        if (isInSync())
            return;
        if (isDebug()) {
            LOG.info("[DEBUG] Syncing snow layer chunk (x={}, z={})...", getPosition().x, getPosition().z);
        }
        BlockPos startPos = getPosition().getStartPos();
        BlockPos endPos = new BlockPos(getPosition().getEndX(), chunk.getHeight(), getPosition().getEndZ());
        for (int x = startPos.getX(); x <= endPos.getX(); x++) {
            for (int y = startPos.getY(); y <= endPos.getY(); y++) {
                for (int z = startPos.getZ(); z <= endPos.getZ(); z++) {
                    BlockPos blockPos = new BlockPos(x, y + chunk.getBottomY(), z);
                    SnowLayerBlock block = getBlock(blockPos);
                    if (block != null) {
                        if (!block.isSupported())
                            removeBlock(block);
                    } else {
                        block = SnowLayerBlock.of(this, blockPos);
                        if (block.isSupported())
                            putBlock(block);
                    }
                }
            }
        }
        Collections.shuffle(activeBlocksList);
        setInSync(true);
    }

    protected void updateBlocksCount(SnowLayerBlock block, boolean remove) {
        BlockPos blockPos = block.getPosition();
        boolean added = false;
        boolean addedActive = false;
        boolean addedInactive = false;
        boolean addedActiveDynamic = false;
        boolean addedActiveStatic = false;
        boolean addedInactiveDynamic = false;
        boolean addedInactiveStatic = false;
        boolean removed = false;
        boolean removedActive = false;
        boolean removedInactive = false;
        boolean removedActiveDynamic = false;
        boolean removedActiveStatic = false;
        boolean removedInactiveDynamic = false;
        boolean removedInactiveStatic = false;
        if (remove) {
            removedActive = activeBlocksList.remove(blockPos);
            removedInactive = inactiveBlocksList.remove(blockPos);
            removedActiveDynamic = activeDynamicBlocksHashSet.remove(blockPos);
            removedActiveStatic = activeStaticBlocksHashSet.remove(blockPos);
            removedInactiveDynamic = inactiveDynamicBlocksHashSet.remove(blockPos);
            removedInactiveStatic = inactiveStaticBlocksHashSet.remove(blockPos);
        } else {
            if (block.isActive()) {
                if (!activeBlocksList.contains(blockPos)) {
                    addedActive = activeBlocksList.add(blockPos);
                    removedInactive = inactiveBlocksList.remove(blockPos);
                }
                if (block.isDynamic()) {
                    if (!activeDynamicBlocksHashSet.contains(blockPos)) {
                        addedActiveDynamic = activeDynamicBlocksHashSet.add(blockPos);
                        removedActiveStatic = activeStaticBlocksHashSet.remove(blockPos);
                        removedInactiveDynamic = inactiveDynamicBlocksHashSet.remove(blockPos);
                        removedInactiveStatic = inactiveStaticBlocksHashSet.remove(blockPos);
                    }
                } else {
                    if (!activeStaticBlocksHashSet.contains(blockPos)) {
                        addedActiveStatic = activeStaticBlocksHashSet.add(blockPos);
                        removedActiveDynamic = activeDynamicBlocksHashSet.remove(blockPos);
                        removedInactiveStatic = inactiveStaticBlocksHashSet.remove(blockPos);
                        removedInactiveDynamic = inactiveDynamicBlocksHashSet.remove(blockPos);
                    }
                }
            } else {
                if (!inactiveBlocksList.contains(blockPos)) {
                    addedInactive = inactiveBlocksList.add(blockPos);
                    removedActive = activeBlocksList.remove(blockPos);
                }
                if (block.isDynamic()) {
                    if (!inactiveDynamicBlocksHashSet.contains(blockPos)) {
                        addedInactiveDynamic = inactiveDynamicBlocksHashSet.add(blockPos);
                        removedInactiveStatic = inactiveStaticBlocksHashSet.remove(blockPos);
                        removedActiveDynamic = activeDynamicBlocksHashSet.remove(blockPos);
                        removedActiveStatic = activeStaticBlocksHashSet.remove(blockPos);
                    }
                } else {
                    if (!inactiveStaticBlocksHashSet.contains(blockPos)) {
                        addedInactiveStatic = inactiveStaticBlocksHashSet.add(blockPos);
                        removedInactiveDynamic = inactiveDynamicBlocksHashSet.remove(blockPos);
                        removedActiveStatic = activeStaticBlocksHashSet.remove(blockPos);
                        removedActiveDynamic = activeDynamicBlocksHashSet.remove(blockPos);
                    }
                }
            }
        }
        if (removedActive) {
            if (activeNextBlockPosArrayListIndex == getBlocksCount(true) - 1)
                activeNextBlockPosArrayListIndex = 0;
            else if (activeNextBlockPosArrayListIndex > 0)
                activeNextBlockPosArrayListIndex--;
        }
        if (removedInactive) {
            if (inactiveNextBlockPosArrayListIndex == getBlocksCount(false) - 1)
                inactiveNextBlockPosArrayListIndex = 0;
            else if (inactiveNextBlockPosArrayListIndex > 0)
                inactiveNextBlockPosArrayListIndex--;
        }
        boolean log = addedActiveDynamic | addedActiveStatic | addedInactiveDynamic | addedInactiveStatic |
                      removedActiveDynamic | removedActiveStatic | removedInactiveDynamic | removedInactiveStatic;
        if (isDebug() && log) {
            LOG.info("[DEBUG] Updating snow layer chunk blocks count (x={}, z={})...", getPosition().x, getPosition().z);
            LOG.info("[DEBUG] total={} [dynamic={}, static={}]",
                    getBlocksCount(), getBlocksCount(true, true) + getBlocksCount(false, true),
                    getBlocksCount(true, false) + getBlocksCount(false, false));
            LOG.info("[DEBUG] active={} [dynamic={}, static={}]",
                    getBlocksCount(true), getBlocksCount(true, true), getBlocksCount(true, false));
            LOG.info("[DEBUG] inactive={} [dynamic={}, static={}]",
                    getBlocksCount(false), getBlocksCount(false, true), getBlocksCount(false, false));
        }
    }

    private static boolean isDebug() {
        return Leavesly.isDebug();
    }

    private static boolean isToLog() {
        return Leavesly.getSettings().getCurrent().shouldLog();
    }

    private static SnowLayerSystem getSystem() {
        return Leavesly.getSnowLayerSystem();
    }
}
