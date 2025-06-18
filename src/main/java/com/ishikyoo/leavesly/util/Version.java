package com.ishikyoo.leavesly.util;

import com.ishikyoo.leavesly.Leavesly;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import java.util.Optional;

public class Version {
    private Version(int value) {
        this.value = value;
    }

    private static final int DEFAULT_INDEX = 0;

    public static final int MIN_VALUE = 0;
    public static final int MAJOR_MAX_VALUE = 255;
    public static final int MINOR_PATCH_MAX_VALUE = 4095;

    public static final Version CHERRY_LEAVES_BLOCK = Version.of(1, 19, 4);
    public static final Version PALE_OAK_LEAVES_BLOCK = Version.of(1, 21, 2);
    public static final Version SHORT_GRASS_BLOCK = Version.of(1, 20, 3);
    public static final Version PARTICLE_LEAVES_CLASS = Version.of(1, 21, 4);
    public static final Version TINTED_UNTINTED_PARTICLE_LEAVES_CLASS = Version.of(1, 21, 5);

    public static final Version CLUTTER_MOD_PATCH = Version.of(0, 6, 0);

    private static Version gameVer;
    private static Version modVer;

    private final int value;

    public static Version game() {
        if (gameVer == null)
            gameVer = Version.of(FabricLoader.getInstance().getModContainer(Leavesly.GAME_ID));
        return gameVer;
    }

    public static Version mod() {
        if (modVer == null)
            modVer = Version.of(FabricLoader.getInstance().getModContainer(Leavesly.MOD_ID));
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

    public static Version of(Optional<ModContainer> modContainer) {
        return of(modContainer, DEFAULT_INDEX);
    }

    public static Version of(Optional<ModContainer> modContainer, int index) {
        if (modContainer.isPresent())
            return of(modContainer.get().getMetadata().getVersion().getFriendlyString(), index);
        return null;
    }

    public static Version of(String format) {
        return of(format, DEFAULT_INDEX);
    }

    public static Version of(String format, int index) {
        String[] versions = format.split("[-+]");
        if (index < versions.length) {
            String[] version = versions[index].split("\\.");
            int major = version.length == 0 ? 0 : Integer.parseInt(version[0]);
            int minor = version.length <= 1 ? 0 : Integer.parseInt(version[1]);
            int patch = version.length <= 2 ? 0 : Integer.parseInt(version[2]);
            return new Version(getValue(major, minor, patch));
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