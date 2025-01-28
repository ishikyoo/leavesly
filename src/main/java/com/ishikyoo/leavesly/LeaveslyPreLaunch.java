package com.ishikyoo.leavesly;

import com.ishikyoo.leavesly.settings.LeaveslySettings;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class LeaveslyPreLaunch implements PreLaunchEntrypoint  {
    @Override
    public void onPreLaunch() {
        LeaveslySettings.preInitialize();
    }
}
