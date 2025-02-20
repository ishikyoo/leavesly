package com.ishikyoo.leavesly.mixin;

import com.ishikyoo.leavesly.support.Version;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MixinPlugin implements IMixinConfigPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("Leavesly/Mixin");

    private static final String MINECRAFT_ID = "minecraft";
    private static final String MINECRAFT_ALL_ID = "common";
    private static final String MINECRAFT_21_ID = "twentyone";
    private static final String MINECRAFT_20_ID = "twenty";
    private static final String MINECRAFT_19_ID = "nineteen";

    private static final int MINECRAFT_ALL_VALUE = 255;

    private static final int MIXIN_MOD_SPLIT_INDEX = 5;
    private static final int MIXIN_MINOR_SPLIT_INDEX = 6;
    private static final int MIXIN_PATCH_SPLIT_INDEX = 7;

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        String mixinModId = getMixinModId(mixinClassName);
        if (mixinModId.equals(MINECRAFT_ID)) {
            Version mixinVersion = getMinecraftMixinVersion(mixinClassName);
            Version minecraftVersion = Version.of(MINECRAFT_ID);
            return isMinecraftVersionMixin(mixinVersion, minecraftVersion);
        }
        return false;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    private static boolean isMinecraftVersionMixin(Version mixin, Version minecraft) {
        if (mixin.getMajor() == minecraft.getMajor()) {
            if (mixin.getMinor() == MINECRAFT_ALL_VALUE)
                return true;
            if (mixin.getMinor() == minecraft.getMinor()) {
                if (mixin.getPatch() == MINECRAFT_ALL_VALUE)
                    return true;
                else return mixin.getPatch() == minecraft.getPatch();
            }
        }
        return false;
    }

    private static Version getMinecraftMixinVersion(String mixinClassName) {
        int major = 1;
        int minor = 0;
        int patch = 0;
        String[] mixinClassNameSplit = mixinClassName.split("\\.");
        String mixinMinorId = mixinClassNameSplit[MIXIN_MINOR_SPLIT_INDEX];
        String mixinPatchId = mixinClassNameSplit[MIXIN_PATCH_SPLIT_INDEX];
        if (mixinMinorId.equals(MINECRAFT_ALL_ID))
            minor = 255;
        else if (mixinMinorId.equals(MINECRAFT_21_ID))
            minor = 21;
        else if (mixinMinorId.equals(MINECRAFT_20_ID))
            minor = 20;
        else if (mixinMinorId.equals(MINECRAFT_19_ID))
            minor = 19;
        if (mixinPatchId.equals(MINECRAFT_ALL_ID))
            patch = 255;
        else if (mixinPatchId.equals("zero"))
            patch = 0;
        else if (mixinPatchId.equals("one"))
            patch = 1;
        else if (mixinPatchId.equals("two"))
            patch = 2;
        else if (mixinPatchId.equals("three"))
            patch = 3;
        else if (mixinPatchId.equals("four"))
            patch = 4;
        else if (mixinPatchId.equals("five"))
            patch = 5;
        else if (mixinPatchId.equals("six"))
            patch = 6;
        return Version.of(major, minor, patch);
    }

    private static String getMixinModId(String mixinClassName) {
        String[] split = mixinClassName.split("\\.");
        return split[MIXIN_MOD_SPLIT_INDEX];
    }
}