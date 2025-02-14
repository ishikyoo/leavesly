package com.ishikyoo.leavesly.support;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Deobfuscator {
    protected static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");
    protected static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    protected static final Version MINE_VER = Version.of("minecraft");

    protected static final HashMap<String, String> obfuscatedClassNameHashMap = new HashMap<>(
            Map.ofEntries(
                    Map.entry("net.minecraft.class_2397", "net.minecraft.block.LeavesBlock"),
                    Map.entry("net.minecraft.class_2541", "net.minecraft.block.VineBlock"),
                    Map.entry("net.minecraft.class_2526", "net.minecraft.block.ShortPlantBlock"),
                    Map.entry("net.minecraft.class_2320", "net.minecraft.block.TallPlantBlock"),
                    Map.entry("net.minecraft.class_7114", "net.minecraft.block.MangroveLeavesBlock"),
                    Map.entry("net.minecraft.class_8167", "net.minecraft.block.ParticleLeavesBlock")
            ));

    protected static final HashMap<String, String> compClassNameHashMap = new HashMap<>(
            Map.ofEntries(
                    Map.entry("net.minecraft.block.ParticleLeavesBlock", "net.minecraft.block.CherryLeavesBlock")
            ));

    public static String getClassName(Block block) {
        return getClassName(block.getClass().getName());
    }

    public static String getClassName(String className) {
        String finalClassName;
        if (!DEV_ENV)
            finalClassName = deobfuscate(className);
        else
            finalClassName = className;
        return getCompClassName(finalClassName);
    }

    protected static String deobfuscate(String className) {
        String deobfuscatedClassName = obfuscatedClassNameHashMap.get(className);
        if (deobfuscatedClassName == null)
            LOGGER.warn("Trying to deobfuscate a null class name (ClassName: {})", className);
        return deobfuscatedClassName;
    }

    protected static String getCompClassName(String className) {
        if (MINE_VER.newer(Version.of(1, 21, 3)))
            return className;
        String compClassName = compClassNameHashMap.get(className);
        if (compClassName == null)
            return className;
        else
            return compClassName;
    }
}
