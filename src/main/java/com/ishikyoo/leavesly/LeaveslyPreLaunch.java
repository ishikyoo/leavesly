package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.settings.LeaveslySettings;
import com.ishikyoo.leavesly.support.Deobfuscator;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class LeaveslyPreLaunch implements PreLaunchEntrypoint  {
    @Override
    public void onPreLaunch() {
        Deobfuscator.initialize();
        LeaveslySettings.preInitialize();
    }
}
