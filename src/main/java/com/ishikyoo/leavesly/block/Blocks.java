package com.ishikyoo.leavesly.block;

import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.settings.BlockData;
import com.ishikyoo.leavesly.settings.LeaveslySettings;
import com.ishikyoo.leavesly.support.Deobfuscator;
import com.ishikyoo.leavesly.support.Version;
import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.*;

public class Blocks {
    private static final Logger LOG = Leavesly.LOGGER;

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

    private static final HashSet<String> supportedVanillaBlockClassNameHashSet = new HashSet<>(Arrays.asList(
            "net.minecraft.block.LeavesBlock",
            "net.minecraft.block.VineBlock",
            "net.minecraft.block.ShortPlantBlock",
            "net.minecraft.block.TallPlantBlock",
            "net.minecraft.block.MangroveLeavesBlock",
            "net.minecraft.block.CherryLeavesBlock",
            "net.minecraft.block.ParticleLeavesBlock"
    ));

    private static final HashMap<Identifier, Identifier> compBlockIdHashMap = new HashMap<>();
    private static final HashMap<Identifier, Identifier> origBlockIdHashMap = new HashMap<>();

    private static final HashMap<Identifier, Block> blockHashMap = new HashMap<>();
    private static final HashMap<Block, Identifier> blockIdHashMap = new HashMap<>();

    public static void initialize() {
        //Register blocks present in the settings
        HashMap<Identifier, BlockData> blocks = LeaveslySettings.getSettings().getBlocks();
        for(Map.Entry<Identifier, BlockData> entry : blocks.entrySet()) {
            Identifier id = entry.getKey();
            Block block = Registries.BLOCK.get(getOrigBlockId(id));
            register(id, block);
        }
        //Configure block ids compatibility
        if (Version.game().olderThan(Version.SHORT_GRASS_BLOCK)) {
            Identifier origId = Identifier.ofVanilla("grass");
            Identifier compId = Identifier.ofVanilla("short_grass");
            compBlockIdHashMap.put(origId, compId);
            origBlockIdHashMap.put(compId, origId);
        }
    }

    public static Block getBlock(Identifier id) {
        Identifier compId = getCompBlockId(id);
        Block block = blockHashMap.get(compId);
        return block != null ? block : Registries.BLOCK.get(getOrigBlockId(id));
    }

    public static Identifier getBlockId(Block block) {
        Identifier id = blockIdHashMap.get(block);
        return id != null ? id : getCompBlockId(Registries.BLOCK.getId(block));
    }

    public static void register(Identifier id, Block block) {
        if (id == null) {
            LOG.error("Trying to register a block with a null id!");
            return;
        }
        if (block == null) {
            LOG.error("Trying to register a null block (Id: {})!", id);
            return;
        }
        Identifier blockId = getCompBlockId(id);
        if (!isRegisteredBlockId(blockId)) {
            String blockClassName = Deobfuscator.getClassName(block);
            blockHashMap.put(blockId, block);
            blockIdHashMap.put(block, blockId);
            LOG.info("Registered block (Id: {}, Class: {}).", blockId, blockClassName);
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

    public static boolean isSupportedVanillaBlockId(Identifier id) {
        return supportedVanillaBlockIdHashSet.contains(getCompBlockId(id));
    }

    public static boolean isSupportedVanillaBlockClassName(String className) {
        return supportedVanillaBlockClassNameHashSet.contains(Deobfuscator.getClassName(className));
    }

    private static Identifier getCompBlockId(Identifier id) {
        Identifier cId = compBlockIdHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }

    private static Identifier getOrigBlockId(Identifier id) {
        Identifier cId = origBlockIdHashMap.get(id);
        if (cId != null)
            return cId;
        return id;
    }
}