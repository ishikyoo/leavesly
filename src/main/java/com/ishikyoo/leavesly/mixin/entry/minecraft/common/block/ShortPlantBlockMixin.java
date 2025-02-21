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

@Mixin(ShortPlantBlock.class)
public abstract class ShortPlantBlockMixin extends PlantBlock {
    public ShortPlantBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initInject(AbstractBlock.Settings settings, CallbackInfo ci) {
        SnowLayerLogic.setDefaultState(this, this.stateManager);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        SnowLayerLogic.appendProperties(this, builder);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        SnowLayerLogic.randomTick(state, world, pos, random);
    }
}
