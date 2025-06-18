package com.ishikyoo.leavesly;

import com.ishikyoo.iyoo.state.property.SubProperty;
import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.iyoo.state.property.BitsmartProperty;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.leavesly.settings.LeaveslySettings;
import com.ishikyoo.leavesly.snowlayer.SnowLayerBlock;
import com.ishikyoo.leavesly.util.Deobfuscator;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.minecraft.util.Identifier;

public class LeaveslyPreLaunch implements PreLaunchEntrypoint  {
    enum TEST {
        AAA,
        BBB
    }

    @Override
    public void onPreLaunch() {
        BitsmartProperty property = BitsmartProperty.of(Identifier.of(Leavesly.MOD_ID, "snow_layer"), 6);
        SubProperty<Integer> coverage = property.registerInt(
                "coverage",
                SnowLayerBlock.PROPERTY_COVERAGE_MIN,
                SnowLayerBlock.PROPERTY_COVERAGE_MAX);
        SubProperty<Boolean> dynamic = property.registerBool("dynamic");
        property.lock();

        BitsmartRegistry.register(property);

        Blocks.preInitialize();
        Deobfuscator.initialize();
        Leavesly.settings = LeaveslySettings.of("leavesly.json");
        Leavesly.settings.initialize();
        if (Leavesly.isDebug())
            Leavesly.LOGGER.warn("[DEBUG] Leavesly is running in debug mode!");
    }
}