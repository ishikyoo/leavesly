package com.ishikyoo.leavesly.mixin.entry.minecraft.common;

import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.snowlayer.SnowLayerSystem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ServerWorld.class)
public class ServerWorldMixin {

    @Inject(at = @At("TAIL"), method = "tickChunk")
    private void tickChunk(WorldChunk chunk, int randomTickSpeed, CallbackInfo ci) {
        Leavesly.getSnowLayerSystem().tick(chunk);
    }

//    @Inject(at = @At("HEAD"), method = "tick")
//    public void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
//        Leavesly.getSnowLayerSystem().putOrRemoveChunks();
//    }

}
