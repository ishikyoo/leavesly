package com.ishikyoo.leavesly.block;

import com.ishikyoo.leavesly.support.Deobfuscator;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Blocks {
    private static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    public static Identifier tempMixinBlockId;

    private static final HashSet<Identifier> supportedVanillaBlockIdHashSet = new HashSet<>(Arrays.asList(
            Identifier.ofVanilla("oak_leaves"),
            Identifier.ofVanilla("spruce_leaves"),
            Identifier.ofVanilla("birch_leaves"),
            Identifier.ofVanilla("jungle_leaves"),
            Identifier.ofVanilla("acacia_leaves"),
            Identifier.ofVanilla("cherry_leaves"),
            Identifier.ofVanilla("dark_oak_leaves"),
            Identifier.ofVanilla("pale_oak_leaves"),
            Identifier.ofVanilla("mangrove_leaves"),
            Identifier.ofVanilla("azalea_leaves"),
            Identifier.ofVanilla("flowering_azalea_leaves"),
            Identifier.ofVanilla("short_grass"),
            Identifier.ofVanilla("fern"),
            Identifier.ofVanilla("vine"),
            Identifier.ofVanilla("tall_grass"),
            Identifier.ofVanilla("large_fern")
    ));

    private static final HashMap<Identifier, Identifier> compBlockIdHashMap = new HashMap<>(Map.of(
            Identifier.of("minecraft:grass"), Identifier.of("minecraft:short_grass")
    ));

    private static final HashMap<Identifier, Block> blockHashMap = new HashMap<>();
    private static final HashMap<Block, Identifier> blockIdHashMap = new HashMap<>();

    public static Block getBlock(Identifier id) {
        Identifier compId = getCompBlockId(id);
        Block block = blockHashMap.get(compId);
        if (block == null)
            LOGGER.error("Trying to get unregistered block (Id: {})!", compId);
        return block;
    }

    public static Identifier getBlockId(Block block) {
        Identifier id = blockIdHashMap.get(block);
        if (id == null) {
            Identifier mineId = getCompBlockId(Registries.BLOCK.getId(block));
            LOGGER.error("Trying to get unregistered block id (Id: {})!", mineId);
            return null;
        }
        return id;
    }

    public static void register(Identifier id, Block block) {
        if (id == null) {
            LOGGER.error("Trying to register a block with a null id!");
            return;
        }
        if (block == null) {
            LOGGER.error("Trying to register a null block (Id: {})!", id);
            return;
        }
        Identifier blockId = getCompBlockId(id);
        if (!isRegisteredBlockId(blockId)) {
            String blockClassName = Deobfuscator.getClassName(block);
            blockHashMap.put(blockId, block);
            blockIdHashMap.put(block, blockId);
            LOGGER.info("Registered block (Id: {}, Class: {}).", blockId, blockClassName);
        }
    }

    public static boolean isRegisteredBlockId(Identifier id) {
        return blockHashMap.containsKey(getCompBlockId(id));
    }

    public static boolean isRegisteredBlock(Block block) {
        return blockIdHashMap.containsKey(block);
    }

    public static boolean isSupportedVanillaBlock(Identifier id) {
        return supportedVanillaBlockIdHashSet.contains(id);
    }

    private static Identifier getCompBlockId(Identifier id) {
        Identifier cId = compBlockIdHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }
}