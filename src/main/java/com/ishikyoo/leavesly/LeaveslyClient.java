package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.snowlayer.SnowLayerColorProvider;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public class LeaveslyClient implements ClientModInitializer {
    public static final Logger LOG = Leavesly.LOGGER;

    private static SnowLayerColorProvider snowLayerColorProvider;

    @Override
    public void onInitializeClient() {
        snowLayerColorProvider = new SnowLayerColorProvider();
        snowLayerColorProvider.initialize();
    }

    public static SnowLayerColorProvider getSnowLayerColorProvider() {
        return snowLayerColorProvider;
    }
}