package com.ishikyoo.leavesly.mixin.entry.minecraft.common;

import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.settings.LeaveslySettings;
import com.ishikyoo.leavesly.snowlayer.SnowLayerSystem;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    private static final Logger LOG = Leavesly.LOGGER;

    @Inject(at = @At("TAIL"), method = "setSimulationDistance")
    public void setSimulationDistance(int simulationDistance, CallbackInfo ci) {
            //Set snow layer simulation distance
            int settingsSimulationDistance = Leavesly.getSettings().getCurrent().getSnowLayer().getSimulationDistance();
            if (settingsSimulationDistance == -1)
                Leavesly.getSnowLayerSystem().setSimulationDistance(simulationDistance);
            else
                Leavesly.getSnowLayerSystem().setSimulationDistance(settingsSimulationDistance);

            //Set snow layer simulation speed
            int settingsSimulationSpeed = Leavesly.getSettings().getCurrent().getSnowLayer().getSimulationSpeed();
            Leavesly.getSnowLayerSystem().setSimulationSpeed(settingsSimulationSpeed);
    }
}
