package com.ishikyoo.leavesly.block;

import com.google.common.collect.Sets;
import com.ishikyoo.leavesly.support.Deobfuscator;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Blocks {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    public static Identifier tempMixinBlockId;

    protected static final HashSet<Identifier> supportedVanillaBlockIdHashSet = new HashSet<>(Arrays.asList(
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

    static final HashMap<Identifier, Identifier> compBlockIdHashMap = new HashMap<>(Map.of(
            Identifier.of("minecraft:grass"), Identifier.of("minecraft:short_grass")
    ));

    protected static final HashSet<String> blockClassNameHashSet = new HashSet<>();
    protected static final HashMap<Identifier, Block> blockHashMap = new HashMap<>();
    protected static final HashMap<Block, Identifier> blockIdHashMap = new HashMap<>();

    public static Block getBlock(Identifier id) {
        Block block = blockHashMap.get(id);
        if (block == null)
            LOGGER.warn("Trying to get unregistered block (Id: {})", id);
        return block;
    }

    public static Identifier getBlockId(Block block) {
        Identifier id = blockIdHashMap.get(block);
        if (id == null)
            LOGGER.warn("Trying to get unregistered block (Id: {})", id);
        return id;
    }

    public static void register(Identifier id, Block block) {
        if (id == null) {
            LOGGER.error("Trying to register a block with a null identifier");
            return;
        }
        if (block == null) {
            LOGGER.error("Trying to register a null block (Id: {}).", id);
            return;
        }
        Identifier blockId = getCompBlockId(id);
        if (!isRegisteredBlock(blockId)) {
            String blockClassName = Deobfuscator.getClassName(block);
            //registerBlockClassName(blockClassName);
            blockHashMap.put(blockId, block);
            blockIdHashMap.put(block, blockId);
            LOGGER.info("Registered block (Id: {}, Class: {}).", blockId, blockClassName);
        }
    }

    public static void registerBlockClassName(String className) {
        if (!isRegisteredBlockClassName(className)) {
            blockClassNameHashSet.add(className);
            LOGGER.info("Registered block class name (Class: {}).", className);
        } else {
            LOGGER.warn("Trying to register an already registered block class name (Class: {}).", className);
        }
    }

    public static boolean isRegisteredBlock(Identifier id) {
        return blockHashMap.containsKey(id);
    }

    public static boolean isRegisteredBlock(Block block) {
        return blockHashMap.containsValue(block);
    }

    public static boolean isRegisteredBlockClassName(String className) {
        return blockClassNameHashSet.contains(className);
    }

    public static boolean isSupportedVanillaBlock(Identifier id) {
        return supportedVanillaBlockIdHashSet.contains(id);
    }

    protected static Identifier getCompBlockId(Identifier id) {
        Identifier cId = compBlockIdHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }
}
