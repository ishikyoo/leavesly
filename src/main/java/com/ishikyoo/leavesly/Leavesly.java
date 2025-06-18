package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.leavesly.settings.LeaveslySettings;
import com.ishikyoo.leavesly.snowlayer.SnowLayerSystem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Leavesly implements ModInitializer {
    public static final String GAME_ID = "minecraft";
    public static final String MOD_ID = "leavesly";
    public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");
    public static final boolean DEV_ENV = FabricLoader.getInstance().isDevelopmentEnvironment();

    protected static LeaveslySettings settings;
    protected static SnowLayerSystem snowLayerSystem;

    public static Identifier currentBlockId;

    @Override
    public void onInitialize() {
        Blocks.initialize();
        initSnowLayerSystem();
    }

    public static LeaveslySettings getSettings() {
        return settings;
    }

    public static boolean isDebug() {
        return settings.getCurrent().isDebug();
    }

    public static boolean shouldLog() {
        return settings.getCurrent().shouldLog() | isDebug();
    }

    public static SnowLayerSystem getSnowLayerSystem() {
        return snowLayerSystem;
    }

    private void initSnowLayerSystem() {
        snowLayerSystem = new SnowLayerSystem();
        ServerWorldEvents.LOAD.register(snowLayerSystem::initialize);
        //ServerWorldEvents.UNLOAD.register(snowLayerSystem::reset);
    }
}