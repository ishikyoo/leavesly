package net.ishikyoo.leavesly.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Block.class)
interface BlockInvoker {
    @Invoker("setDefaultState")
    void invokeSetDefaultState(BlockState state);
}
