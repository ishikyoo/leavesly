package com.ishikyoo.iyoo.state;

import com.ishikyoo.iyoo.state.property.BitsmartProperty;
import com.ishikyoo.iyoo.state.property.SubProperty;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;

import java.util.Objects;

public final class BitsmartStateViewer {

    private final BlockState state;
    private final BitsmartProperty property;
    private final int raw;

    private BitsmartStateViewer(BlockState state, BitsmartProperty property, int raw) {
        this.state = Objects.requireNonNull(state, "BlockState cannot be null");
        this.property = Objects.requireNonNull(property, "BitsmartProperty cannot be null");
        this.raw = raw;
    }

    public static BitsmartStateViewer of(BlockState state, BitsmartProperty property) {
        property.lock();
        Objects.requireNonNull(state, "BlockState cannot be null");
        Objects.requireNonNull(property, "BitsmartProperty cannot be null");

        IntProperty backing = property.property();
        if (!state.contains(backing)) {
            throw new IllegalArgumentException("BlockState does not contain backing property: " + backing.getName());
        }

        int raw = state.get(backing);
        return new BitsmartStateViewer(state, property, raw);
    }

    public <T> T get(SubProperty<T> sub) {
        validate(sub);
        return sub.decode(raw);
    }

    public int raw() {
        return raw;
    }

    public BlockState state() {
        return state;
    }

    public BitsmartProperty property() {
        return property;
    }

    private void validate(SubProperty<?> sub) {
        if (sub == null) {
            throw new NullPointerException("SubProperty cannot be null");
        }
        if (!property.hasSubProperty(sub.name())) {
            throw new IllegalArgumentException("SubProperty '" + sub.name() +
                    "' is not part of BitsmartProperty '" + property.name() + "'");
        }
    }

    @Override
    public String toString() {
        return "BitsmartViewer[" + property.name() + "= " + raw + "]";
    }
}
