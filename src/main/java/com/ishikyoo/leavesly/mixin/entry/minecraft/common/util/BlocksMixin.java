package com.ishikyoo.leavesly.mixin.entry.minecraft.common.util;

import com.ishikyoo.leavesly.LeaveslyBlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin (Blocks.class)
public abstract class BlocksMixin {
    @Inject(at = @At("RETURN"), method = "register(Ljava/lang/String;Lnet/minecraft/block/Block;)Lnet/minecraft/block/Block;")
    private static void injectRegister(String id, Block block, CallbackInfoReturnable<Block> cir) {
        if (LeaveslyBlockRegistry.isPreregisteredBlockClass(block))
            LeaveslyBlockRegistry.register("minecraft:" + id, block);
    }
}
