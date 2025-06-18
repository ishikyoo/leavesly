package com.ishikyoo.leavesly.mixin.entry.minecraft.common.block;

import com.ishikyoo.leavesly.Leavesly;
import com.ishikyoo.leavesly.block.Blocks;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.leavesly.snowlayer.SnowLayerBlock;
import net.minecraft.block.*;
import net.minecraft.state.StateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShortPlantBlock.class)
public abstract class ShortPlantBlockMixin extends PlantBlock {
    public ShortPlantBlockMixin(Settings settings) {
        super(settings);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    private void initInject(AbstractBlock.Settings settings, CallbackInfo ci) {
        if (Blocks.isSupportedBlockId(Leavesly.currentBlockId)) {
            if (Leavesly.isDebug())
                Leavesly.LOGGER.info("[DEBUG] Setting {} LEAVESLY_SNOW_LAYER to {}", Leavesly.currentBlockId, 0);
            setDefaultState(getDefaultState().with(BitsmartRegistry.get(SnowLayerBlock.PROPERTY_ID).property(), 0));
        }
        Leavesly.currentBlockId = null;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        if (Blocks.isSupportedBlockId(Leavesly.currentBlockId)) {
            if (Leavesly.isDebug())
                Leavesly.LOGGER.info("[DEBUG] Appending LEAVESLY_SNOW_LAYER to {}", Leavesly.currentBlockId);
            builder.add(BitsmartRegistry.get(SnowLayerBlock.PROPERTY_ID).property());
        }
    }
}