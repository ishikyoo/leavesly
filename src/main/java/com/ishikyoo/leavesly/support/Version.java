package com.ishikyoo.leavesly.support;

import com.ishikyoo.leavesly.Leavesly;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;

import java.util.Optional;

public class Version {
    private Version(int value) {
        this.value = value;
    }

    private static final Logger LOG = Leavesly.LOGGER;

    public static final int MIN_VALUE = 0;
    public static final int MAJOR_MAX_VALUE = 255;
    public static final int MINOR_PATCH_MAX_VALUE = 4095;

    public static final Version CHERRY_LEAVES_BLOCK = Version.of(1, 20, 0);
    public static final Version PALE_OAK_LEAVES_BLOCK = Version.of(1, 21, 2);
    public static final Version SHORT_GRASS_BLOCK = Version.of(1, 20, 3);
    public static final Version PARTICLE_LEAVES_CLASS = Version.of(1, 21, 4);

    private static Version gameVer;
    private static Version modVer;

    private final int value;

    public static Version game() {
        if (gameVer == null)
            gameVer = Version.of(Leavesly.GAME_ID);
        return gameVer;
    }

    public static Version mod() {
        if (modVer == null)
            modVer = Version.of(Leavesly.MOD_ID);
        return modVer;
    }

    public String toString() {
        return getMajor() + "." + getMinor() + "." + getPatch();
    }

    public int getValue() {
        return value;
    }

    public int getMajor() {
        return (value & 0xFF000000) >> 24;
    }

    public int getMinor() {
        return (value & 0xFFF000) >> 12;
    }

    public int getPatch() {
        return value & 0xFFF;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;
        if (object.getClass() != this.getClass())
            return false;
        return value == ((Version) object).value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    public boolean olderThan(Version version) {
        return value < version.value;
    }

    public boolean olderEqualThan(Version version) {
        return value <= version.value;
    }

    public boolean newerThan(Version version) {
        return value > version.value;
    }

    public boolean newerEqualThan(Version version) {
        return value >= version.value;
    }

    public static Version of(String modid) {
        Optional<ModContainer> modContainerI = FabricLoader.getInstance().getModContainer(modid);
        if (modContainerI.isPresent()) {
            ModContainer modContainer = modContainerI.get();
            ModMetadata modMetadata = modContainer.getMetadata();
            String version = modMetadata.getVersion().getFriendlyString();
            String[] versionSplit = version.split("\\.");
            int major = Integer.parseInt(versionSplit[0]);
            int minor = Integer.parseInt(versionSplit[1]);
            int patch = versionSplit.length <= 2 ? 0 : Integer.parseInt(versionSplit[2]);
            return new Version(getValue(major, minor, patch));
        } else {
            LOG.error("Couldn't get the version of (modid: {}).", modid);
        }
        return null;
    }

    public static Version of(int major, int minor, int patch) {
        return new Version(getValue(clampValue(major, MAJOR_MAX_VALUE),
                clampValue(minor, MINOR_PATCH_MAX_VALUE),
                clampValue(patch, MINOR_PATCH_MAX_VALUE)));
    }

    public static Version of(int value) {
        return new Version(value);
    }

    private static int getValue(int major, int minor, int patch) {
        return major << 24 | minor << 12 | patch;
    }

    private static int clampValue(int value, int max) {
        return Math.max(MIN_VALUE, Math.min(max, value));
    }
}
