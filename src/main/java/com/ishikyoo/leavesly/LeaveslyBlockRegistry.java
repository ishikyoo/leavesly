package com.ishikyoo.leavesly;

import net.minecraft.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LeaveslyBlockRegistry {
    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    static HashSet<String> preregistrationblockClassNamesHashSet = new HashSet<>();
    static final HashMap<String, String> blockIdsCompatibilityHashMap = new HashMap<>(Map.of(
            "grass", "short_grass"
    ));

    static HashMap<String, Block> blocksHashMap = new HashMap<>();

    public static Block getBlock(String mod, String id) {
        Block block = blocksHashMap.get(mod + ":" + id);
        if (block == null)
            LOGGER.warn("Trying to get unregistered block (Mod: {}, Id: {})", mod, id);
        return block;
    }

    public static boolean isRegisteredBlock(Block block) {
        return blocksHashMap.containsValue(block) || preregistrationblockClassNamesHashSet.contains(block.getClass().getName());
    }
    public static boolean isRegisteredBlock(String mod, String id) {
        return blocksHashMap.containsKey(mod + ":" + id);
    }

    public static void register(String mod, String id, Block block) {
        String blockId =  mod + ":" + getCompatibilityBlockId(id);
        if (!blocksHashMap.containsKey(blockId)) {
            blocksHashMap.put(blockId, block);
            LOGGER.info("Registered block (Mod: {}, Id: {}, Class: {}).", mod, id, block.getClass().getSimpleName());
        } else {
            LOGGER.warn("Trying to register already registered block (Mod: {}, Id: {}, Class: {}).", mod, id, block.getClass().getSimpleName());
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
