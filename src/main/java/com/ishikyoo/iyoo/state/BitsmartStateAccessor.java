package com.ishikyoo.iyoo.state;

import com.ishikyoo.iyoo.state.property.BitsmartProperty;
import com.ishikyoo.iyoo.state.property.BitsmartRegistry;
import com.ishikyoo.iyoo.state.property.SubProperty;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;

import java.util.Objects;

public final class BitsmartStateAccessor {

    private final BlockState state;
    private final BitsmartProperty property;
    private final int raw;

    private BitsmartStateAccessor(BlockState state, BitsmartProperty property, int raw) {
        this.state = Objects.requireNonNull(state, "BlockState cannot be null");
        this.property = Objects.requireNonNull(property, "BitsmartProperty cannot be null");
        this.raw = raw;
    }

    public static BitsmartStateAccessor of(BlockState state, IntProperty backing) {
        return new BitsmartStateAccessor(state, BitsmartRegistry.get(backing), state.get(backing));
    }

    public static BitsmartStateAccessor of(BlockState state, BitsmartProperty property) {
        property.lock();
        return new BitsmartStateAccessor(state, property, state.get(property.property()));
    }

    public static BitsmartStateAccessor of(BlockState state, BitsmartProperty property, int raw) {
        property.lock();
        state = state.with(property.property(), raw);
        return new BitsmartStateAccessor(state, property, raw);
    }

    public static BitsmartStateAccessor of(BitsmartStateAccessor accessor) {
        return new BitsmartStateAccessor(accessor.state, accessor.property, accessor.raw);
    }

    public <T> T get(SubProperty<T> sub) {
        validateSubProperty(sub);
        return sub.decode(raw);
    }

    public boolean is(SubProperty<Boolean> sub) {
        return get(sub);
    }

    public BitsmartStateAccessor toggle(SubProperty<Boolean> subProperty) {
        if (subProperty.type() != Boolean.class) {
            throw new IllegalArgumentException("SubProperty must be of type Boolean.");
        }
        Boolean currentValue = get(subProperty);
        Boolean newValue = !currentValue;
        return with(subProperty, newValue);
    }

    public <T> BitsmartStateAccessor with(SubProperty<T> sub, T value) {
        validateSubProperty(sub);
        int updated = sub.encode(value, raw);
        BlockState newState = state.with(property.property(), updated);
        return new BitsmartStateAccessor(newState, property, updated);
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

    private void validateSubProperty(SubProperty<?> sub) {
        if (sub == null) throw new NullPointerException("Sub-property cannot be null");
        if (!property.hasSubProperty(sub.name())) {
            throw new IllegalArgumentException("Sub-property '" + sub.name() +
                    "' does not belong to property: " + property.identifier());
        }
    }
}
