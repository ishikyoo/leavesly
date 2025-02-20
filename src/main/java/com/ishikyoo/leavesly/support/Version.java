package com.ishikyoo.leavesly.support;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Version {
    private Version(int value) {
        this.value = value;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    private final int value;

    public String toString() {
        return getMajor() + "." + getMinor() + "." + getPatch();
    }

    public int getValue() {
        return value;
    }

    public int getMajor() {
        return (value & 0xFF0000) >> 16;
    }

    public int getMinor() {
        return (value & 0xFF00) >> 8;
    }

    public int getPatch() {
        return value & 0xFF;
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

    public boolean newerThan(Version version) {
        return value > version.value;
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
            LOGGER.error("Couldn't get the version of (Mod: {}).", modid);
        }
        return null;
    }

    public static Version of(int major, int minor, int patch) {
        return new Version(getValue(major, minor, patch));
    }

    public static Version of(int value) {
        return new Version(value);
    }

    private static int getValue(int major, int minor, int patch) {
        return major << 16 | minor << 8 | patch;
    }
}
