package com.ishikyoo.leavesly.mixin.entry.minecraft.twentyone.common.block;

import net.minecraft.block.CherryLeavesBlock;
import net.minecraft.block.LeavesBlock;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CherryLeavesBlock.class)
public abstract class CherryLeavesBlockMixin extends LeavesBlock {
    public CherryLeavesBlockMixin(Settings settings) {
        super(settings);
    }
}

