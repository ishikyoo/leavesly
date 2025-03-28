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

    private static final HashMap<Identifier, Identifier> compBlockIdHashMap = new HashMap<>();
    private static final HashMap<Identifier, Identifier> origBlockIdHashMap = new HashMap<>();

    private static final HashMap<Identifier, Block> blockHashMap = new HashMap<>();
    private static final HashMap<Block, Identifier> blockIdHashMap = new HashMap<>();
    private static final HashSet<String> blockClassNameHashSet = new HashSet<>();

    public static void preInitialize() {
        initRegisteredBlockClassNameHashSet();
    }

    public static void initialize() {
        applyBlockIdComp();
        registerBlocks();
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
        if (!isSupportedBlockId(blockId)) {
            String blockClassName = Deobfuscator.getClassName(block);
            blockHashMap.put(blockId, block);
            blockIdHashMap.put(block, blockId);
            if (!isSupportedBlockClassName(block))
                blockClassNameHashSet.add(blockClassName);
            LOG.info("Registered block (Id: {}, Class: {}).", blockId, blockClassName);
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

    public static boolean isSupportedBlockId(Identifier id) {
        return blockHashMap.containsKey(getCompBlockId(id));
    }

    public static boolean isSupportedBlock(Block block) {
        return blockIdHashMap.containsKey(block);
    }

    public static boolean isSupportedBlockClassName(Block block) {
        return isSupportedBlockClassName(block.getClass().getName());
    }

    public static boolean isRegisteredBlockId(Identifier blockId) {
        return blockHashMap.containsKey(getCompBlockId(blockId)) | Registries.BLOCK.containsId(getOrigBlockId(blockId));
    }

    public static boolean isSupportedBlockClassName(String className) {
        return blockClassNameHashSet.contains(Deobfuscator.getClassName(className));
    }

    private static void applyBlockIdComp() {
        LOG.info("Applying block id compatibility...");
        if (Version.game().olderThan(Version.SHORT_GRASS_BLOCK)) {
            Identifier origId = Identifier.of(Leavesly.GAME_ID, "grass");
            Identifier compId = Identifier.of(Leavesly.GAME_ID, "short_grass");
            compBlockIdHashMap.put(origId, compId);
            origBlockIdHashMap.put(compId, origId);
            LOG.info("Applied block id compatibility (orig: {}, comp: {}).", origId, compId);
        }
    }

    private static void registerBlocks() {
        LOG.info("Registering blocks...");
        HashMap<Identifier, BlockData> blocks = LeaveslySettings.getSettings().getBlocks();
        for(Map.Entry<Identifier, BlockData> entry : blocks.entrySet()) {
            Identifier id = entry.getKey();
            Block block = Registries.BLOCK.get(getOrigBlockId(id));
            register(id, block);
        }
    }

    private static void initRegisteredBlockClassNameHashSet() {
        blockClassNameHashSet.add("net.minecraft.block.LeavesBlock");
        blockClassNameHashSet.add("net.minecraft.block.VineBlock");
        blockClassNameHashSet.add("net.minecraft.block.ShortPlantBlock");
        blockClassNameHashSet.add("net.minecraft.block.TallPlantBlock");
        blockClassNameHashSet.add("net.minecraft.block.MangroveLeavesBlock");
        if (Version.game().olderThan(Version.PARTICLE_LEAVES_CLASS))
            blockClassNameHashSet.add("net.minecraft.block.CherryLeavesBlock");
        else if (Version.game().newerEqualThan(Version.PARTICLE_LEAVES_CLASS) && Version.game().olderThan(Version.TINTED_UNTINTED_PARTICLE_LEAVES_CLASS))
            blockClassNameHashSet.add("net.minecraft.block.ParticleLeavesBlock");
        else {
            blockClassNameHashSet.add("net.minecraft.block.TintedParticleLeavesBlock");
            blockClassNameHashSet.add("net.minecraft.block.UntintedParticleLeavesBlock");
        }
    }

    private static Identifier getCompBlockId(Identifier id) {
        Identifier cId = compBlockIdHashMap.get(id);
        return cId != null ? cId : id;
    }

    private static Identifier getOrigBlockId(Identifier id) {
        Identifier cId = origBlockIdHashMap.get(id);
        return cId != null ? cId : id;
    }
}