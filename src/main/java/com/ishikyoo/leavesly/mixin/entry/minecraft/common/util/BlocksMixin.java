package com.ishikyoo.leavesly.mixin.entry.minecraft.common.util;

import com.ishikyoo.leavesly.LeaveslyBlockRegistry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin (Blocks.class)
public abstract class BlocksMixin {
    @Inject(at = @At("RETURN"), method = "register(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/Block;")
    private static void injectRegister(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, CallbackInfoReturnable<Block> cir) {
        Block block = cir.getReturnValue();
        if (LeaveslyBlockRegistry.isRegisteredBlock(block))
            LeaveslyBlockRegistry.register("minecraft", id, block);
    }
}