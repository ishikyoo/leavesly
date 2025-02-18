package com.ishikyoo.leavesly.support;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Version {
    private Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    int major;
    int minor;
    int patch;

    public String getString() {
        return major + "." + minor + "." + patch;
    }
    public int getMajor() {
        return major;
    }
    public int getMinor() {
        return minor;
    }
    public int getPatch() { return patch; }

    public boolean equal(Version version) {
        return (getMajor() + getMinor() + getPatch()) == (version.getMajor() + version.getMinor() + version.getPatch());
    }

    public boolean older(Version version) {
        return (getMajor() + getMinor() + getPatch()) < (version.getMajor() + version.getMinor() + version.getPatch());
    }

    public boolean newer(Version version) {
        return (getMajor() + getMinor() + getPatch()) > (version.getMajor() + version.getMinor() + version.getPatch());
    }

    public static Version of(String id) {
        Optional<ModContainer> modContainerI = FabricLoader.getInstance().getModContainer("minecraft");
        if (modContainerI.isPresent()) {
            ModContainer modContainer = modContainerI.get();
            ModMetadata modMetadata = modContainer.getMetadata();
            String version = modMetadata.getVersion().getFriendlyString();
            String[] versionSplit = version.split("\\.");
            int major = Integer.parseInt(versionSplit[0]);
            int minor = Integer.parseInt(versionSplit[1]);
            int patch = 0;
            if (versionSplit.length >= 3)
                patch = Integer.parseInt(versionSplit[2]);
            return new Version(major, minor, patch);
        } else {
            LOGGER.error("Couldn't get the version of (Mod: {})!", id);
        }
        return null;
    }

    public static Version of(int major, int minor, int patch) {
        return new Version(major, minor, patch);
    }
}