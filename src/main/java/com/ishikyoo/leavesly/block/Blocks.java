package com.ishikyoo.leavesly.block;

import com.ishikyoo.leavesly.support.Deobfuscator;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Blocks {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    protected static final HashSet<String> supportedVanillaBlockIdHashSet = new HashSet<>(Arrays.asList(
            "minecraft:oak_leaves",
            "minecraft:spruce_leaves",
            "minecraft:birch_leaves",
            "minecraft:jungle_leaves",
            "minecraft:acacia_leaves",
            "minecraft:cherry_leaves",
            "minecraft:dark_oak_leaves",
            "minecraft:mangrove_leaves",
            "minecraft:azalea_leaves",
            "minecraft:flowering_azalea_leaves",
            "minecraft:short_grass",
            "minecraft:fern",
            "minecraft:vine",
            "minecraft:tall_grass",
            "minecraft:large_fern"
    ));

    protected static final HashSet<String> supportedVanillaBlockClassNameHashSet = new HashSet<>(Arrays.asList(
            "net.minecraft.block.LeavesBlock",
            "net.minecraft.block.VineBlock",
            "net.minecraft.block.ShortPlantBlock",
            "net.minecraft.block.TallPlantBlock",
            "net.minecraft.block.MangroveLeavesBlock",
            "net.minecraft.block.CherryLeavesBlock",
            "net.minecraft.block.ParticleLeavesBlock"
    ));

    static final HashMap<String, String> compBlockIdHashMap = new HashMap<>(Map.of(
            "minecraft:grass", "minecraft:short_grass"
    ));

    protected static final HashMap<String, Block> blockHashMap = new HashMap<>();
    protected static final HashMap<Block, String> blockIdHashMap = new HashMap<>();

    public static Block getBlock(String id) {
        String compId = getCompBlockId(id);
        Block block = blockHashMap.get(compId);
        if (block == null)
            LOGGER.error("Trying to get unregistered block (Id: {})!", compId);
        return block;
    }

    public static String getBlockId(Block block) {
        String id = blockIdHashMap.get(block);
        String mineId = getCompBlockId(Registries.BLOCK.getId(block).toString());
        if (id == null) {
            LOGGER.error("Trying to get unregistered block identifier (Id: {})!", mineId);
            return null;
        } else {
            if (!id.equals(mineId)) {
                LOGGER.error("Mismatch between Leavesly and Minecraft block id (Leavesly: {}, Minecraft: {})!", id, mineId);
                return null;
            }
        }
        return id;
    }

    public static void register(String id, Block block) {
        if (id == null) {
            LOGGER.error("Trying to register a block with a null identifier!");
            return;
        }
        if (block == null) {
            LOGGER.error("Trying to register a null block (Id: {}).", id);
            return;
        }
        String blockId = getCompBlockId(id);
        if (!isRegisteredBlockId(blockId)) {
            String blockClassName = Deobfuscator.getClassName(block);
            blockHashMap.put(blockId, block);
            blockIdHashMap.put(block, blockId);
            LOGGER.info("Registered block (Id: {}, Class: {}).", blockId, blockClassName);
        }
    }

    public static boolean isRegisteredBlockId(String id) {
        return blockHashMap.containsKey(getCompBlockId(id));
    }

    public static boolean isRegisteredBlock(Block block) {
        return isRegisteredBlockId(Registries.BLOCK.getId(block).toString());
    }

    public static boolean isSupportedVanillaBlockId(String id) {
        return supportedVanillaBlockIdHashSet.contains(getCompBlockId(id));
    }

    public static boolean isSupportedVanillaBlockClassName(String className) {
        return supportedVanillaBlockClassNameHashSet.contains(Deobfuscator.getClassName(className));
    }

    protected static String getCompBlockId(String id) {
        String cId = compBlockIdHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }
}