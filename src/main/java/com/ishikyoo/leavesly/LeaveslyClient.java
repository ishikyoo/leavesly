package com.ishikyoo.leavesly;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaveslyClient implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger(Leavesly.MOD_ID);

    @Override
    public void onInitializeClient() {
        LeaveslyColorProvider.initialize();
    }
}
