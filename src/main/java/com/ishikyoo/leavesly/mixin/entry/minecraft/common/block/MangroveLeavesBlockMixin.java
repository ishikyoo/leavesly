package com.ishikyoo.leavesly.mixin.entry.minecraft.common.block;

import net.minecraft.block.*;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MangroveLeavesBlock.class)
public abstract class MangroveLeavesBlockMixin extends LeavesBlock {
    public MangroveLeavesBlockMixin(Settings settings) {
        super(settings);
    }
}

