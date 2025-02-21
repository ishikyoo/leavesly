package com.ishikyoo.leavesly.support;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class Deobfuscator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");
    private static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();
    private static final Version MINE_VER = Version.of("minecraft");
    private static final Version MINE_COMP_VER = Version.of(1, 21, 3);

    private static final HashMap<String, String> obfuscatedClassNameHashMap = new HashMap<>(
            Map.ofEntries(
                    Map.entry("net.minecraft.class_2397", "net.minecraft.block.LeavesBlock"),
                    Map.entry("net.minecraft.class_2541", "net.minecraft.block.VineBlock"),
                    Map.entry("net.minecraft.class_2526", "net.minecraft.block.ShortPlantBlock"),
                    Map.entry("net.minecraft.class_2320", "net.minecraft.block.TallPlantBlock"),
                    Map.entry("net.minecraft.class_7114", "net.minecraft.block.MangroveLeavesBlock"),
                    Map.entry("net.minecraft.class_8167", "net.minecraft.block.ParticleLeavesBlock")
            ));

    private static final HashMap<String, String> compClassNameHashMap = new HashMap<>(
            Map.ofEntries(
                    Map.entry("net.minecraft.block.ParticleLeavesBlock", "net.minecraft.block.CherryLeavesBlock")
            ));

    public static String getClassName(Block block) {
        return getClassName(block.getClass().getName());
    }

    public static String getClassName(String className) {
        return getCompClassName(!DEV_ENV ? deobfuscate(className) : className);
    }

    private static String deobfuscate(String className) {
        return obfuscatedClassNameHashMap.get(className);
    }

    private static String getCompClassName(String className) {
        if (MINE_VER.newerThan(MINE_COMP_VER))
            return className;
        String compClassName = compClassNameHashMap.get(className);
        return compClassName == null ? className : compClassName;
    }
}