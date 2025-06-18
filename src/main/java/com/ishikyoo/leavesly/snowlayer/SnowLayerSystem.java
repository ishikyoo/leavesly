package com.ishikyoo.leavesly.snowlayer;

import com.ishikyoo.leavesly.Leavesly;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SnowLayerSystem {
    public SnowLayerSystem() {

    }

    private static final Logger LOG = Leavesly.LOGGER;

    private static final double DEBUG_SIMULATION_DISTANCE = 1.5;
    private static final double DEBUG_UNLOAD_DISTANCE = DEBUG_SIMULATION_DISTANCE;

    private final static int TICKS_PER_SECOND = 20;

    private static final double CHUNK_UNLOAD_OFFSET = 3;

    private final static int SIMULATION_SPEED_MIN = 24000;
    private final static int SIMULATION_SPEED_MAX = 1200;
    private final static int SIMULATION_DISTANCE_MIN = 5;
    private final static int SIMULATION_DISTANCE_MAX = 16;

    private int cycleTicksPerGameTick;
    private double cycleLod0Distance;
    private double cycleLod1Distance;
    private double cycleLod2Distance;
    private double cycleLod3Distance;
    private int cycleLod0StartTick;
    private int cycleLod1StartTick;
    private int cycleLod2StartTick;
    private int cycleLod3StartTick;
    private int cycleLod4StartTick;
    private int cycleLod0Ticks;
    private int cycleLod1Ticks;
    private int cycleLod2Ticks;
    private int cycleLod3Ticks;
    private int cycleLod4Ticks;
    private int cycleTotalTicks;

    private double cycleValueChangePerTick;

    private int simulationSpeed;

    private double simulationSquaredDistance;
    private double unloadSquaredDistance;

    private ServerWorld world;
    private boolean enabled;

    private final Map<ChunkPos, SnowLayerChunk> chunksHashMap = new ConcurrentHashMap<>();
    private final Map<WorldChunk, Boolean> inRangeChunksHashMap = new ConcurrentHashMap<>();

    public void initialize(MinecraftServer server, ServerWorld world) {
        if (!isOverworld(world))
            return;
        LOG.info("Initializing snow layer system...");
        this.world = world;
        enabled = Leavesly.getSettings().getCurrent().getSnowLayer().isEnabled();
        ServerChunkEvents.CHUNK_UNLOAD.register(this::onChunkUnloadEvent);
        ServerChunkEvents.CHUNK_LOAD.register(this::onChunkLoadEvent);
    }

    private void onChunkLoadEvent(ServerWorld serverWorld, WorldChunk worldChunk) {
        if (!isOverworld(serverWorld))
            return;
        if (isEnabled()) {
            double distance = getMinPlayersDistanceFromChunk(worldChunk);
            if (distance <= simulationSquaredDistance)
                putChunk(worldChunk);
        }
    }

    private void onChunkUnloadEvent(ServerWorld serverWorld, WorldChunk worldChunk) {
        if (!isOverworld(serverWorld))
            return;
        if (isEnabled()) {
            removeChunk(worldChunk);
        }
    }

    public void setBlockState(BlockPos pos, World world, BlockState state, int flags) {
        if (!world.isClient()) {
            if ((flags & SnowLayerBlock.SKIP_SNOW_LAYER) == 0) {
                WorldChunk chunk = world.getWorldChunk(pos);
                SnowLayerChunk snowLayerChunk;
                SnowLayerBlock snowLayerBlock;
                if (isInRangeChunk(chunk)) {
                    if (isActiveChunk(chunk)) {
                        snowLayerChunk = getChunk(chunk);
                        if (((flags & Block.FORCE_STATE) == Block.FORCE_STATE)) {
                            snowLayerChunk.setInSync(false);
                        } else {
                            snowLayerBlock = snowLayerChunk.getBlock(pos);
                            if (snowLayerBlock != null) {
                                if (!snowLayerBlock.isSupported()) {
                                    snowLayerChunk.removeBlock(snowLayerBlock);
                                    if (!snowLayerChunk.isSupported()) {
                                        removeChunk(snowLayerChunk);
                                        setInRangeChunk(chunk, false);
                                    }
                                }
                            } else {
                                snowLayerBlock = SnowLayerBlock.of(snowLayerChunk, pos);
                                if (snowLayerBlock.isSupported())
                                    snowLayerChunk.putBlock(snowLayerBlock);
                            }
                        }
                    } else {
                        snowLayerChunk = SnowLayerChunk.of(chunk, false);
                        snowLayerBlock = SnowLayerBlock.of(snowLayerChunk, pos);
                        if (snowLayerBlock.isSupported()) {
                            snowLayerChunk.putBlock(snowLayerBlock);
                            putChunk(snowLayerChunk);
                            setInRangeChunk(chunk, true);
                        }
                    }
                }
            }
        }
    }

    public void tick(WorldChunk chunk) {
        if (shouldTick()) {
            double distance = getMinPlayersDistanceFromChunk(chunk);
            if (distance <= simulationSquaredDistance) {
                if (!isInRangeChunk(chunk))
                    putChunk(chunk);
                if (isActiveChunk(chunk)) {
                    SnowLayerChunk snowLayerChunk = getChunk(chunk);
                    int blockCount = snowLayerChunk.getBlocksCount();
                    int lod = getChunkLOD(distance);
                    int chunkLODStartTick = getChunkLODStartTick(lod);
                    int chunkLODTicks = getChunkLODTicks(lod);
                    int ticksPerSecond = getChunkTicksPerSecond(chunkLODTicks, blockCount);
                    double valueChange = getChunkValueChangePerTick(cycleValueChangePerTick, blockCount, ticksPerSecond);
                    for (int i = 0; i < cycleTicksPerGameTick; i++) {
                        snowLayerChunk.tick(lod, cycleTotalTicks, chunkLODStartTick, ticksPerSecond, valueChange);
                    }
                }
            } else if (distance > unloadSquaredDistance) {
                removeChunk(chunk);
            }
        }
    }

    public void setSimulationSpeed(int value) {
        if (simulationSpeed == value)
            return;
        int previousSimulationSpeed = simulationSpeed;
        simulationSpeed = value;
        cycleValueChangePerTick = 1d / MathHelper.clamp(simulationSpeed, SIMULATION_SPEED_MAX, SIMULATION_SPEED_MIN);
        LOG.info("Changing snow layer simulation speed to {}, from {}", simulationSpeed, previousSimulationSpeed);
    }

    public int getSimulationSpeed() {
        return simulationSpeed;
    }

    public void setSimulationDistance(int value) {
        double currentDistance = getSimulationDistance();
        if (currentDistance == value)
            return;
        if (isDebug()) {
            LOG.info("Changing snow layer simulation distance to {}, from {}", 1, (int) currentDistance);
            simulationSquaredDistance = MathHelper.square(DEBUG_SIMULATION_DISTANCE);
            unloadSquaredDistance = MathHelper.square(DEBUG_UNLOAD_DISTANCE);
            setCycleLODDistances(0.5, 1.5, 0, 0);
            setCycleTicksPerGameTick(1, 15, 5, 0, 0, 0);
        } else {
            LOG.info("Changing snow layer simulation distance to {}, from {}", value, (int) currentDistance);
            double simulationDistance = MathHelper.clamp(value, SIMULATION_DISTANCE_MIN, SIMULATION_DISTANCE_MAX);
            simulationSquaredDistance = MathHelper.square(simulationDistance);
            double unloadDistance = simulationDistance + CHUNK_UNLOAD_OFFSET;
            unloadSquaredDistance = MathHelper.square(unloadDistance);
            if (simulationDistance <= 6)
                setCycleLODDistances(1.5 , 3, 4, 5);
            else if (simulationDistance <= 8)
                setCycleLODDistances(1.5 , 3, 4, 6);
            else
                setCycleLODDistances(1.5 , 3, 5, 8);
            setCycleTicksPerGameTick(1, 9, 5, 3, 2, 1);
        }
    }



    public double getSimulationDistance() {
        return Math.sqrt(simulationSquaredDistance);
    }

    private void setCycleLODDistances(double lod0, double lod1, double lod2, double lod3) {
        cycleLod0Distance = MathHelper.square(lod0);
        cycleLod1Distance = MathHelper.square(lod1);
        cycleLod2Distance = MathHelper.square(lod2);
        cycleLod3Distance = MathHelper.square(lod3);
    }

    private void setCycleTicksPerGameTick(int value, int lod0Ticks, int lod1Ticks, int lod2Ticks, int lod3Ticks, int lod4Ticks) {
        cycleTicksPerGameTick = value;
        cycleLod0Ticks = cycleTicksPerGameTick * lod0Ticks;
        cycleLod1Ticks = cycleTicksPerGameTick * lod1Ticks;
        cycleLod2Ticks = cycleTicksPerGameTick * lod2Ticks;
        cycleLod3Ticks = cycleTicksPerGameTick * lod3Ticks;
        cycleLod4Ticks = cycleTicksPerGameTick * lod4Ticks;
        cycleLod0StartTick = 0;
        cycleLod1StartTick = cycleLod0Ticks;
        cycleLod2StartTick = cycleLod1Ticks + cycleLod1StartTick;
        cycleLod3StartTick = cycleLod2Ticks + cycleLod2StartTick;
        cycleLod4StartTick = cycleLod3Ticks + cycleLod3StartTick;
        cycleTotalTicks = cycleLod0Ticks + cycleLod1Ticks + cycleLod2Ticks + cycleLod3Ticks + cycleLod4Ticks;
    }

    private int getChunkTicksPerSecond(int LODTicks, int blocksCount) {
        return Math.min(blocksCount, LODTicks);
    }

    private double getMinPlayersDistanceFromChunk(WorldChunk chunk) {
        double minDistance = Integer.MAX_VALUE;
        List<? extends PlayerEntity> playerEntityList = world.getPlayers();
        if (!playerEntityList.isEmpty()) {
            for (PlayerEntity player : playerEntityList) {
                ChunkPos playerChunkPos = player.getChunkPos();
                ChunkPos chunkPos = chunk.getPos();
                double playerDistanceFromChunk = playerChunkPos.getSquaredDistance(chunkPos);
                minDistance = Math.min(minDistance, playerDistanceFromChunk);
                if (minDistance == 0)
                    break;
            }
        }
        return minDistance;
    }

    private int getChunkLOD(double distance) {
        if (distance < cycleLod0Distance)
            return 0;
        else if (distance < cycleLod1Distance)
            return 1;
        else if (distance < cycleLod2Distance)
            return 2;
        else if (distance < cycleLod3Distance)
            return 3;
        else
            return 4;
    }

    private int getChunkLODStartTick(int lod) {
        return switch (lod) {
            case 0 -> cycleLod0StartTick;
            case 1 -> cycleLod1StartTick;
            case 2 -> cycleLod2StartTick;
            case 3 -> cycleLod3StartTick;
            case 4 -> cycleLod4StartTick;
            default -> 0;
        };
    }

    private int getChunkLODTicks(int lod) {
        return switch (lod) {
            case 0 -> cycleLod0Ticks;
            case 1 -> cycleLod1Ticks;
            case 2 -> cycleLod2Ticks;
            case 3 -> cycleLod3Ticks;
            case 4 -> cycleLod4Ticks;
            default -> 0;
        };
    }

    private SnowLayerChunk putChunk(WorldChunk chunk) {
        if (isInRangeChunk(chunk))
            return null;
        SnowLayerChunk snowLayerChunk = SnowLayerChunk.of(chunk, true);
        setInRangeChunk(chunk, snowLayerChunk.isSupported());
        if (snowLayerChunk.isSupported()) {
            if (putChunk(snowLayerChunk))
                return snowLayerChunk;
        }
        return null;
    }

    public boolean putChunk(SnowLayerChunk chunk) {
        ChunkPos pos = chunk.getPosition();
        if (containsChunk(pos))
            return false;
        if (isDebug())
            LOG.info("[DEBUG] Adding chunk to snow layer system (x={}, z={})",
                    pos.x, pos.z);
        chunksHashMap.put(pos, chunk);
        return true;
    }

    public SnowLayerChunk getChunk(WorldChunk chunk) {
        return getChunk(chunk.getPos());
    }
    public SnowLayerChunk getChunk(ChunkPos position) {
        return chunksHashMap.get(position);
    }

    public boolean removeChunk(WorldChunk chunk) {
        if (!isInRangeChunk(chunk))
            return false;
        inRangeChunksHashMap.remove(chunk);
        return removeChunk(chunk.getPos());
    }

    public boolean removeChunk(SnowLayerChunk chunk) {
        return removeChunk(chunk.getPosition());
    }

    public boolean removeChunk(ChunkPos position) {
        if (!containsChunk(position))
            return false;
        if (isDebug())
            LOG.info("[DEBUG] Removing chunk from snow layer system (x={}, z={})",
                    position.x, position.z);
        chunksHashMap.remove(position);
        return true;
    }

    private void setInRangeChunk(WorldChunk chunk, boolean active) {
        if (isInRangeChunk(chunk))
            inRangeChunksHashMap.replace(chunk, active);
        else
            inRangeChunksHashMap.put(chunk, active);
    }

    public boolean containsBlock(BlockPos position) {
        SnowLayerChunk chunk = getChunk(new ChunkPos(position));
        if (chunk == null)
            return false;
        return chunk.containsBlock(position);
    }

    public boolean containsChunk(WorldChunk chunk) {
        return containsChunk(chunk.getPos());
    }

    public boolean containsChunk(ChunkPos position) {
        return chunksHashMap.containsKey(position);
    }

    public boolean isInRangeChunk(WorldChunk chunk) {
        return inRangeChunksHashMap.containsKey(chunk);
    }

    public boolean isActiveChunk(WorldChunk chunk) {
        return isInRangeChunk(chunk) ? inRangeChunksHashMap.get(chunk) : false;
    }

    public int getChunksCount() {
        return chunksHashMap.size();
    }

    private double getChunkValueChangePerTick(double value, int blockCount, int ticks) {
        double valueChangePerTick = ((double) TICKS_PER_SECOND / ticks) * value;
        return blockCount * valueChangePerTick;
    }

    private boolean shouldTick() {
        return isEnabled() &&
                isOverworld();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public ServerWorld getWorld() {
        return world;
    }

    private static boolean isDebug() {
        return Leavesly.isDebug();
    }

    private static boolean isOverworld(World world) {
        return world.getRegistryKey().equals(ServerWorld.OVERWORLD);
    }

    private boolean isOverworld() {
        return isOverworld(world);
    }
}