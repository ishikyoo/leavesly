package com.ishikyoo.leavesly.mixin.entry.minecraft.common.util;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin (Blocks.class)
public abstract class BlocksMixin {
    //public static final Logger LOGGER = LoggerFactory.getLogger("Leavesly");

    @Inject(at = @At("HEAD"), method = "register(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/Block;")
    private static void injectRegisterHead(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, CallbackInfoReturnable<Block> cir) {
        com.ishikyoo.leavesly.block.Blocks.tempMixinBlockId = Identifier.of(id);
    }

    @Inject(at = @At("RETURN"), method = "register(Ljava/lang/String;Ljava/util/function/Function;Lnet/minecraft/block/AbstractBlock$Settings;)Lnet/minecraft/block/Block;")
    private static void injectRegisterReturn(String id, Function<AbstractBlock.Settings, Block> factory, AbstractBlock.Settings settings, CallbackInfoReturnable<Block> cir) {
        Block block = cir.getReturnValue();
        Identifier blockId = Identifier.ofVanilla(id);
        if (com.ishikyoo.leavesly.block.Blocks.isSupportedVanillaBlock(blockId))
            com.ishikyoo.leavesly.block.Blocks.register(blockId, block);
    }
}