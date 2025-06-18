package com.ishikyoo.iyoo.util;

import net.minecraft.util.math.MathHelper;

public final class MathUtils {
    public static double normalize(double value, double min, double max) {
        return (MathHelper.clamp(value, min, max) - min) / (max - min);
    }

    public static double denormalize(double value, double min, double max) {
        return min + (MathHelper.clamp(value, 0, 1) * (max - min));
    }
}
