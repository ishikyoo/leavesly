package com.ishikyoo.leavesly.mixin.entry.minecraft.common.block;

import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.leavesly.snowlayer.SnowLayerBlock;
import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LeavesBlock.class)
public abstract class LeavesBlockMixin extends Block {
    public LeavesBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initInject(float leafParticleChance, AbstractBlock.Settings settings, CallbackInfo ci) {
        if (Blocks.isSupportedBlockId(Leavesly.currentBlockId)) {
            if (Leavesly.isDebug())
                Leavesly.LOGGER.info("[DEBUG] Setting {} LEAVESLY_SNOW_LAYER to {}", Leavesly.currentBlockId, 0);
            setDefaultState(getDefaultState().with(BitsmartRegistry.get(SnowLayerBlock.PROPERTY_ID).property(), 0));
        }
        Leavesly.currentBlockId = null;
    }

    @Inject(at = @At("TAIL"), method = "appendProperties")
    protected void injectAppendProperties(StateManager.Builder<Block, BlockState> builder, CallbackInfo ci) {
        if (Blocks.isSupportedBlockId(Leavesly.currentBlockId)) {
            if (Leavesly.isDebug())
                Leavesly.LOGGER.info("[DEBUG] Appending LEAVESLY_SNOW_LAYER to {}", Leavesly.currentBlockId);
            builder.add(BitsmartRegistry.get(SnowLayerBlock.PROPERTY_ID).property());
        }
    }
}