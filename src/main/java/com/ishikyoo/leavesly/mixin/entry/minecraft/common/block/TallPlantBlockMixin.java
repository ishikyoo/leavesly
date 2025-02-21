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

@Mixin(TallPlantBlock.class)
public abstract class TallPlantBlockMixin extends PlantBlock {
    public TallPlantBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initInject(AbstractBlock.Settings settings, CallbackInfo ci) {
        SnowLayerLogic.setDefaultState(this, this.stateManager);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        SnowLayerLogic.randomDoubleBlockTick(state, world, pos, random);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties")
    protected void injectAppendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        SnowLayerLogic.appendProperties(this, builder);
    }

}