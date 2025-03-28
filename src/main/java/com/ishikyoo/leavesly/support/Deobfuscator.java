package com.ishikyoo.leavesly.support;

import com.ishikyoo.leavesly.Leavesly;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import org.slf4j.Logger;
import java.util.HashMap;

public class Deobfuscator {
    private static final Logger LOG = Leavesly.LOGGER;
    private static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();

    private static final HashMap<String, String> obfuscatedClassNameHashMap = new HashMap<>();

    public static void initialize() {
        obfuscatedClassNameHashMap.put("net.minecraft.class_2397", "net.minecraft.block.LeavesBlock");
        obfuscatedClassNameHashMap.put("net.minecraft.class_7114", "net.minecraft.block.MangroveLeavesBlock");
        obfuscatedClassNameHashMap.put("net.minecraft.class_2541", "net.minecraft.block.VineBlock");
        obfuscatedClassNameHashMap.put("net.minecraft.class_2526", "net.minecraft.block.ShortPlantBlock");
        obfuscatedClassNameHashMap.put("net.minecraft.class_2320", "net.minecraft.block.TallPlantBlock");
        if (Version.game().olderThan(Version.PARTICLE_LEAVES_CLASS))
            obfuscatedClassNameHashMap.put("net.minecraft.class_8167", "net.minecraft.block.CherryLeavesBlock");
        else if (Version.game().newerEqualThan(Version.PARTICLE_LEAVES_CLASS) && Version.game().olderThan(Version.TINTED_UNTINTED_PARTICLE_LEAVES_CLASS))
            obfuscatedClassNameHashMap.put("net.minecraft.class_8167", "net.minecraft.block.ParticleLeavesBlock");
        else {
            obfuscatedClassNameHashMap.put("net.minecraft.class_10716", "net.minecraft.block.TintedParticleLeavesBlock");
            obfuscatedClassNameHashMap.put("net.minecraft.class_10717", "net.minecraft.block.UntintedParticleLeavesBlock");
        }
    }

    public static String getClassName(Block block) {
        return getClassName(block.getClass().getName());
    }

    public static String getClassName(String className) {
        return !DEV_ENV ? deobfuscate(className) : className;
    }

    private static String deobfuscate(String className) {
        String deobClassName = obfuscatedClassNameHashMap.get(className);
        return deobClassName != null ? deobClassName : className;
    }
}