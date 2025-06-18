package com.ishikyoo.leavesly.mixin.entry.minecraft.common;

import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(DebugHud.class)
public class DebugHudMixin {

    @Inject(method = "getRightText", at = @At("RETURN"), cancellable = true)
    private void injectRightText(CallbackInfoReturnable<List<String>> cir) {
        cir.setReturnValue(com.ishikyoo.iyoo.client.gui.hud.DebugHud.getRightText(cir.getReturnValue()));
    }
}
