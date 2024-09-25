package net.ishikyoo.leavesly.mixin;

import net.ishikyoo.leavesly.Leavesly;
import net.minecraft.block.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin {

    // Add the snowiness property to leaf blocks
    @Inject(at = @At("TAIL"), method = "appendProperties")
    private void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Leavesly.SNOW);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(AbstractBlock.Settings settings, CallbackInfo ci) {
        ((BlockInvoker) this).invokeSetDefaultState(((Block) (Object) this).getDefaultState().with(Leavesly.SNOW, 0));
    }

    @Inject(at = @At("RETURN"), method = "hasRandomTicks", cancellable = true)
    private void hasRandomTicks(BlockState state, CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "randomTick")
    private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        Leavesly.getInstance().tick(state, world, pos, random);
    }
}
