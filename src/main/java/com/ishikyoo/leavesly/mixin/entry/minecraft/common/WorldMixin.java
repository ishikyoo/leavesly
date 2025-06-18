package com.ishikyoo.leavesly.mixin.entry.minecraft.common;

import com.ishikyoo.leavesly.Leavesly;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {

    @Inject(at = @At("RETURN"), method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z")
    private void setBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir) {
        World world = (World) (Object) this;
        if (cir.getReturnValue())
            Leavesly.getSnowLayerSystem().setBlockState(pos, world, state, flags);
    }

}
