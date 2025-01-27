package com.ishikyoo.leavesly;

import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public final class LeaveslyBlockRegistry {
    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    static HashSet<String> preregistrationblockClassNamesHashSet = new HashSet<>();
    static final HashMap<Identifier, Identifier> blockIdsCompatibilityHashMap = new HashMap<>(Map.of(
            Identifier.of("minecraft:grass"), Identifier.of("minecraft:short_grass")
    ));

    static HashMap<Identifier, Block> blocksHashMap = new HashMap<>();
    static HashMap<Block, Identifier> identifierHashMap = new HashMap<>();

    public static Block getBlock(Identifier id) {
        Block block = blocksHashMap.get(id);
        if (block == null)
            LOGGER.warn("Trying to get unregistered block (Mod: {}, Id: {})", id.getNamespace(), id.getPath());
        return block;
    }
    public static Identifier getBlock(Block block) {
        Identifier id = identifierHashMap.get(block);
        if (id == null)
            LOGGER.warn("Trying to get unregistered block (Mod: {}, Id: {})", id.getNamespace(), id.getPath());
        return id;
    }

    public static boolean isRegisteredBlock(Block block) {
        return blocksHashMap.containsValue(block) || preregistrationblockClassNamesHashSet.contains(block.getClass().getName());
    }
    public static boolean isRegisteredBlock(Identifier id) {
        return blocksHashMap.containsKey(id);
    }

    public static void register(Identifier id, Block block) {
        Identifier blockId =  getCompatibilityBlockId(id);
        if (!blocksHashMap.containsKey(blockId)) {
            blocksHashMap.put(blockId, block);
            identifierHashMap.put(block, blockId);
            LOGGER.info("Registered block (Mod: {}, Id: {}, Class: {}).", id.getNamespace(), id.getPath(), block.getClass().getSimpleName());
        } else {
            LOGGER.warn("Trying to register already registered block (Mod: {}, Id: {}, Class: {}).", id.getNamespace(), id.getPath(), block.getClass().getSimpleName());
        }
    }

    public static void preregister(String mod, String blockClassName) {
        preregistrationblockClassNamesHashSet.add(blockClassName);
        LOGGER.info("Block class ready to be preregistered (Mod: {}, Class: {}).", mod, blockClassName);
    }

    static Identifier getCompatibilityBlockId(Identifier id) {
        Identifier cId = blockIdsCompatibilityHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }
}
