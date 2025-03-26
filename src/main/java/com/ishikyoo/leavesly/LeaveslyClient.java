package com.ishikyoo.leavesly;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;

public class LeaveslyClient implements ClientModInitializer {
    public static final Logger LOG = Leavesly.LOGGER;

    @Override
    public void onInitializeClient() {
        LeaveslyColorProvider.initialize();
    }
}