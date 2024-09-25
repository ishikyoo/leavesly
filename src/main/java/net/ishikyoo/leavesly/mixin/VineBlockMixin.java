package net.ishikyoo.leavesly.mixin;

import net.ishikyoo.leavesly.Leavesly;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VineBlock.class)
public abstract class VineBlockMixin extends Block {

    public VineBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "appendProperties")
    private void appendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        builder.add(Leavesly.SNOW);
    }

    // Set the default snowiness to zero
    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(AbstractBlock.Settings settings, CallbackInfo ci) {
        ((BlockInvoker) this).invokeSetDefaultState(((Block) (Object) this).getDefaultState().with(Leavesly.SNOW, 0));
    }

    @Override
    protected boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Inject(at = @At("HEAD"), method = "randomTick")
    private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        Leavesly.getInstance().tick(state, world, pos, random);
    }
}
