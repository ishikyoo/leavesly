package com.ishikyoo.leavesly;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LeaveslyBlockRegistry {
    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    static HashSet<String> preregistrationblockClassNamesHashSet = new HashSet<>();
    static final HashMap<String, String> blockIdsCompatibilityHashMap = new HashMap<>(Map.of(
            "minecraft:grass", "minecraft:short_grass"
    ));

    static HashMap<String, Block> blocksHashMap = new HashMap<>();
    static HashMap<Block, String> identifierHashMap = new HashMap<>();

    public static Block getBlock(String id) {
        Block block = blocksHashMap.get(id);
        if (block == null)
            LOGGER.warn("Trying to get unregistered block (Id: {})", id);
        return block;
    }
    public static String getBlock(Block block) {
        String id = identifierHashMap.get(block);
        if (id == null)
            LOGGER.warn("Trying to get unregistered block (Id: {})", id);
        return id;
    }

    public static boolean isPreregisteredBlockClass(Block block) {
        return preregistrationblockClassNamesHashSet.contains(block.getClass().getName());
    }
    public static boolean isRegisteredBlock(Block block) {
        return blocksHashMap.containsValue(block);
    }
    public static boolean isRegisteredBlock(String id) {
        return blocksHashMap.containsKey(id);
    }

    public static void register(String id, Block block) {
        if (!id.equals("minecraft:pitcher_plant")) {
            String blockId = getCompatibilityBlockId(id);
            if (!blocksHashMap.containsKey(blockId)) {
                blocksHashMap.put(blockId, block);
                identifierHashMap.put(block, blockId);
                LOGGER.info("Registered block (Id: {}, Class: {}).", id, block.getClass().getSimpleName());
            } else {
                LOGGER.warn("Trying to register already registered block (Id: {}, Class: {}).", id, block.getClass().getSimpleName());
            }
        }
    }

    public static void preregister(String mod, String blockClassName) {
        preregistrationblockClassNamesHashSet.add(blockClassName);
        LOGGER.info("Block class ready to be preregistered (Mod: {}, Class: {}).", mod, blockClassName);
    }

    static String getCompatibilityBlockId(String id) {
        String cId = blockIdsCompatibilityHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }
}