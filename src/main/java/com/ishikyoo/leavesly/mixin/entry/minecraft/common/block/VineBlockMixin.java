package com.ishikyoo.leavesly.mixin.entry.minecraft.common.block;

import com.ishikyoo.leavesly.SnowLayerLogic;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VineBlock.class)
public abstract class VineBlockMixin extends Block {
    public VineBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initInject(AbstractBlock.Settings settings, CallbackInfo ci) {
        SnowLayerLogic.setDefaultState(this, this.stateManager);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties")
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        SnowLayerLogic.appendProperties(this, builder);
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return SnowLayerLogic.hasRandomTick(state);
    }

    @Inject(at = @At("HEAD"), method = "randomTick")
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        SnowLayerLogic.randomTick(state, world, pos, random);
    }
}
