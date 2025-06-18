package com.ishikyoo.leavesly.mixin.entry.minecraft.common;

import com.ishikyoo.leavesly.Leavesly;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin (Blocks.class)
public abstract class BlocksMixin {

    @Inject(at = @At("HEAD"), method = "register(Lnet/minecraft/registry/RegistryKey;Ljava/util/function/Function;Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/Block;")
    private static void injectRegisterHead(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, CallbackInfoReturnable<Block> cir) {
        Identifier blockId = key.getValue();
        if (Leavesly.getSettings().getDefault().containsBlock(blockId))
            Leavesly.currentBlockId = blockId;
    }

//    @Inject(at = @At("RETURN"), method = "register(Lnet/minecraft/registry/RegistryKey;Ljava/util/function/Function;Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/Block;")
//    private static void injectRegisterReturn(RegistryKey<Block> key, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, CallbackInfoReturnable<Block> cir) {
//        Block block = cir.getReturnValue();
//        if (com.ishikyoo.leavesly.block.Blocks.isSupportedBlockId(Leavesly.currentBlockId)) {
//            com.ishikyoo.leavesly.block.Blocks.register(Leavesly.currentBlockId, block);
//        }
//    }
}