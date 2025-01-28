package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.settings.LeaveslySettings;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Leavesly implements ModInitializer {
    public static final String MOD_ID = "leavesly";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LeaveslySettings.initialize();
    }
}