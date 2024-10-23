package com.ishikyoo.leavesly.mixin.entry.minecraft.twenty.common.block;

import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CherryLeavesBlock.class)
public abstract class CherryLeavesBlockMixin extends LeavesBlock {
    public CherryLeavesBlockMixin(Settings settings) {
        super(settings);
    }
}

