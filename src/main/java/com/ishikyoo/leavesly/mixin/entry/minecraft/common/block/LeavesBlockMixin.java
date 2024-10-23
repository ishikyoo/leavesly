package com.ishikyoo.leavesly.mixin.entry.minecraft.common.block;

import com.ishikyoo.leavesly.SnowLayerLogic;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initInject(AbstractBlock.Settings settings, CallbackInfo ci) {
        SnowLayerLogic.setDefaultState(this, this.stateManager);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties")
    protected void injectAppendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        SnowLayerLogic.appendProperties(this, builder);
    }

    @Inject(at = @At("RETURN"), method = "hasRandomTicks", cancellable = true)
    private void injectHasRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(cir.getReturnValue() | SnowLayerLogic.hasRandomTick(state));
    }

    @Inject(at = @At("HEAD"), method = "randomTick")
    protected void injectRandomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        SnowLayerLogic.randomTick(state, world, pos, random);
    }
}